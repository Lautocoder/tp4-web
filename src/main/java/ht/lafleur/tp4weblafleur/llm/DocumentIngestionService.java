package ht.lafleur.tp4weblafleur.llm;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class DocumentIngestionService {

    private static final int SEGMENT_SIZE = 512;
    private static final int SEGMENT_OVERLAP = 30;

    @Inject
    private EmbeddingModel embeddingModel;

    @Inject
    private StoreRegistry storeRegistry;

    // Méthode pour diviser le document en segments
    public List<TextSegment> splitDocument(Document document) {
        DocumentSplitter splitter = DocumentSplitters.recursive(SEGMENT_SIZE, SEGMENT_OVERLAP);
        return splitter.split(document);
    }

    // Méthode pour créer des embeddings à partir des segments et les stocker dans l'EmbeddingStore
    public void embed(List<TextSegment> textSegments, String storeName, String description) {
        EmbeddingStore<TextSegment> store = storeRegistry.getOrCreate(storeName, description);
        var embeddings = embeddingModel.embedAll(textSegments).content();
        store.addAll(embeddings, textSegments);
    }

    // Méthode principale pour ingérer un document : charger, splitter et embedder
    public void ingestDocument(String documentPath, String storeName, String description) {
        DocumentParser parser = new ApacheTikaDocumentParser();
        Document document = ClassPathDocumentLoader.loadDocument(documentPath, parser);
        List<TextSegment> segments = splitDocument(document);
        embed(segments, storeName,description);
    }

}
