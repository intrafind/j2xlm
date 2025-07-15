package com.intrafind.llm.exceptions;

public class RateLimitException extends LLMException {
    
    public RateLimitException(String message) {
        super(message);
    }
    
    public RateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}