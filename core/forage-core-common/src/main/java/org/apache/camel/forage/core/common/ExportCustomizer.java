package org.apache.camel.forage.core.common;

import java.util.Set;
import org.apache.camel.dsl.jbang.core.common.RuntimeType;

public interface ExportCustomizer {

    default boolean isEnabled() {
        return true;
    }

    Set<String> resolveRuntimeDependencies(RuntimeType runtime);
}
