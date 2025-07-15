package com.intrafind.llm.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intrafind.llm.exceptions.LLMException;

import java.io.IOException;

public class JsonParser {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static <T> T parse(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new LLMException("Failed to parse JSON", e);
        }
    }
    
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new LLMException("Failed to serialize to JSON", e);
        }
    }
}