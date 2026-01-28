# Installation and Compilation

## Prerequisites

The project requires Java 17+ (JDK, not just JRE).

### Check your Java installation

```bash
java -version    # Check the runtime
javac -version   # Check the compiler
```

If `javac` is not found, you only have the JRE. You need to install the JDK.

## JDK Installation

### Ubuntu/Debian
```bash
# Install OpenJDK 21
sudo apt-get update
sudo apt-get install openjdk-21-jdk

# Verify
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
Download from: https://adoptium.net/

---

## Compilation

### Option 1: Automatic script (recommended)
```bash
./compile.sh
```

### Option 2: Manual command
```bash
# Create the bin directory
mkdir -p bin

# Compile all files
find src -name "*.java" -type f -print | xargs javac -d bin -sourcepath src

# Or with a temporary file
find src -name "*.java" > sources.txt
javac -d bin @sources.txt
rm sources.txt
```

---

## Execution

### Launch the server
```bash
java -cp bin infrastructure.server.GameServer
```

### Launch a client (in another terminal)
```bash
java -cp bin presentation.client.GameClient
```

---

## Compilation Structure

```
loup-garou/
├── src/                      # Source code
│   ├── domain/              # Domain layer
│   ├── application/         # Application layer
│   ├── infrastructure/      # Infrastructure layer
│   └── presentation/        # Presentation layer
│
├── bin/                      # Compiled .class files
│   ├── domain/
│   ├── application/
│   ├── infrastructure/
│   └── presentation/
│
├── compile.sh               # Compilation script
└── ARCHITECTURE.md          # Architecture documentation
```

---

## Complete Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md) - Architecture
- [README.md](README.md) - Project overview

---

