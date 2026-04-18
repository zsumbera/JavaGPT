# JavaGPT

JavaFX alapú asztali chat alkalmazás, amely egy helyben futó LLM-et (Ollama) használ a válaszok generálásához. A kliens és az AI-agent közötti kommunikáció egy dedikált Java szerveren keresztül zajlik, Socket kapcsolaton, szálkezeléssel. A projekt egy egyetemi beadandó.

---

## Fő funkciók

- Regisztráció és bejelentkezés (felhasználók JSON fájlban tárolva)
- Chat felület üzenetbuborékokkal (felhasználó és asszisztens elkülönítve)
- Több chat session kezelése session ID alapján
- Valós idejű, streamelt AI válaszok (token-by-token megjelenítés)
- Kezdő üzenet az agenttől az ablak megnyitásakor
- Ollama integráció (alapértelmezett modell: `gemma3:1b`)

---

## Architektúra

Az alkalmazás három rétegből áll:

```
┌─────────────────┐        Socket (JSON)        ┌──────────────────────┐        HTTP        ┌─────────────────┐
│  Kliens (JavaFX)│  ◄──────────────────────►   │  Server (Java)       │  ◄───────────────► │  Ollama (lokál) │
│                 │                             │                      │                    │  gemma3:1b      │
│  LoginView      │                             │  ServerSocket        │                    │  :11434         │
│  RegisterView   │                             │  ThreadPoolExecutor  │                    └─────────────────┘
│  ChatController │                             │  ClientHandler       │
│  Client (szál)  │                             │  AuthService         │
└─────────────────┘                             │  Ollama (wrapper)    │
                                                └──────────────────────┘
```

### Kommunikációs protokoll

A kliens és a szerver között minden üzenet egyetlen JSON sor (`Message` objektum):

| `type`             | Irány          | Leírás                              |
|--------------------|----------------|-------------------------------------|
| `LOGIN`            | kliens → szerver | Bejelentkezési kísérlet            |
| `LOGIN_OK`         | szerver → kliens | Sikeres bejelentkezés              |
| `REGISTER`         | kliens → szerver | Regisztrációs kísérlet             |
| `REGISTER_OK`      | szerver → kliens | Sikeres regisztráció               |
| `CHAT`             | kliens → szerver | Felhasználói üzenet                |
| `CHAT_CHUNK`       | szerver → kliens | AI válasz egy tokenje (streaming)  |
| `CHAT_END`         | szerver → kliens | Streaming vége                     |
| `HISTORY_REQUEST`  | kliens → szerver | Korábbi üzenetek lekérése          |
| `HISTORY_RESPONSE` | szerver → kliens | Egy korábbi üzenet                 |
| `ERROR`            | szerver → kliens | Hibaüzenet                         |

### Szálkezelés

- A szerver `ThreadPoolExecutor`-t használ (max. 20 szál), minden klienshez egy `ClientHandler` fut saját szálban.
- A kliens oldalon a `Client` osztály egy daemon szálként fut, és kizárólag ő olvas a socketről.
- A JavaFX UI frissítések mindig `Platform.runLater()` hívással történnek.

---

## Projektstruktúra

```
beadando/
├── HelloApplication.java

├── client/
│   ├── Client.java
│   └── controller/
│       ├── ChatController.java   ← csak controller logika, nincs inner model
│       ├── ChatMessage.java      ← Role enum itt van, appendText() itt van
│       ├── User.java       

├── protocol/
│   ├── Message.java
│   └── MessageType.java

├── server/
│   ├── Server.java
│   ├── ClientHandler.java
│   ├── AuthService.java          ← User importja: beadando.model.User
│   └── ollama/
│       └── Ollama.java
```

---

## Követelmények

- Java 20
- Maven
- Ollama futtatva lokálisan: `http://127.0.0.1:11434`
- `gemma3:1b` modell (az alkalmazás induláskor automatikusan letölti, ha még nincs meg)

---

## Futtatás

### 1. Ollama indítása

```bash
ollama serve
```

Ellenőrzés:
```bash
curl http://127.0.0.1:11434
# → "Ollama is running"
```

### 2. Szerver indítása

```bash
mvn exec:java -Dexec.mainClass="hu.ppke.itk.beadando.server.Server"
```

### 3. Kliens indítása

```bash
mvn clean javafx:run
```

---

## Függőségek (`pom.xml`)

```xml
<!-- JavaFX -->
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>20.0.1</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>20.0.1</version>
</dependency>

<!-- Ollama4j -->
<dependency>
    <groupId>io.github.ollama4j</groupId>
    <artifactId>ollama4j</artifactId>
    <version>1.1.6</version>
</dependency>

<!-- Gson -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

---

## Megjegyzések

- A jelszavak SHA-256 hash-elve tárolódnak a `users.json` fájlban.
- A chat history session ID alapján van kezelve a szerveren (`felhasználónév:sessionId` kulccsal), így ugyanaz a felhasználó több ablakban is tud chatelni.
- Az Ollama `pullModel()` hívás csak egyszer fut le a szerver indulásakor.
- A `users.json` a szerver munkakönyvtárában jön létre automatikusan az első regisztrációkor.
