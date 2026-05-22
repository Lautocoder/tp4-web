package ht.lafleur.tp4weblafleur.llm;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.io.Serializable;

@Dependent
public class LlmClientPourClaude implements Serializable {

    @Inject
    private RagAssistantFactory ragAssistantFactory;

    private String systemRole;

    private Assistant assistant;

    private ChatMemory chatMemory;

    @PostConstruct
    public void init() {
        String claudeKey = System.getenv("CLAUDE_KEY");
        StreamingChatModel model = AnthropicStreamingChatModel.builder()
                .apiKey(claudeKey)
                .modelName("claude-sonnet-4-6")
                .logRequests(true)
                .logResponses(true)
                .temperature(0.3)
                .build();

        this.chatMemory = MessageWindowChatMemory.withMaxMessages(20);
        this.assistant = ragAssistantFactory.build(model, chatMemory);
    }

    public void setSystemRole(String systemRole) {
        this.systemRole = systemRole;
        this.chatMemory.clear(); // vider la mémoire avant de changer de rôle
        this.chatMemory.add(SystemMessage.from(this.systemRole));
    }
    public TokenStream envoyerRequete(String question){
        return assistant.chat(question);
    }

    public void addReponse(AiMessage message) {
        this.chatMemory.add(message);
    }

}
