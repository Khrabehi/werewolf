#!/bin/bash

# Script de compilation du projet Loup-Garou

echo "🔨 Compilation du projet Loup-Garou..."

# Créer le répertoire bin s'il n'existe pas
mkdir -p bin

# Trouver tous les fichiers Java et les compiler
find src -name "*.java" -type f > sources.txt

# Compiler avec Java (utilise la version par défaut du système)
if command -v javac &> /dev/null; then
    javac -d bin @sources.txt
    compile_status=$?
else
    echo "❌ javac n'est pas installé. Installation du JDK nécessaire:"
    echo "   sudo apt-get install openjdk-21-jdk"
    exit 1
fi

# Nettoyer
rm sources.txt

if [ $compile_status -eq 0 ]; then
    echo "✅ Compilation réussie !"
    echo ""
    echo "Pour lancer le serveur:"
    echo "  java -cp bin infrastructure.server.GameServer"
    echo ""
    echo "Pour lancer un client:"
    echo "  java -cp bin presentation.client.GameClient"
else
    echo "❌ Erreur de compilation"
    exit 1
fi
