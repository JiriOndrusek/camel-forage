package org.apache.camel.forage.plugin;

import java.io.InputStream;

/**
 * Utility class for jdbc configuration value processing and transformation in the Camel Forage framework.
 */
public final class ExportHelper {

    public enum DependenciesType {
        plain_jdbc,
        quarkus_jdbc,
        springBoot_jdbc,
        plain_jms,
        quarkus_jms,
        springBoot_jms
    }

    /**
     * Regexp for See {@link org.apache.camel.forage.core.util.config.ConfigStore#readPrefixes(InputStream, String)}
     * to extract all datasource groups from the properties file.
     *
     * <p>From properties
     * <pre>
     *     ds1.jdbc.url=jdbc:postgresql://localhost:5432/postgres
     *     ds2.jdbc.url=jdbc:mysql://localhost:3306/test
     * </pre>
     *  both <Strong>ds1, ds2</Strong> prefixes are extracted.
     * </p>
     * */
    public static final String JDBC_PREFIXES_REGEXP = "(.+).jdbc\\..*";

    public static final String JMS_PREFIXES_REGEXP = "(.+).jms\\..*";

    /**
     * Regexp to find any datasource property at all.
     *
     * <p>finds properties like
     * <pre>
     *     jdbc.url=jdbc:postgresql://localhost:5432/postgres
     *     jdbc.db.kind=postgresql
     * </pre>
     * */
    public static final String JDBC_REGEXP = "jdbc\\..*";

    /**
     * Gets the quarkus version from the versions.properties file. (which is populated during buildtime)
     *
     * @return the project version
     */
    public static String getQuarkusVersion() {
        return getString("quarkus.version", "Could not determine quarkus version from properties file.");
    }

    /**
     * Gets the camel version from the versions.properties file. (which is populated during buildtime)
     *
     * @return the project version
     */
    public static String getCamelVersion() {
        return getString("camel.version", "Could not determine quarkus version from properties file.");
    }

    /**
     * Gets the quarkus version from the versions.properties file. (which is populated during buildtime)
     *
     * @return the project version
     */
    public static String getProjectVersion() {
        return getString("jdbc.dependency.version", "Could not determine project version from properties file.");
    }

    /**
     * Gets the dependencies from the datasource-command.properties file. (which is populated during buildtime)
     *
     * @return the project version
     */
    public static String getDependencies(DependenciesType type) {
        return getString(
                type.name().replace("_", ".") + ".dependencies",
                "Could not determine dependencies from properties file.");
    }

    /**
     * Reads property from the file versions.properties (which contains build time resolved versions)
     */
    public static String getString(String key, String error) {
        try {
            java.util.Properties properties = new java.util.Properties();
            try (InputStream is =
                    ExportHelper.class.getClassLoader().getResourceAsStream("datasource-command.properties")) {
                if (is != null) {
                    properties.load(is);
                    String version = properties.getProperty(key);
                    if (version != null && !version.trim().isEmpty()) {
                        return version;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(error, e);
        }

        // Ultimate fallback
        return null;
    }
}
