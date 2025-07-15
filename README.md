# J2XLM - Java Library for LLM APIs

J2XLM is a Java library that provides a unified abstraction layer for interacting with multiple Large Language Model (LLM) providers. It simplifies the process of integrating different LLM services into your Java applications by providing a consistent interface across providers.

## Features

- **Multi-Provider Support**: OpenAI, Anthropic, Google Gemini, and Mistral AI
- **Unified API**: Consistent interface regardless of the underlying provider
- **Tool Calling**: Support for function calling and tool use
- **Flexible Configuration**: Easy setup with API keys and custom parameters
- **Error Handling**: Comprehensive exception handling for different error types
- **Health Checks**: Built-in health monitoring for LLM clients
- **Interactive CLI**: Command-line tool for manual testing and experimentation

## Supported Providers

| Provider | Status | Models |
|----------|--------|---------|
| OpenAI | ✅ | GPT-4, GPT-3.5, etc. |
| Anthropic | ✅ | Claude 3 Sonnet, Claude 3 Haiku, etc. |
| Google Gemini | ✅ | Gemini Pro, Gemini Pro Vision |
| Mistral AI | ✅ | Mistral Medium, Mistral Small, etc. |

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- API keys for the LLM providers you want to use

### Installation

Clone the repository and build the project:

```bash
git clone <repository-url>
cd j2xlm
mvn clean install
```

### Basic Usage

```java
import com.intrafind.llm.config.LLMConfig;
import com.intrafind.llm.config.LLMClientFactory;
import com.intrafind.llm.core.LLMClient;
import com.intrafind.llm.core.LLMProvider;
import com.intrafind.llm.core.LLMRequest;
import com.intrafind.llm.core.LLMResponse;

// Create configuration
LLMConfig config = new LLMConfig("your-api-key");

// Create client
LLMClient client = LLMClientFactory.create(LLMProvider.OPENAI, config);

// Make request
LLMRequest request = new LLMRequest("Explain quantum computing in simple terms")
    .withParameter("temperature", 0.7)
    .withParameter("max_tokens", 150);

LLMResponse response = client.generate(request);

// Process response
System.out.println("Provider: " + response.getProvider().getDisplayName());
System.out.println("Model: " + response.getModel());
System.out.println("Response: " + response.getContent());

// Clean up
client.close();
```

## Command Line Interface

J2XLM includes an interactive CLI tool for manual testing and experimentation with different LLM providers.

### Running the CLI

#### Option 1: Using Maven (Development)

```bash
mvn exec:java -Dexec.args="<provider> <api_key> [model]"
```

Examples:
```bash
mvn exec:java -Dexec.args="openai sk-your-key gpt-4"
mvn exec:java -Dexec.args="anthropic your-key claude-3-sonnet-20240229"
mvn exec:java -Dexec.args="gemini your-key gemini-pro"
mvn exec:java -Dexec.args="mistral your-key mistral-medium"
```

#### Option 2: Using JAR file

First, build the executable JAR:
```bash
mvn clean package
```

Then run the CLI:
```bash
java -jar target/j2xlm-cli.jar <provider> <api_key> [model]
```

Examples:
```bash
java -jar target/j2xlm-cli.jar openai sk-your-key gpt-4
java -jar target/j2xlm-cli.jar anthropic your-key claude-3-sonnet-20240229
java -jar target/j2xlm-cli.jar gemini your-key gemini-pro
java -jar target/j2xlm-cli.jar mistral your-key mistral-medium
```

### CLI Commands

Once the CLI is running, you can use the following commands:

- **Type any message**: Send a message to the LLM
- **`help`**: Show available commands
- **`health`**: Check client health status
- **`quit`** or **`exit`**: Exit the CLI

### CLI Features

- **Real-time interaction**: Send messages and receive responses instantly
- **Response metadata**: View model, provider, response time, and usage information
- **Error handling**: Graceful handling of API errors and rate limits
- **Health monitoring**: Check connection status to LLM providers

## Testing

