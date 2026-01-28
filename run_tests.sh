#!/bin/bash

echo "Running Loup-Garou tests"
echo "=================================="
echo ""

# Check if Maven is available
if command -v mvn &> /dev/null; then
    echo "Using Maven..."
    mvn clean test
    exit $?
fi

# Otherwise, manual compilation with JUnit
echo "Manual compilation of code and tests..."

# Compile source code
mkdir -p bin
javac -d bin -sourcepath src $(find src -name "*.java")

if [ $? -ne 0 ]; then
    echo "Source code compilation error"
    exit 1
fi

# Download JUnit if necessary
if [ ! -f "lib/junit-platform-console-standalone-1.10.1.jar" ]; then
    echo "Downloading JUnit..."
    mkdir -p lib
    curl -L "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.1/junit-platform-console-standalone-1.10.1.jar" \
        -o lib/junit-platform-console-standalone-1.10.1.jar
fi

# Compile tests
echo "Compiling tests..."
javac -d bin -cp "bin:lib/junit-platform-console-standalone-1.10.1.jar" $(find test -name "*.java")

if [ $? -ne 0 ]; then
    echo "Test compilation error"
    exit 1
fi

echo ""
echo "Running tests..."
echo ""

# Exécuter tous les tests avec JUnit
java -jar lib/junit-platform-console-standalone-1.10.1.jar \
    --class-path bin \
    --scan-class-path

echo ""
echo "Tests completed!"
