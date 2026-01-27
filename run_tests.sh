#!/bin/bash

echo "🧪 Exécution des tests Loup-Garou"
echo "=================================="
echo ""

# Vérifier si Maven est disponible
if command -v mvn &> /dev/null; then
    echo "📦 Utilisation de Maven..."
    mvn clean test
    exit $?
fi

# Sinon, compilation manuelle avec JUnit
echo "📦 Compilation manuelle du code et des tests..."

# Compiler le code source
mkdir -p bin
javac -d bin -sourcepath src $(find src -name "*.java")

if [ $? -ne 0 ]; then
    echo "❌ Erreur de compilation du code source"
    exit 1
fi

# Télécharger JUnit si nécessaire
if [ ! -f "lib/junit-platform-console-standalone-1.10.1.jar" ]; then
    echo "📥 Téléchargement de JUnit..."
    mkdir -p lib
    curl -L "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.1/junit-platform-console-standalone-1.10.1.jar" \
        -o lib/junit-platform-console-standalone-1.10.1.jar
fi

# Compiler les tests
echo "📦 Compilation des tests..."
javac -d bin -cp "bin:lib/junit-platform-console-standalone-1.10.1.jar" $(find test -name "*.java")

if [ $? -ne 0 ]; then
    echo "❌ Erreur de compilation des tests"
    exit 1
fi

echo ""
echo "🚀 Exécution des tests..."
echo ""

# Exécuter tous les tests avec JUnit
java -jar lib/junit-platform-console-standalone-1.10.1.jar \
    --class-path bin \
    --scan-class-path

echo ""
echo "✅ Tests terminés !"
