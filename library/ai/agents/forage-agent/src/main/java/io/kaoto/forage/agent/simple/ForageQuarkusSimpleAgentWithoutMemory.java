package io.kaoto.forage.agent.simple;

import dev.langchain4j.data.message.Content;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import io.kaoto.forage.agent.factory.ConfigurationAware;
import java.util.List;
import org.apache.camel.component.langchain4j.agent.api.Agent;
import org.apache.camel.component.langchain4j.agent.api.AgentConfiguration;
import org.apache.camel.component.langchain4j.agent.api.AiAgentBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple implementation of an AI agent that provides basic chat functionality
 */
public class ForageQuarkusSimpleAgentWithoutMemory implements Agent, ConfigurationAware {
    private static final Logger LOG = LoggerFactory.getLogger(ForageQuarkusSimpleAgentWithoutMemory.class);

    private AgentConfiguration configuration;

    // Cached AI service instances to avoid recreating proxies on every request
    private ForageAgentWithoutMemory cachedNoMemoryService;
    private ToolProvider lastToolProvider;

    public ForageQuarkusSimpleAgentWithoutMemory() {}

    @Override
    public void configure(AgentConfiguration configuration) {
        this.configuration = configuration;
    }

    private boolean hasMemory() {
        return configuration.getChatMemoryProvider() != null;
    }

    @Override
    public String chat(AiAgentBody aiAgentBody, ToolProvider toolProvider) {
        LOG.debug("Chatting using ForageAgent");

        LOG.debug("Chatting without memory");
        ForageAgentWithoutMemory agentService = createAiAgentService(toolProvider, ForageAgentWithoutMemory.class);

        if (aiAgentBody.getContent() != null) {
            if (aiAgentBody.getContent() instanceof List contents) {
                return agentService.chat(aiAgentBody.getUserMessage(), contents);
            } else if (aiAgentBody.getContent() instanceof Content content) {
                return agentService.chat(aiAgentBody.getUserMessage(), List.of(content));
            }
        }

        return aiAgentBody.getSystemMessage() != null
                ? agentService.chat(aiAgentBody.getUserMessage(), aiAgentBody.getSystemMessage())
                : agentService.chat(aiAgentBody.getUserMessage());
    }

    /**
     * Create AI service with a single universal tool that handles multiple Camel routes and Memory Provider.
     * Services are cached to avoid recreating proxies on every request when the toolProvider is unchanged.
     */
    @SuppressWarnings("unchecked")
    private ForageAgentWithoutMemory createAiAgentService(
            ToolProvider toolProvider, Class<ForageAgentWithoutMemory> clazz) {
        // Check if we can return a cached instance
        if (toolProvider == lastToolProvider) {
            LOG.debug("Reusing cached ForageAgentWithoutMemory service");
            return (ForageAgentWithoutMemory) cachedNoMemoryService;
        } else {
            // Tool provider changed, invalidate cache
            cachedNoMemoryService = null;
            lastToolProvider = toolProvider;
        }

        LOG.info("Creating new {} service", clazz.getSimpleName());
        AiServices<ForageAgentWithoutMemory> builder =
                AiServices.builder(ForageAgentWithoutMemory.class).chatModel(configuration.getChatModel());

        if (hasMemory()) {
            builder = builder.chatMemoryProvider(configuration.getChatMemoryProvider());
        }

        // Apache Camel Tool Provider
        if (toolProvider != null) {
            builder.toolProvider(toolProvider);
        }

        // RAG
        if (configuration.getRetrievalAugmentor() != null) {
            builder.retrievalAugmentor(configuration.getRetrievalAugmentor());
        }

        // Input Guardrails
        if (configuration.getInputGuardrailClasses() != null
                && !configuration.getInputGuardrailClasses().isEmpty()) {
            builder.inputGuardrailClasses((List) configuration.getInputGuardrailClasses());
        }

        // Output Guardrails
        if (configuration.getOutputGuardrailClasses() != null
                && !configuration.getOutputGuardrailClasses().isEmpty()) {
            builder.outputGuardrailClasses((List) configuration.getOutputGuardrailClasses());
        }

        ForageAgentWithoutMemory service = builder.build();
        //        T service = builder.build();

        // Cache the service
        cachedNoMemoryService = (ForageAgentWithoutMemory) service;

        return null;
    }
}
