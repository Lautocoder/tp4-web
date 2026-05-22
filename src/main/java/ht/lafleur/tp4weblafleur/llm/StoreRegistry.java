package ht.lafleur.tp4weblafleur.llm;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class StoreRegistry {

    private final Map<String, EmbeddingStore<TextSegment>> stores = new ConcurrentHashMap<>();
    private final Map<String, String> descriptions = new ConcurrentHashMap<>();

    public EmbeddingStore<TextSegment> getOrCreate(String nomStore, String description) {
        descriptions.put(nomStore, description);
        return stores.computeIfAbsent(nomStore, k -> new InMemoryEmbeddingStore<>());
    }
    public String getDescription(String nomStore) {
        return descriptions.getOrDefault(nomStore, nomStore);
    }

    public EmbeddingStore<TextSegment> get(String nomStore) {
        EmbeddingStore<TextSegment> store = stores.get(nomStore);
        if (store == null) {
            throw new IllegalArgumentException("Store introuvable : " + nomStore);
        }
        return store;
    }

    public boolean exists(String nomStore) {
        return stores.containsKey(nomStore);
    }

    public Set<String> getStoreNames() {
        return Collections.unmodifiableSet(stores.keySet());
    }
}
