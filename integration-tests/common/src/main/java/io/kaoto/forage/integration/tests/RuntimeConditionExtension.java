package io.kaoto.forage.integration.tests;

import java.util.Optional;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

/**
 * JUnit ExecutionCondition that evaluates runtime-specific disable annotations.
 *
 * <p>This extension checks for {@link DisableOnQuarkus}, {@link DisableOnCamelMain},
 * and {@link DisableOnSpringBoot} annotations and disables tests based on the current
 * runtime specified in {@link IntegrationTestSetupExtension#RUNTIME_PROPERTY}.
 */
public class RuntimeConditionExtension implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        String runtime = System.getProperty(IntegrationTestSetupExtension.RUNTIME_PROPERTY);
        boolean isCamelMain = runtime == null || runtime.isEmpty();
        boolean isSpringBoot = runtime != null && runtime.contains("spring-boot");
        boolean isQuarkus = runtime != null && runtime.contains("quarkus");

        // Check class-level annotations first
        Optional<DisableOnCamelMain> disableOnCamelMain =
                AnnotationSupport.findAnnotation(context.getElement(), DisableOnCamelMain.class);
        if (disableOnCamelMain.isPresent() && isCamelMain) {
            String reason = disableOnCamelMain.get().reason();
            return ConditionEvaluationResult.disabled(
                    "Test disabled on Camel Main runtime" + (reason.isEmpty() ? "" : ": " + reason));
        }

        Optional<DisableOnSpringBoot> disableOnSpringBoot =
                AnnotationSupport.findAnnotation(context.getElement(), DisableOnSpringBoot.class);
        if (disableOnSpringBoot.isPresent() && isSpringBoot) {
            String reason = disableOnSpringBoot.get().reason();
            return ConditionEvaluationResult.disabled(
                    "Test disabled on Spring Boot runtime" + (reason.isEmpty() ? "" : ": " + reason));
        }

        Optional<DisableOnQuarkus> disableOnQuarkus =
                AnnotationSupport.findAnnotation(context.getElement(), DisableOnQuarkus.class);
        if (disableOnQuarkus.isPresent() && isQuarkus) {
            String reason = disableOnQuarkus.get().reason();
            return ConditionEvaluationResult.disabled(
                    "Test disabled on Quarkus runtime" + (reason.isEmpty() ? "" : ": " + reason));
        }

        return ConditionEvaluationResult.enabled("No runtime-specific disable condition matched");
    }
}
