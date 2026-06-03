# Issue #361: Export to Quarkus - Generate Native Properties

**GitHub Issue:** https://github.com/KaotoIO/forage/issues/361

## Problem Statement

Currently, when exporting a Camel route to Quarkus using `camel export --runtime=quarkus`, forage properties (e.g., `forage.spring.rabbitmq.host`, `forage.jdbc.url`) are written to `application.properties` in their original forage namespace. These properties are then translated at **runtime** by Quarkus `ConfigSourceFactory` adapters.

**Current Behavior:**
```properties
# Exported application.properties
forage.jdbc.url=jdbc:postgresql://localhost:5432/mydb
```

**Desired Behavior:**
```properties
# Exported application.properties
quarkus.datasource."dataSource".jdbc.url=jdbc:postgresql://localhost:5432/mydb
```

## Current Implementation

### Runtime Translation
Properties are currently translated at runtime by:
- **Quarkus ConfigSourceFactory implementations** (e.g., `ForageJdbcConfigSourceFactory`)
- These use **ForageModuleDescriptor.translateProperties()** to convert forage properties to Quarkus-native properties
- Translation logic exists in each module descriptor (e.g., `JdbcModuleDescriptor:61-109`, `SpringRabbitMQModuleDescriptor:65-68`)

### Export System
The export system uses:
- **PluginExporter SPI** - Camel JBang plugin interface for participating in exports
- **ExportCustomizer** - Forage-specific interface for adding runtime dependencies
- **CatalogDrivenExportCustomizer** - Scans properties and resolves dependencies
- **ForagePlugin.getExporter()** - Returns PluginExporter implementation

## Proposed Solution

Translate properties **at export time** instead of runtime, writing native Quarkus properties directly to `application.properties` during the export process.

## Implementation Approach

### Key Discovery
The `PluginExporter` interface provides a `getBuildProperties()` method:

```java
public interface PluginExporter {
    /**
     * Provide additional build properties.
     * @return build properties to add to the exported project.
     */
    default Properties getBuildProperties() {
        return new Properties();
    }
    // ... other methods
}
```

This is the perfect hook for adding translated properties to the exported project!

### Implementation Steps

#### 1. Extend ExportCustomizer Interface
**File:** `core/forage-core-common/src/main/java/io/kaoto/forage/core/common/ExportCustomizer.java`

Add a new method for property translation:
```java
/**
 * Translate forage properties to runtime-native properties for export.
 * 
 * @param runtime target runtime (main, springBoot, quarkus)
 * @return map of translated property names to values
 */
default Map<String, String> translatePropertiesForExport(RuntimeType runtime) {
    return Collections.emptyMap();
}
```

#### 2. Implement Property Translation in CatalogDrivenExportCustomizer
**File:** `tooling/camel-jbang-plugin-forage/src/main/java/io/kaoto/forage/plugin/CatalogDrivenExportCustomizer.java`

Add method to translate all scanned properties:

```java
@Override
public Map<String, String> translatePropertiesForExport(RuntimeType runtime) {
    if (runtime != RuntimeType.quarkus) {
        return Collections.emptyMap(); // Only translate for Quarkus
    }
    
    Map<String, String> translated = new HashMap<>();
    Map<String, Map<String, List<String>>> properties = getScannedProperties();
    
    for (String factoryTypeKey : properties.keySet()) {
        Map<String, List<String>> factoryProperties = properties.get(factoryTypeKey);
        
        // Load the module descriptor for this factory
        ForageModuleDescriptor<?, ?> descriptor = loadModuleDescriptor(factoryTypeKey);
        if (descriptor == null) {
            continue;
        }
        
        // Extract prefixes from scanned properties
        Set<String> prefixes = extractPrefixes(factoryTypeKey, factoryProperties);
        
        // Translate properties for each prefix
        for (String prefix : prefixes) {
            Config config = descriptor.createConfig(prefix);
            Map<String, String> props = descriptor.translateProperties(prefix, config);
            translated.putAll(props);
        }
    }
    
    return translated;
}

private ForageModuleDescriptor<?, ?> loadModuleDescriptor(String factoryTypeKey) {
    // Map factory type to module descriptor using catalog or hardcoded mapping
    // e.g., "jdbc" -> JdbcModuleDescriptor, "jms" -> JmsModuleDescriptor
}

private Set<String> extractPrefixes(String factoryTypeKey, 
                                    Map<String, List<String>> factoryProperties) {
    // Extract instance prefixes from property keys
    // e.g., "ds1.jdbc.url" -> prefix "ds1"
    // "jdbc.url" -> prefix null (default instance)
}
```

#### 3. Update ForagePlugin to Use getBuildProperties()
**File:** `tooling/camel-jbang-plugin-forage/src/main/java/io/kaoto/forage/plugin/ForagePlugin.java`

Modify the PluginExporter implementation:

