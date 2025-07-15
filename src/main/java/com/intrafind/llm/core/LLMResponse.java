package com.intrafind.llm.core;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LLMResponse {
    private String content;
    private String model;
    private LLMProvider provider;
    private Map<String, Object> metadata;
    private Optional<String> functionCall;
    private List<ToolCall> toolCalls;
    
    public LLMResponse(String content, String model, LLMProvider provider) {
        this.content = content;
        this.model = model;
        this.provider = provider;
        this.functionCall = Optional.empty();
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public LLMProvider getProvider() {
        return provider;
    }
    
    public void setProvider(LLMProvider provider) {
        this.provider = provider;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public Optional<String> getFunctionCall() {
        return functionCall;
    }
    
    public void setFunctionCall(Optional<String> functionCall) {
        this.functionCall = functionCall;
    }
    
    public List<ToolCall> getToolCalls() {
        return toolCalls;
    }
    
    public void setToolCalls(List<ToolCall> toolCalls) {
        this.toolCalls = toolCalls;
    }
}