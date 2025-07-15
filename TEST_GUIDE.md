# J2XLM Test Guide

This guide explains how to run the comprehensive test suite for the J2XLM library.

## Test Categories

### 1. Unit Tests
- **Location**: `src/test/java/com/intrafind/llm/config/`, `src/test/java/com/intrafind/llm/core/`
- **Purpose**: Test core functionality without external dependencies
- **Run command**: `mvn test -Dtest="!**/*Integration*"`

### 2. Integration Tests
- **Location**: `src/test/java/com/intrafind/llm/integration/`
- **Purpose**: Test actual LLM provider integrations
- **Requirements**: API keys for respective providers
- **Run command**: `mvn test -Dtest="**/*Integration*"`

### 3. Tool Calling Tests
- **Location**: `src/test/java/com/intrafind/llm/tools/`
- **Purpose**: Test function/tool calling capabilities
- **Requirements**: API keys (primarily OpenAI and Anthropic)

### 4. Error Handling Tests
- **Location**: `src/test/java/com/intrafind/llm/error/`
- **Purpose**: Test error conditions and exception handling
- **Requirements**: Some tests need API keys

### 5. Performance Tests
- **Location**: `src/test/java/com/intrafind/llm/performance/`
- **Purpose**: Test response times, concurrency, and resource usage
- **Requirements**: API keys for performance comparison

## Setup

### Environment Variables

Set the following environment variables with your API keys:

```bash
export OPENAI_API_KEY="your-openai-api-key"
export ANTHROPIC_API_KEY="your-anthropic-api-key"
export GEMINI_API_KEY="your-gemini-api-key"
export MISTRAL_API_KEY="your-mistral-api-key"
```

### Enable Integration Tests

```bash
export INTEGRATION_TESTS=true
```

## Running Tests

### Quick Test Runner

Use the provided test runner script:

```bash
# Run unit tests only
./test-runner.sh

# Run all tests (unit + integration)
./test-runner.sh integration
```

### Manual Test Execution

#### Unit Tests Only
```bash
mvn test -Dtest="!**/*Integration*"
```

#### Integration Tests Only
```bash
INTEGRATION_TESTS=true mvn test -Dtest="**/*Integration*"
```

#### Specific Provider Tests
```bash
# OpenAI only
INTEGRATION_TESTS=true mvn test -Dtest="OpenAIIntegrationTest"

# Anthropic only
INTEGRATION_TESTS=true mvn test -Dtest="AnthropicIntegrationTest"

# Gemini only
INTEGRATION_TESTS=true mvn test -Dtest="GeminiIntegrationTest"

# Mistral only
INTEGRATION_TESTS=true mvn test -Dtest="MistralIntegrationTest"
```

#### Tool Calling Tests
```bash
INTEGRATION_TESTS=true mvn test -Dtest="ToolCallingTest"
```

#### Error Handling Tests
```bash
INTEGRATION_TESTS=true mvn test -Dtest="ErrorHandlingTest"
```

#### Performance Tests
```bash
INTEGRATION_TESTS=true mvn test -Dtest="PerformanceTest"
```

#### Full Test Suite
```bash
INTEGRATION_TESTS=true mvn test -Dtest="TestSuite"
```

## Test Coverage

### Provider-Specific Tests

Each provider integration test covers:
- Basic text generation
- Model-specific functionality
- Parameter handling (temperature, max_tokens, etc.)
- Stop sequences
- Usage metadata
- Provider-specific features

### Tool Calling Tests

- Function definition and calling
- Parameter parsing
- Multiple tool handling
- Tool calling without tools defined
- Error handling in tool calls

### Error Handling Tests

- Invalid API keys
- Authentication failures
- Invalid models
- Invalid parameters
- Network timeouts
- Rate limiting
- Exception hierarchy

### Performance Tests

- Response time measurement
- Concurrent request handling
- Memory usage monitoring
- Provider performance comparison
- Client reuse efficiency

## Expected Results

### Unit Tests
- Should all pass without API keys
- Test core library functionality
- Validate request/response models

### Integration Tests
- Require valid API keys
- Test actual API communication
- Validate provider-specific behavior
- May have some failures due to model availability or rate limits

### Tool Calling Tests
- Primarily test OpenAI and Anthropic (best tool support)
- Test function definition and execution
- Validate parameter passing

### Error Tests
- Test various error conditions
- Validate exception types and messages
- Test recovery and cleanup

### Performance Tests
- Measure response times (typically < 30 seconds)
- Test concurrent execution
- Monitor memory usage
- Compare provider performance

## Troubleshooting

### Common Issues

1. **API Key Not Found**: Ensure environment variables are set correctly
2. **Rate Limiting**: Some tests may fail due to rate limits - run with delays
3. **Model Access**: Some models may not be available in your account
4. **Network Issues**: Integration tests depend on network connectivity

### Test Failures

- **Authentication Errors**: Check API key validity
- **Timeout Errors**: Increase timeout in test configuration
- **Rate Limit Errors**: Wait and retry
- **Model Not Found**: Use available models for your account

## Adding New Tests

### For New Providers
1. Create integration test class extending `BaseIntegrationTest`
2. Implement provider-specific methods
3. Add provider-specific test cases
4. Update test runner and documentation

### For New Features
1. Add unit tests for core functionality
2. Add integration tests for API behavior
3. Add error handling tests
4. Update test documentation

## Continuous Integration

For CI/CD pipelines:

```bash
# Run only unit tests (no API keys needed)
mvn test -Dtest="!**/*Integration*"

# Run integration tests if API keys are available
if [ -n "$OPENAI_API_KEY" ]; then
    INTEGRATION_TESTS=true mvn test -Dtest="**/*Integration*"
fi
```

## Test Reports

Maven generates test reports in `target/surefire-reports/`:
- `TEST-*.xml`: JUnit XML reports
- `*.txt`: Text reports with detailed output

For detailed HTML reports, use:
```bash
mvn surefire-report:report
```

## Performance Benchmarks

Expected performance (approximate):
- **OpenAI**: 1-5 seconds for simple queries
- **Anthropic**: 1-3 seconds for simple queries
- **Gemini**: 1-4 seconds for simple queries
- **Mistral**: 1-3 seconds for simple queries

Note: Performance varies by model, prompt complexity, and current API load.