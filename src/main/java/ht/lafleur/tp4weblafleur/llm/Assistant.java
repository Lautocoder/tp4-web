package ht.lafleur.tp4weblafleur.llm;

import dev.langchain4j.service.TokenStream;

public interface Assistant {
    TokenStream chat(String question);
}