### Running All Tests

```bash
mvn test
```

### Running Specific Test Categories

**Unit Tests:**
```bash
mvn test -Dtest="*Test"
```

**Integration Tests:**
```bash
mvn test -Dtest="*IntegrationTest"
```

**Performance Tests:**
```bash
mvn test -Dtest="PerformanceTest"
```

### Test Configuration

Most integration tests are skipped by default unless API keys are provided. To run integration tests:

1. Set environment variables for API keys:
   ```bash
   export OPENAI_API_KEY="your-openai-key"
   export ANTHROPIC_API_KEY="your-anthropic-key"
   export GEMINI_API_KEY="your-gemini-key"
   export MISTRAL_API_KEY="your-mistral-key"
   ```

2. Run tests with integration profile:
   ```bash
   mvn test -Dtest="*IntegrationTest"
   ```

### Test Structure

- **Unit Tests**: `src/test/java/com/intrafind/llm/core/`
- **Integration Tests**: `src/test/java/com/intrafind/llm/integration/`
- **Performance Tests**: `src/test/java/com/intrafind/llm/performance/`
- **Error Handling Tests**: `src/test/java/com/intrafind/llm/error/`
- **Tool Calling Tests**: `src/test/java/com/intrafind/llm/tools/`

## Development

### Project Structure

```
src/
├── main/java/com/intrafind/llm/
│   ├── cli/                    # Command line interface
│   ├── config/                 # Configuration classes
│   ├── core/                   # Core interfaces and classes
│   ├── examples/               # Usage examples
│   ├── exceptions/             # Custom exceptions
│   ├── providers/              # Provider-specific implementations
│   └── utils/                  # Utility classes
└── test/java/com/intrafind/llm/
    ├── config/                 # Configuration tests
    ├── core/                   # Core functionality tests
    ├── error/                  # Error handling tests
    ├── integration/            # Integration tests
    ├── performance/            # Performance tests
    └── tools/                  # Tool calling tests
```

### Building the Project

**Clean and compile:**
```bash
mvn clean compile
```

**Run tests:**
```bash
mvn test
```

**Package (includes tests):**
```bash
mvn package
```

**Install to local repository:**
```bash
mvn install
```

### Adding New Providers

To add support for a new LLM provider:

1. Add the provider to the `LLMProvider` enum
2. Create a new implementation in `src/main/java/com/intrafind/llm/providers/`
3. Implement the `LLMClient` interface
4. Update the `LLMClientFactory` to handle the new provider
5. Add integration tests in `src/test/java/com/intrafind/llm/integration/`

## Configuration

### API Keys

API keys can be provided through:
- Constructor parameter: `new LLMConfig("your-api-key")`
- Environment variables (provider-specific)
- Configuration files (custom implementation)

### Request Parameters

Common parameters that can be set on requests:
- `temperature`: Controls randomness (0.0 to 1.0)
- `max_tokens`: Maximum response length
- `top_p`: Nucleus sampling parameter
- `frequency_penalty`: Penalize frequent tokens
- `presence_penalty`: Penalize new tokens

### Provider-Specific Configuration

Each provider may support additional configuration options:
- **OpenAI**: Organization ID, custom base URL
- **Anthropic**: Custom headers, version specification
- **Gemini**: Safety settings, generation config
- **Mistral**: Custom endpoint, model variations

## Error Handling

The library includes comprehensive error handling:

- **`LLMException`**: Base exception for all LLM-related errors
- **`AuthenticationException`**: Authentication failures
- **`RateLimitException`**: Rate limiting errors
- **Provider-specific exceptions**: Custom error types for each provider

## Performance Considerations

- **Connection pooling**: HTTP clients use connection pooling
- **Timeout configuration**: Configurable request timeouts
- **Rate limiting**: Built-in rate limit detection and handling
- **Health checks**: Monitor provider availability

## Contributing

1. Fork the repository
2. Create a feature branch
3. Write tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and feature requests, please create an issue in the GitHub repository.