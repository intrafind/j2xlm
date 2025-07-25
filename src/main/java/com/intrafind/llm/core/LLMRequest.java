package com.intrafind.llm.core;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LLMRequest {
    private String prompt;
    private Map<String, Object> parameters;
    private String model;
    private List<String> stopSequences;
    private List<Tool> tools;
    private ImageDTO image;

    public LLMRequest(String prompt) {
        this.prompt = prompt;
        this.parameters = new HashMap<>();
    }
    
    public String getPrompt() {
        return prompt;
    }
    
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public List<String> getStopSequences() {
        return stopSequences;
    }
    
    public void setStopSequences(List<String> stopSequences) {
        this.stopSequences = stopSequences;
    }
    
    public LLMRequest withParameter(String key, Object value) {
        this.parameters.put(key, value);
        return this;
    }
    
    public LLMRequest withModel(String model) {
        this.model = model;
        return this;
    }
    
    public LLMRequest withStopSequences(List<String> stopSequences) {
        this.stopSequences = stopSequences;
        return this;
    }
    
    public List<Tool> getTools() {
        return tools;
    }
    
    public void setTools(List<Tool> tools) {
        this.tools = tools;
    }
    
    public LLMRequest withTools(List<Tool> tools) {
        this.tools = tools;
        return this;
    }

    public ImageDTO getImage() {
        return image;
    }

    public void setImage(String mediaType, byte[] data) {
        this.image = new ImageDTO(mediaType, data);
    }

    public LLMRequest withImage(String mediaType, byte[] data) {
        this.image = new ImageDTO(mediaType, data);
        return this;
    }

    public record ImageDTO(String mediaType, byte[] data) {

        public String asDataUrl() {
            return "data:" + mediaType + ";base64," + Base64.getEncoder().encodeToString(data);
        }
    }
}