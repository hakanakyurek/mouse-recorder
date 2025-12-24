# Mouse Recorder & Replayer

A Java application that records and replays mouse movements and clicks globally.
Because I couldn't find one that works and free for MacOS

## Requirements

- Java 11 or higher (Developed with Java 25)
- Maven

## Quick Start

```bash
mvn clean compile exec:java
```

## Tests

```bash
mvn test
```

## Keyboard Shortcuts

| Action | Shortcut 1 | Shortcut 2 |
|--------|-----------|-----------|
| Record | Alt+R | Ctrl+R |
| Play | Alt+P | Ctrl+P |
| Stop | Alt+S | Ctrl+S |
| Clear | Alt+C | Ctrl+D |

## Features

- Global mouse recording
- Infinite playback until stopped
- Live action list display
- Save/load recordings
- Keyboard shortcuts for important actions

## Usage

1. Click **Start Recording** or press **Ctrl+R**
2. Perform mouse actions anywhere on your screen
3. Click **Stop Recording** or press **Ctrl+R** again
4. Click **Play** or press **Ctrl+P** to replay
5. Use **Save** (Ctrl+S) to store recordings for later use

## Troubleshooting

**App doesn't record mouse events (macOS)**
- Grant Accessibility permissions (see macOS Setup above)
