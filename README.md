# JavaGPT

JavaFX alapú, egyszerű chat alkalmazás, amely egy helyben futó LLM-et (Ollama) használ a válaszok generálásához. A projekt egy egyetemi beadandó, célja egy letisztult, asztali „chat kliens” megvalósítása.

## Fő funkciók
- Bejelentkező nézet (UI-ból indítható a chat).
- Chat felület üzenetbuborékokkal (felhasználó és asszisztens elkülönítése).
- Ollama integráció (alapértelmezett modell: `gemma3:1b`).

## Követelmények
- Java 20
- Maven
- Ollama futtatva lokálisan: `http://127.0.0.1:11434`

## Futtatás
1. Indítsd el az Ollama szervert a gépeden.
2. A projekt gyökerében futtasd:
```bash
mvn clean javafx:run
```
3. A megjelenő ablakban kattints a „Start” gombra, majd írd be az üzeneted.

## Technológiák
- JavaFX (UI)
- Maven
- Ollama4j (LLM kliens)

## Megjegyzések
- A chat indításakor az alkalmazás lekéri az alapértelmezett modellt, és küld egy kezdeti üzenetet.
