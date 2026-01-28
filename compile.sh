#!/bin/bash

# Compilation script for the Loup-Garou project

echo "Compiling the Loup-Garou project..."

# Create the bin directory if it doesn't exist
mkdir -p bin

# Find all Java files and compile them
find src -name "*.java" -type f > sources.txt

# Compile with Java (uses the system's default version)
if command -v javac &> /dev/null; then
    javac -d bin @sources.txt
    compile_status=$?
else
    echo "javac is not installed. JDK installation required:"
    echo "   sudo apt-get install openjdk-21-jdk"
    exit 1
fi

# Clean up
rm sources.txt

if [ $compile_status -eq 0 ]; then
    echo "Compilation successful!"
    echo ""
    echo "To launch the server:"
    echo "  java -cp bin infrastructure.server.GameServer"
    echo ""
    echo "To launch a client:"
    echo "  java -cp bin presentation.client.GameClient"
else
    echo "Compilation error"
    exit 1
fi
