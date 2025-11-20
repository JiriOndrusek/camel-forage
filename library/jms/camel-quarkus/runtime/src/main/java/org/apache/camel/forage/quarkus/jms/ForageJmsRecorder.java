package org.apache.camel.forage.quarkus.jms;

import io.quarkus.runtime.annotations.Recorder;
import org.jboss.logging.Logger;

/**
 * Aggregation repository is created via Recorder
 */
@Recorder
public class ForageJmsRecorder {
    private static final org.jboss.logging.Logger LOG = Logger.getLogger(ForageJmsRecorder.class);
}
