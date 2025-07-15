package com.intrafind.llm.cli;

import com.intrafind.llm.config.LLMClientFactory;
import com.intrafind.llm.config.LLMConfig;
import com.intrafind.llm.core.LLMClient;
import com.intrafind.llm.core.LLMProvider;
import com.intrafind.llm.core.LLMRequest;
import com.intrafind.llm.core.LLMResponse;
import com.intrafind.llm.exceptions.LLMException;

import java.util.Arrays;
import java.util.Scanner;

public class LLMTestCLI {
    
    private static final String USAGE = "Usage: java -jar j2xlm.jar <provider> <api_key> [model]\n\n" +
        "Providers:\n" +
        "- openai       : OpenAI GPT models\n" +
        "- anthropic    : Anthropic Claude models\n" +
        "- gemini       : Google Gemini models\n" +
        "- mistral      : Mistral AI models\n\n" +
        "Examples:\n" +
        "java -jar j2xlm.jar openai sk-your-key gpt-4\n" +
        "java -jar j2xlm.jar anthropic your-key claude-3-sonnet-20240229\n" +
        "java -jar j2xlm.jar gemini your-key gemini-pro\n" +
        "java -jar j2xlm.jar mistral your-key mistral-medium";
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println(USAGE);
            System.exit(1);
        }
        
        String providerName = args[0].toLowerCase();
        String apiKey = args[1];
        String model = args.length > 2 ? args[2] : null;
        
        LLMProvider provider;
        try {
            provider = parseProvider(providerName);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.out.println(USAGE);
            System.exit(1);
            return;
        }
        
        LLMConfig config = new LLMConfig(apiKey);
        
        LLMClient client = LLMClientFactory.create(provider, config);
        try {
            System.out.println("=== LLM Test CLI ===");
            System.out.println("Provider: " + provider.getDisplayName());
            System.out.println("Model: " + (model != null ? model : "default"));
            System.out.println("Type 'quit' to exit, 'help' for commands");
            System.out.println();
            
            if (!client.isHealthy()) {
                System.err.println("Warning: Client health check failed");
            }
            
            runInteractiveMode(client, model);
            
        } catch (Exception e) {
            System.err.println("Error initializing client: " + e.getMessage());
            System.exit(1);
        } finally {
            client.close();
        }
    }
    
    private static LLMProvider parseProvider(String providerName) {
        switch (providerName) {
            case "openai":
                return LLMProvider.OPENAI;
            case "anthropic":
                return LLMProvider.ANTHROPIC;
            case "gemini":
                return LLMProvider.GEMINI;
            case "mistral":
                return LLMProvider.MISTRAL;
            default:
                throw new IllegalArgumentException("Unknown provider: " + providerName + 
                    ". Supported providers: openai, anthropic, gemini, mistral");
        }
    }
    
    private static void runInteractiveMode(LLMClient client, String modelToUse) {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            
            if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }
            
            if (input.equalsIgnoreCase("help")) {
                printHelp();
                continue;
            }
            
            if (input.equalsIgnoreCase("health")) {
                System.out.println("Client healthy: " + client.isHealthy());
                continue;
            }
            
            if (input.isEmpty()) {
                continue;
            }
            
            try {
                LLMRequest request = new LLMRequest(input);
                if (modelToUse != null) {
                    request.setModel(modelToUse);
                }
                
                System.out.println("Sending request...");
                long startTime = System.currentTimeMillis();
                
                LLMResponse response = client.generate(request);
                
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                
                System.out.println("\n--- Response ---");
                System.out.println(response.getContent());
                System.out.println("\n--- Metadata ---");
                System.out.println("Model: " + response.getModel());
                System.out.println("Provider: " + response.getProvider().getDisplayName());
                System.out.println("Response time: " + duration + "ms");
                
                if (response.getMetadata() != null && !response.getMetadata().isEmpty()) {
                    System.out.println("Additional metadata:");
                    response.getMetadata().forEach((key, value) -> 
                        System.out.println("  " + key + ": " + value));
                }
                
                if (response.getToolCalls() != null && !response.getToolCalls().isEmpty()) {
                    System.out.println("Tool calls: " + response.getToolCalls().size());
                }
                
                System.out.println();
                
            } catch (LLMException e) {
                System.err.println("LLM Error: " + e.getMessage());
                if (e.getCause() != null) {
                    System.err.println("Cause: " + e.getCause().getMessage());
                }
            } catch (Exception e) {
                System.err.println("Unexpected error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        scanner.close();
    }
    
    private static void printHelp() {
        System.out.println("Available commands:\n" +
            "- <message>    : Send a message to the LLM\n" +
            "- help         : Show this help message\n" +
            "- health       : Check client health status\n" +
            "- quit/exit    : Exit the CLI\n\n" +
            "Simply type your message and press Enter to send it to the LLM.");
    }
}