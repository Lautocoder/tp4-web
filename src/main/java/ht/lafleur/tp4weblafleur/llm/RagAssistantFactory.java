package ht.lafleur.tp4weblafleur.llm;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class RagAssistantFactory {

    @Inject
    private StoreRegistry storeRegistry;

    @Inject
    private EmbeddingModel embeddingModel;

    public Assistant build(StreamingChatModel streamingModel, ChatMemory chatMemory){

        ContentRetriever retrieverIA = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(storeRegistry.get("ia"))
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.6)
                .build();

        ContentRetriever retrieverBigData = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(storeRegistry.get("bigdata"))
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.6)
                .build();


        String tavilyKey= System.getenv("TAVILY_KEY");
        String claudeKey = System.getenv("CLAUDE_KEY");

        WebSearchEngine webSearchEngine = TavilyWebSearchEngine.builder()
                .apiKey(tavilyKey)
                .build();

        // Création du ContentRetriever Web
        ContentRetriever webRetriever = WebSearchContentRetriever.builder()
                .webSearchEngine(webSearchEngine)
                .build();
        ChatModel modelClaude = AnthropicChatModel.builder()
                .apiKey(claudeKey)
                .modelName("claude-sonnet-4-6")
                .temperature(0.0)
                .logRequests(true)
                .logResponses(true)
                .build();


        PromptTemplate promptTemplate = PromptTemplate.from(
                "Tu es un routeur de requêtes. Voici les sources disponibles :\n" +
                        "1 : Document sur l'intelligence artificielle, le machine learning, le RAG, le fine-tuning et les LLMs.\n" +
                        "2 : Document sur Hadoop, Spark, MapReduce et les systèmes de fichiers distribués.\n\n" +
                        "Si la question porte sur une de ces sources, réponds avec le numéro correspondant (1 ou 2).\n" +
                        "Si la question ne porte sur aucune de ces sources, réponds uniquement par '0'.\n" +
                        "Réponds UNIQUEMENT avec un chiffre : 0, 1 ou 2. Rien d'autre.\n\n" +
                        "Question : {{question}}"
        );
        QueryRouter routage = new QueryRouter() {
            @Override
            public Collection<ContentRetriever> route(Query query) {
                String question = query.text();
                String prompt = promptTemplate.apply(Map.of("question", question)).text();
                String reponse = modelClaude.chat(prompt).trim();

                return switch (reponse) {
                    case "1" -> List.of(retrieverIA);
                    case "2" -> List.of(retrieverBigData);
                    default -> List.of(webRetriever);
                };
            }
        };
        CompressingQueryTransformer queryTransformer = new CompressingQueryTransformer(modelClaude);

        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryTransformer(queryTransformer)
                .queryRouter(routage)
                .build();


        return AiServices.builder(Assistant.class)
                .streamingChatModel(streamingModel)
                .chatMemory(chatMemory)
                .retrievalAugmentor(retrievalAugmentor)
                .build();

    }
}
