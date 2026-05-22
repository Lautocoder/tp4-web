package ht.lafleur.tp4weblafleur.llm;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class EmbeddingStoreProducer {
    @Produces
    @ApplicationScoped
    public EmbeddingModel produceEmbeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }
}
