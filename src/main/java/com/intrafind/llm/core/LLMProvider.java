package com.intrafind.llm.core;

public enum LLMProvider {
    OPENAI("OpenAI"),
    ANTHROPIC("Anthropic"),
    GEMINI("Google Gemini"),
    MISTRAL("Mistral AI");
    
    private final String displayName;
    
    LLMProvider(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}