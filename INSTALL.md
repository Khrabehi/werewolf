# Installation et Compilation

## 🔧 Prérequis

Le projet nécessite Java 17+ (JDK, pas seulement JRE).

### Vérifier votre installation Java

```bash
java -version    # Vérifie le runtime
javac -version   # Vérifie le compilateur
```

Si `javac` n'est pas trouvé, vous n'avez que le JRE. Il faut installer le JDK.

## 📦 Installation du JDK

### Ubuntu/Debian
```bash
# Installer OpenJDK 21
sudo apt-get update
sudo apt-get install openjdk-21-jdk

# Vérifier
javac -version
```

### Fedora/RHEL
```bash
sudo dnf install java-21-openjdk-devel
```

### macOS
```bash
brew install openjdk@21
```

### Windows
Télécharger depuis : https://adoptium.net/

---

## 🔨 Compilation

### Option 1 : Script automatique (recommandé)
```bash
./compile.sh
```

### Option 2 : Commande manuelle
```bash
# Créer le répertoire bin
mkdir -p bin

# Compiler tous les fichiers
find src -name "*.java" -type f -print | xargs javac -d bin -sourcepath src

# Ou avec un fichier temporaire
find src -name "*.java" > sources.txt
javac -d bin @sources.txt
rm sources.txt
```

### Option 3 : Avec Maven (futur)
```bash
mvn clean compile
```

---

## 🚀 Exécution

### Lancer le serveur
```bash
java -cp bin infrastructure.server.GameServer
```

### Lancer un client (dans un autre terminal)
```bash
java -cp bin presentation.client.GameClient
```

---

## 🐛 Dépannage

### Erreur : "javac: command not found"
**Cause** : JDK non installé (seulement JRE)  
**Solution** : Installer le JDK (voir ci-dessus)

### Erreur : "package X does not exist"
**Cause** : Ordre de compilation incorrect  
**Solution** : Utiliser `-sourcepath src` ou compiler tous les fichiers ensemble

### Erreur de version Java
**Cause** : Code Java 17+ utilisé, mais ancien JDK installé  
**Solution** : Installer Java 17 minimum

```bash
# Ubuntu
sudo apt-get install openjdk-21-jdk
sudo update-alternatives --config java
sudo update-alternatives --config javac
```

---

## 📁 Structure de Compilation

```
loup-garou/
├── src/                      # Code source
│   ├── domain/              # Couche domaine
│   ├── application/         # Couche application
│   ├── infrastructure/      # Couche infrastructure
│   └── presentation/        # Couche présentation
│
├── bin/                      # Fichiers .class compilés
│   ├── domain/
│   ├── application/
│   ├── infrastructure/
│   └── presentation/
│
├── compile.sh               # Script de compilation
└── ARCHITECTURE.md          # Documentation architecture
```

---

## 🎯 Commandes Rapides

```bash
# Tout nettoyer et recompiler
rm -rf bin/* && ./compile.sh

# Lancer serveur + 2 clients (3 terminaux)
# Terminal 1
java -cp bin infrastructure.server.GameServer

# Terminal 2
java -cp bin presentation.client.GameClient

# Terminal 3
java -cp bin presentation.client.GameClient
```

---

## 🧪 Test Rapide

Une fois le serveur et 4 clients lancés :

```
Client 1: PSEUDO Alice
Client 2: PSEUDO Bob
Client 3: PSEUDO Charlie
Client 4: PSEUDO Diana

Client 1 (admin): START

# Le jeu démarre, les rôles sont assignés
# Si vous êtes loup : KILL <pseudo>
```

---

## 📚 Documentation Complète

- [ARCHITECTURE.md](ARCHITECTURE.md) - Architecture et principes SOLID
- [MIGRATION_GUIDE.md](MIGRATION_GUIDE.md) - Comparaison ancien/nouveau code
- [README.md](README.md) - Vue d'ensemble du projet

---

## 💡 Prochaines Étapes

1. **Ajouter Maven/Gradle** pour gérer les dépendances
2. **Tests Unitaires** avec JUnit
3. **CI/CD** avec GitHub Actions
4. **Packaging** en JAR exécutable
