# ProjectForge DynamicLayout

> **Status: Early Draft** — API is unstable, breaking changes expected.

[![CI](https://github.com/MaurerAnton/projectforge-dynamiclayout/actions/workflows/build.yml/badge.svg)](https://github.com/MaurerAnton/projectforge-dynamiclayout/actions)
[![License](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![Playground](https://img.shields.io/badge/Playground-try%20it%20online-brightgreen)](https://maureranton.github.io/projectforge-dynamiclayout/playground/)

**Server-Driven UI Engine.** Kotlin DSL → JSON → React / Native.

```
UILayout + UIRow + UIInput + UIButton  →  JSON  →  <DynamicLayout />
          (backend)                         (React)
```

---

## Quick Start

### 1. Install Kotlin/Native (once)

```bash
cd projectforge-dynamiclayout/core
./build.sh           # downloads kotlinc-native 2.0.21 (~500MB), builds .klib
```

Everything else works offline — no Gradle, no JVM at runtime.

### 2. Build Native library

```bash
make native
# → build/dynamiclayout.klib    (for embedding in C/C++/Swift/Rust)
```

### 3. Build JVM .jar

```bash
make jvm
# → build/dynamiclayout-jvm.jar (for Java/Spring projects)
```

### 4. Run tests

```bash
make test
```

Output:
```
=== DynamicLayout Core Tests ===

[PASS] UIElementType count: 33
[PASS] Layout created: title='Test Page
[PASS] Row/Col layout built
[PASS] Button added
[PASS] JSON serialized (1234 chars)
[PASS] JSON contains expected types
[PASS] DataType: STRING
[PASS] Alert JSON: ...
[PASS] JSON deserialization roundtrip OK
[PASS] UISelect generic JSON: ...
[PASS] ResponseAction JSON: ...

=== All 11 tests PASSED ===
```

### 5. Standalone executable (no JVM)

```bash
make app
# builds and runs a standalone Native binary
```

---

## Usage

### Kotlin (backend)

```kotlin
import org.dynamiclayout.core.*
import org.dynamiclayout.core.ui.*
import org.dynamiclayout.core.util.*

val layout = UILayout("'Registration")
layout.add(
    UIFieldset(title = "'User Info")
        .add(UIInput("firstName", label = "'First Name"))
        .add(UIInput("email", label = "'Email"))
)
layout.addAction(UIButton.createDefaultButton(title = "'Submit"))
layout.addAction(UIButton.createCancelButton())

val json = DynamicLayoutJson.encode(layout)
```

### TypeScript / React (frontend)

```jsx
import { DynamicLayout } from '@dynamiclayout/react';

function App() {
    const [ui, setUi] = useState(null);
    const [data, setData] = useState({});

    useEffect(() => {
        fetch('/api/page').then(r => r.json()).then(d => setUi(d.ui));
    }, []);

    return <DynamicLayout ui={ui} data={data} setData={setData} />;
}
```

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  backend (Kotlin Multiplatform)                             │
│  UILayout + UIRow + UICol + UIInput + UIButton + ...        │
│         │                                                   │
│         ▼                                                   │
│  DynamicLayoutJson.encode(layout)                           │
│         │                                                   │
│         ▼                                                   │
│  JSON: { "title": "...", "layout": [...], "actions": [...] }│
└─────────────────────────────────────────────────────────────┘
         │  HTTP GET /api/page
         ▼
┌─────────────────────────────────────────────────────────────┐
│  frontend (React)                                           │
│  <DynamicLayout ui={json} data={...} />                     │
│         │                                                   │
│         ▼                                                   │
│  DynamicRenderer → components[type](props)                  │
│         │                                                   │
│         ▼                                                   │
│  <DynamicFieldset> → <DynamicInput> → <DynamicButton>       │
└─────────────────────────────────────────────────────────────┘
```

---

## Modules

| Module | Platform | Description | Files | Lines |
|--------|-----------|----------|:-----:|:-----:|
| **core** | JVM + Native + JS | UI DSL (UIRow, UIInput, UIButton...) | 52 .kt | ~1000 |
| react | JS | DynamicRenderer | 21 .jsx | ~650 |

---

## Commands

```bash
cd projectforge-dynamiclayout/core

./build.sh native    # → build/dynamiclayout.klib
./build.sh jvm       # → build/dynamiclayout-jvm.jar
./build.sh app       # → build/app.kexe + run
./build.sh clean     # → rm -rf build

make test            # compile and run 11 tests
make native          # same as build.sh native
make jvm             # same as build.sh jvm
make app             # same as build.sh app
```

---

## Requirements

| Tool | Purpose | When |
|-----------|-------|-------|
| `curl`, `tar` | Download kotlinc-native | Once |
| Java 17+ | Run kotlinc-native | At build |
| Linux x86_64 **or** ARM64 | Target platform | At build |
| `make` | Convenience | Optional |
| Node.js 18+ | Build React package | Optional |

Supported architectures:
- `x86_64` — Intel/AMD (primary)
- `aarch64` / `arm64` — Apple Silicon, Raspberry Pi, AWS Graviton

---

## Examples

Example JSON layouts that demonstrate what DynamicLayout can render:

| File | Description | Components used |
|------|-------------|-----------------|
| [examples/about-layout.json](examples/about-layout.json) | Static info page | FIELDSET, LABEL |
| [examples/feedback-form.json](examples/feedback-form.json) | Contact form with two-column layout | ROW, COL, INPUT, TEXTAREA, BUTTON |
| [examples/registration-form.json](examples/registration-form.json) | Complex form with select, checkbox, collapsible fieldset | ROW, COL, FIELDSET, INPUT, SELECT, CHECKBOX, TEXTAREA, BUTTON |

To preview any example:
1. Copy the JSON into a DynamicLayout React component
2. Or serve it via the demo server (`dynamiclayout-demo/`)

```jsx
import { DynamicLayout } from '@dynamiclayout/react';

const json = { /* paste any example JSON here */ };
<DynamicLayout ui={json.ui} data={{}} />;
```

---

## Gradle comparison

| Aspect | Gradle | Makefile + build.sh |
|--------|--------|---------------------|
| Dependencies | Maven Central (network) | One curl on first run |
| Speed | ~2 min (daemon) | ~10 sec (no daemon) |
| Config | 200+ lines Gradle DSL | 30 lines Makefile |
| Flexibility | Plugins (Spring, JPA) | kotlinc-native only |
| JVM needed | Yes (Gradle + compiler) | Yes (compiler only) |

---

## Further reading

| File | About |
|------|-------|
| [GETTING_STARTED.md](GETTING_STARTED.md) | Full tutorial from scratch |
| [playground/](playground/) | Interactive playground — paste JSON, see live preview |
| [core/tests/src/Tests.kt](core/tests/src/Tests.kt) | 11 tests as a starting point |
| [dynamiclayout-schema.json](dynamiclayout-schema.json) | JSON Schema |
| [docs/](docs/) | Full engine documentation |
| [ROADMAP](docs/projectforge-sdui-engine-docs/ROADMAP.md) | Planned features and priorities |
