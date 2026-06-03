# Review of `review-issue-361-implementation-plan.md`

## Context

Issue #361 asks to translate forage properties to Quarkus-native format at export time (`camel export --runtime=quarkus`). Two documents exist: an **implementation plan** and a **review** that declares the plan unworkable. This analysis evaluates both against the actual codebase and proposes a corrected approach.

---

## What the Review Gets RIGHT

**1. Hard-coded `ModuleDescriptorRegistry` is bad (#4).** Agreed. A `Map.of("jdbc", JdbcModuleDescriptor.class, ...)` defeats the modular design. ForageModuleDescriptors should be discovered via ServiceLoader (no ServiceLoader files exist for them yet, but they can be added).

**2. Silent failures should be logged (#6).** Agreed. Skipping untranslatable modules without warning is a debugging nightmare.

**3. `getBuildProperties()` call-order concerns (#5).** Partially valid. But both documents miss the larger issue: `getBuildProperties()` likely returns build-system properties (Maven/Gradle), **not** `application.properties` entries. The correct hook is `addSourceFiles(Path buildDir, ...)` which gives direct access to the exported project filesystem.

---

## What the Review Gets WRONG

### "BLOCKER #1": Config objects cannot work at export time — WRONG

The review claims ConfigStore only queries live env/sys vars. The actual code shows otherwise:

- **`ConfigStore.set(ConfigModule, String)`** (`ConfigStore.java:326`) directly stores values in the internal `Properties` object, bypassing the resolver chain entirely.
- **`AbstractConfig.register(String name, String value)`** (`AbstractConfig.java:37-39`) calls `ConfigEntries.find()` to match the property name to a `ConfigModule`, then calls `ConfigStore.set()`.
- **`ConfigStore.get(ConfigModule)`** (`ConfigStore.java:273-274`) reads from the internal `Properties` store — it does NOT re-query env/sys at read time.

Therefore: if you call `createConfig(prefix)` then `config.register(fullPropertyName, value)` for each scanned property, the Config object will have the correct values and `translateProperties()` will work.

### "BLOCKER #2": Prefix information is irreversibly lost — WRONG

The review only examined `scanProperties()`. There is also:

```java
// ForagePropertyScanner.java:78-79
public static Map<String, Map<String, List<PropertyOccurrence>>> scanPropertiesWithFileTracking(
        File directory, ForageCatalogReader catalog, boolean trackUnknown)
```

Where `PropertyOccurrence` (`ForagePropertyScanner.java:33`) is:
```java
public record PropertyOccurrence(File file, String fullPropertyName, String value) {}
```

The `fullPropertyName` field preserves the complete key (e.g., `"forage.ds1.jdbc.url"`). Prefix extraction is straightforward: strip `"forage."`, check if the next segment equals `modulePrefix()`. If not, that segment is the named prefix.

### "CRITICAL #3": No mapping between scanned names and Config methods — OVERSTATED

The mapping exists and is the same mechanism used at runtime. `AbstractConfig.register(name, value)` calls `ConfigEntries.find()` which iterates all registered `ConfigModule`s and calls `ConfigModule.match(name)`. The `match()` method compares against `buildPrefixedName()`:

```java
// ConfigModule.java:172-180
private String buildPrefixedName() {
    if (prefix == null) return name;
    if (name.startsWith("forage."))
        return "forage." + prefix + "." + name.substring(7);
    return prefix + "." + name;
}
```

For a Config created with prefix `"ds1"` and a base module `"forage.jdbc.url"`, the prefixed module's `buildPrefixedName()` returns `"forage.ds1.jdbc.url"` — which is exactly what `PropertyOccurrence.fullPropertyName` contains.

---

## What BOTH Documents Miss

**1. `getBuildProperties()` is the wrong hook.** Neither document verifies what Camel JBang actually does with these properties. The safe approach is `addSourceFiles(Path buildDir, ...)` — it gives filesystem access to the exported project, where we can directly read and rewrite `src/main/resources/application.properties`.

**2. `AgentModuleDescriptor` classpath check.** `AgentModuleDescriptor.translateProperties()` (`AgentModuleDescriptor.java:77-84`) calls `Class.forName()` for Quarkus extension classes. At export time, these aren't on the classpath, so it returns an empty map. Needs a `translatePropertiesForExport()` override or a way to skip the check.

**3. Placeholder resolution at export time.** `register()` calls `PlaceholderResolver.resolve(value)`, which would resolve `{{env:DB_URL}}` against the export machine's environment — wrong. This is an edge case (most users use literal values), but worth noting.

---

## Corrected Approach

### Hook: `addSourceFiles(Path buildDir, ...)` in ForagePlugin

The `addSourceFiles` method (`ForagePlugin.java:73`, currently empty) receives the exported project's build directory. Implementation:

1. Return early if runtime is not Quarkus
2. Read `buildDir/src/main/resources/application.properties`
3. For each `forage.*` property:
   - Use `scanPropertiesWithFileTracking()` on the user's working directory to get `PropertyOccurrence` objects
   - Extract prefix from `PropertyOccurrence.fullPropertyName` using the descriptor's `modulePrefix()`
   - Group properties by `(factoryTypeKey, prefix)`
4. For each group:
   - Call `descriptor.createConfig(prefix)` — registers ConfigModules
   - Call `config.register(fullPropertyName, value)` for each property — populates ConfigStore
   - Call `descriptor.translateProperties(prefix, config)` — produces quarkus-native properties
5. Rewrite the exported `application.properties`: remove translated forage keys, add quarkus keys
6. Call `ConfigStore.getInstance().reload()` to clean up the singleton

### Descriptor Discovery

Add `META-INF/services/io.kaoto.forage.core.common.ForageModuleDescriptor` in each `-common` module. Match discovered descriptors to scanned factory types via `modulePrefix()`.

### AgentModuleDescriptor Export Mode

Add a default method to `ForageModuleDescriptor`:
```java
default Map<String, String> translatePropertiesForExport(String prefix, C config) {
    return translateProperties(prefix, config);
}
```
Override in `AgentModuleDescriptor` to skip the `Class.forName()` check.

### Files to Modify

| File | Change |
|------|--------|
| `ForagePlugin.java` | Implement `addSourceFiles()`, capture runtime from `getDependencies()` |
| `ForageModuleDescriptor.java` | Add `translatePropertiesForExport()` default method |
| `AgentModuleDescriptor.java` | Override `translatePropertiesForExport()` to skip classpath check |
| `tooling/camel-jbang-plugin-forage/pom.xml` | Add `-common` module deps (with heavy transitive exclusions) |
| 5x `META-INF/services/` files | ServiceLoader registration for each descriptor |
| New: `QuarkusPropertyTranslator.java` | Orchestrates scan → group → populate → translate → rewrite |

### Verification

1. Unit test `extractPrefix()` for all patterns (`null`, named, multi-segment module prefix)
2. Unit test the full translate flow with temp directories containing `application.properties`
3. Integration test: mock an exported project dir, run `addSourceFiles()`, verify the rewritten properties
4. Test that modules without translation (CXF, SpringRabbitMQ) are harmlessly skipped with a logged warning
5. Test multiple named instances (`ds1`, `ds2`) produce separate Quarkus property blocks
