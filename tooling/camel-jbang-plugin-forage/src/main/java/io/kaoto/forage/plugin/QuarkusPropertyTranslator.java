package io.kaoto.forage.plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.kaoto.forage.catalog.reader.ForageCatalogReader;
import io.kaoto.forage.core.common.ForageModuleDescriptor;
import io.kaoto.forage.core.util.config.Config;
import io.kaoto.forage.core.util.config.ConfigStore;
import io.kaoto.forage.plugin.ForagePropertyScanner.PropertyOccurrence;

/**
 * Translates forage properties to Quarkus-native properties at export time.
 * Uses existing {@link ForageModuleDescriptor#translatePropertiesForExport} logic
 * by populating {@link ConfigStore} with scanned property values.
 */
public final class QuarkusPropertyTranslator {

    private static final Logger LOG = LoggerFactory.getLogger(QuarkusPropertyTranslator.class);

    private QuarkusPropertyTranslator() {}

    public record TranslationResult(Map<String, String> quarkusProperties, Set<String> translatedForageKeys) {}

    /**
     * Translates forage properties found in the working directory to Quarkus-native properties.
     *
     * @param workingDir the directory containing route and properties files
     * @return translation result with quarkus properties and the forage keys that were translated
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static TranslationResult translate(File workingDir) throws IOException {
        ForageCatalogReader catalog = ForageCatalogReader.getInstance();

        Map<String, Map<String, List<PropertyOccurrence>>> scanned =
                ForagePropertyScanner.scanPropertiesWithFileTracking(workingDir, catalog, false);

        Map<String, ForageModuleDescriptor> descriptors = discoverDescriptors();

        Map<String, String> quarkusProperties = new LinkedHashMap<>();
        Set<String> translatedForageKeys = new LinkedHashSet<>();

        for (Map.Entry<String, Map<String, List<PropertyOccurrence>>> factoryEntry : scanned.entrySet()) {
            String factoryTypeKey = factoryEntry.getKey();
            Map<String, List<PropertyOccurrence>> factoryProperties = factoryEntry.getValue();

            ForageModuleDescriptor descriptor = descriptors.get(factoryTypeKey);
            if (descriptor == null) {
                LOG.warn(
                        "No ForageModuleDescriptor for factory type '{}' — properties will not be translated to Quarkus format",
                        factoryTypeKey);
                continue;
            }

            Map<String, Map<String, String>> byPrefix = groupByPrefix(factoryProperties, descriptor.modulePrefix());

            for (Map.Entry<String, Map<String, String>> prefixEntry : byPrefix.entrySet()) {
                String prefix = prefixEntry.getKey();
                Map<String, String> props = prefixEntry.getValue();

                try {
                    Config config = descriptor.createConfig(prefix);

                    for (Map.Entry<String, String> prop : props.entrySet()) {
                        config.register(prop.getKey(), prop.getValue());
                    }

                    Map<String, String> translated = descriptor.translatePropertiesForExport(prefix, config);

                    if (!translated.isEmpty()) {
                        quarkusProperties.putAll(translated);
                        translatedForageKeys.addAll(props.keySet());
                    }
                } catch (Exception e) {
                    LOG.warn(
                            "Failed to translate properties for module '{}' prefix '{}': {}",
                            factoryTypeKey,
                            prefix,
                            e.getMessage());
                    LOG.debug("Translation error details:", e);
                }
            }
        }

        return new TranslationResult(quarkusProperties, translatedForageKeys);
    }

    @SuppressWarnings("rawtypes")
    private static Map<String, ForageModuleDescriptor> discoverDescriptors() {
        Map<String, ForageModuleDescriptor> result = new HashMap<>();
        try {
            ServiceLoader<ForageModuleDescriptor> loader = ServiceLoader.load(ForageModuleDescriptor.class);
            for (ForageModuleDescriptor descriptor : loader) {
                result.put(descriptor.modulePrefix(), descriptor);
                LOG.debug(
                        "Discovered ForageModuleDescriptor: {} → {}",
                        descriptor.modulePrefix(),
                        descriptor.getClass().getName());
            }
        } catch (Exception e) {
            LOG.warn("Error discovering ForageModuleDescriptors: {}", e.getMessage());
            LOG.debug("ServiceLoader error details:", e);
        }
        return result;
    }

    /**
     * Groups scanned properties by their named prefix.
     *
     * <p>For {@code "forage.ds1.jdbc.url=..."} with modulePrefix {@code "jdbc"},
     * the prefix is {@code "ds1"} and the full key {@code "forage.ds1.jdbc.url"} is preserved.
     *
     * <p>For {@code "forage.jdbc.url=..."} the prefix is {@code null}.
     *
     * @return map of prefix (nullable) → (fullPropertyName → value)
     */
    private static Map<String, Map<String, String>> groupByPrefix(
            Map<String, List<PropertyOccurrence>> factoryProperties, String modulePrefix) {
        Map<String, Map<String, String>> result = new LinkedHashMap<>();

        for (List<PropertyOccurrence> occurrences : factoryProperties.values()) {
            for (PropertyOccurrence occ : occurrences) {
                String prefix = extractPrefix(occ.fullPropertyName(), modulePrefix);
                result.computeIfAbsent(prefix, k -> new LinkedHashMap<>()).put(occ.fullPropertyName(), occ.value());
            }
        }

        return result;
    }

    /**
     * Extracts the named prefix from a full forage property name.
     *
     * <p>Examples with modulePrefix {@code "jdbc"}:
     * <ul>
     *   <li>{@code "forage.jdbc.url"} → {@code null} (default instance)</li>
     *   <li>{@code "forage.ds1.jdbc.url"} → {@code "ds1"}</li>
     * </ul>
     */
    static String extractPrefix(String fullPropertyName, String modulePrefix) {
        if (!fullPropertyName.startsWith("forage.")) {
            return null;
        }
        String afterForage = fullPropertyName.substring("forage.".length());

        if (afterForage.startsWith(modulePrefix + ".") || afterForage.equals(modulePrefix)) {
            return null;
        }

        int firstDot = afterForage.indexOf('.');
        if (firstDot > 0) {
            String remaining = afterForage.substring(firstDot + 1);
            if (remaining.startsWith(modulePrefix + ".") || remaining.equals(modulePrefix)) {
                return afterForage.substring(0, firstDot);
            }
        }

        return null;
    }
}
