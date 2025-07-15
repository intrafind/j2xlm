#!/bin/bash

# J2XLM Test Runner
echo "=== J2XLM Test Runner ==="

# Check if integration tests should be enabled
if [ "$1" = "integration" ]; then
    export INTEGRATION_TESTS=true
    echo "Integration tests enabled"
else
    echo "Running unit tests only (use 'integration' argument to enable integration tests)"
fi

# Check for API keys
echo "Checking API keys..."

if [ -n "$OPENAI_API_KEY" ]; then
    echo "✓ OpenAI API key found"
else
    echo "✗ OpenAI API key not found (set OPENAI_API_KEY environment variable)"
fi

if [ -n "$ANTHROPIC_API_KEY" ]; then
    echo "✓ Anthropic API key found"
else
    echo "✗ Anthropic API key not found (set ANTHROPIC_API_KEY environment variable)"
fi

if [ -n "$GEMINI_API_KEY" ]; then
    echo "✓ Gemini API key found"
else
    echo "✗ Gemini API key not found (set GEMINI_API_KEY environment variable)"
fi

if [ -n "$MISTRAL_API_KEY" ]; then
    echo "✓ Mistral API key found"
else
    echo "✗ Mistral API key not found (set MISTRAL_API_KEY environment variable)"
fi

echo ""

# Build the project
echo "Building project..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo "Build successful!"

# Run unit tests
echo "Running unit tests..."
mvn test -Dtest="!**/*Integration*" -q

if [ $? -ne 0 ]; then
    echo "Unit tests failed!"
    exit 1
fi

echo "Unit tests passed!"

# Run integration tests if enabled
if [ "$INTEGRATION_TESTS" = "true" ]; then
    echo "Running integration tests..."
    mvn test -Dtest="**/*Integration*,**/TestSuite,**/ToolCallingTest,**/ErrorHandlingTest,**/PerformanceTest" -q
    
    if [ $? -ne 0 ]; then
        echo "Integration tests failed!"
        exit 1
    fi
    
    echo "Integration tests passed!"
fi

echo "All tests completed successfully!"