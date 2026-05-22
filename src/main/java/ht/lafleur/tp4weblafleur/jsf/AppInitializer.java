package ht.lafleur.tp4weblafleur.jsf;

import ht.lafleur.tp4weblafleur.llm.DocumentIngestionService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ejb.Singleton;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
@Startup
public class AppInitializer {
    @Inject
    private DocumentIngestionService ingestionService;

    @PostConstruct
    public void init() {
        configureLogger();
        ingestionService.ingestDocument("docs/rag.pdf", "ia",
                "Document sur l'intelligence artificielle, le machine learning, le RAG, le fine-tuning et les LLMs.");
        ingestionService.ingestDocument("docs/HadoopSparkMapReduce_1.pdf", "bigdata",
                "Document sur Hadoop, Spark, MapReduce et les systèmes de fichiers distribués.");
    }

    private void configureLogger() {
        Logger packageLogger = Logger.getLogger("dev.langchain4j");
        packageLogger.setLevel(Level.FINE);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINE);
        packageLogger.addHandler(handler);
    }
}
