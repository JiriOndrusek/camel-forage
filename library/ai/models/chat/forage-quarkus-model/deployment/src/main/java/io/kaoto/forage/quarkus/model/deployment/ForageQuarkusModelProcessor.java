package io.kaoto.forage.quarkus.model.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class ForageQuarkusModelProcessor {

    private static final String FEATURE = "forage-quarkus-model";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }
}