```java
@Override
public Optional<PluginExporter> getExporter() {
    return Optional.of(new PluginExporter() {
        private RuntimeType currentRuntime;
        
        @Override
        public Set<String> getDependencies(RuntimeType runtimeType) {
            this.currentRuntime = runtimeType; // Store for getBuildProperties()
            
            return ExportHelper.getAllCustomizers()
                    .filter(ExportCustomizer::isEnabled)
                    .map(c -> c.resolveRuntimeDependencies(
                        io.kaoto.forage.core.common.RuntimeType.fromValue(runtimeType.name())))
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet());
        }

        @Override
        public Properties getBuildProperties() {
            if (currentRuntime == null) {
                return new Properties();
            }
            
            Properties props = new Properties();
            
            // Get translated properties from all customizers
            ExportHelper.getAllCustomizers()
                    .filter(ExportCustomizer::isEnabled)
                    .forEach(customizer -> {
                        Map<String, String> translated = customizer.translatePropertiesForExport(
                            io.kaoto.forage.core.common.RuntimeType.fromValue(currentRuntime.name()));
                        props.putAll(translated);
                    });
            
            return props;
        }

        @Override
        public boolean contributeRuntimeDependencies() {
            return true;
        }

        @Override
        public void addSourceFiles(Path buildDir, String packageName, Printer printer) {}
    });
}
```

#### 4. Module Descriptor Discovery
Create a helper class to map factory types to module descriptors:

**File:** `tooling/camel-jbang-plugin-forage/src/main/java/io/kaoto/forage/plugin/ModuleDescriptorRegistry.java`

```java
public class ModuleDescriptorRegistry {
    private static final Map<String, Class<? extends ForageModuleDescriptor<?, ?>>> DESCRIPTORS = 
        Map.of(
            "jdbc", JdbcModuleDescriptor.class,
            "jms", JmsModuleDescriptor.class,
            "spring.rabbitmq", SpringRabbitMQModuleDescriptor.class,
            "agent", AgentModuleDescriptor.class,
            "cxf", CxfModuleDescriptor.class
            // Add more as needed
        );
    
    public static ForageModuleDescriptor<?, ?> getDescriptor(String factoryTypeKey) {
        Class<? extends ForageModuleDescriptor<?, ?>> descriptorClass = 
            DESCRIPTORS.get(factoryTypeKey);
        if (descriptorClass == null) {
            return null;
        }
        try {
            return descriptorClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }
}
```

### Optional Enhancement: Remove Original Forage Properties

After export, optionally remove forage-namespaced properties from `application.properties` to avoid confusion:

```java
@Override
public void addSourceFiles(Path buildDir, String packageName, Printer printer) {
    if (currentRuntime == RuntimeType.QUARKUS) {
        removeForagePropertiesFromExport(buildDir);
    }
}

private void removeForagePropertiesFromExport(Path buildDir) {
    // Read application.properties
    // Remove lines starting with "forage."
    // Write back
}
```

## Files to Modify

1. **ExportCustomizer.java** - Add `translatePropertiesForExport()` method
2. **CatalogDrivenExportCustomizer.java** - Implement property translation
3. **ForagePlugin.java** - Use `getBuildProperties()` to add translated properties
4. **ModuleDescriptorRegistry.java** (new) - Map factory types to descriptors

## Testing Strategy

1. **Unit Tests** - Add tests to `CatalogDrivenExportCustomizerTest`:
   - Test JDBC property translation for Quarkus
   - Test JMS property translation for Quarkus
   - Test Spring RabbitMQ (should return empty - no translation needed)
   - Test named instances (prefixed properties)
   - Test that main/springBoot runtimes don't translate

2. **Integration Tests** - Add export integration tests:
   - Export a project with forage properties to Quarkus
   - Verify `application.properties` contains Quarkus-native properties
   - Verify exported project builds and runs successfully

## Open Questions

1. **Should we remove original forage properties?** 
   - Pro: Cleaner, no confusion
   - Con: May break if users manually added forage properties for other purposes

2. **What about Spring Boot exports?**
   - Spring Boot modules may also have property translation needs
   - Check if any `ForageModuleDescriptor` implementations return non-empty maps for Spring Boot

3. **How to handle errors in translation?**
   - Log warnings if a module descriptor can't be loaded?
   - Skip that module and continue with others?

## Benefits

- ✅ Exported Quarkus projects are self-contained (no runtime translation needed)
- ✅ Easier to understand and debug (native properties visible in `application.properties`)
- ✅ Consistent with Quarkus conventions
- ✅ Reduces runtime overhead (no property translation at startup)
- ✅ Leverages existing `ForageModuleDescriptor.translateProperties()` logic

## Migration Impact

- **No breaking changes** for existing users
- Exported projects will have different `application.properties` but same runtime behavior
- Users can still use forage properties during development with `camel run`
