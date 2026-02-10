package io.kaoto.forage.agent.runtime;

import io.kaoto.forage.agent.AgentBeanFactory;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;
import org.apache.camel.CamelContext;

@Recorder
public class ForageAgentRecorder {

    public void recordAgents(RuntimeValue<CamelContext> camelContext) {
        AgentBeanFactory agentBeanFactory = new AgentBeanFactory();
        agentBeanFactory.setCamelContext(camelContext.getValue());
        agentBeanFactory.configure();
    }
}
