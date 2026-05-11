# ProjectForge DynamicLayout

> **Status: Early Draft** — API is unstable, breaking changes expected.

[![CI](https://github.com/MaurerAnton/projectforge-dynamiclayout/actions/workflows/build.yml/badge.svg)](https://github.com/MaurerAnton/projectforge-dynamiclayout/actions)
[![License](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![Playground](https://img.shields.io/badge/Playground-try%20it%20online-brightgreen)](https://maureranton.github.io/projectforge-dynamiclayout-xx/playground/)
[![Mobile](https://img.shields.io/badge/Mobile-Flutter%20app-blue)](mobile_playground/)

**Server-Driven UI Engine.** Kotlin DSL → JSON → React / Native.

```
UILayout + UIRow + UIInput + UIButton  →  JSON  →  <DynamicLayout />
          (backend)                         (React)
```

The code, core architecture, and initial design of the DynamicLayout engine originate from the **[ProjectForge](https://www.projectforge.org/)** project by **[Micromata GmbH](https://www.micromata.com/)** (Copyright 2001–2026). The original engine was built by **Fin Reinhard** (React) and **Kai Reinhard** (Kotlin) starting in March 2019. This project continues developing the engine's strengths, with the goal of making DynamicLayout convenient and accessible to any programmer.

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

### 42 language SDKs

Beyond Kotlin, the PFDL JSON format has SDKs in **42 programming languages** — C, Go, Python, Rust, Java, C#, Swift, Zig, Fortran, COBOL, Solidity, and more. All produce identical JSON from a common builder API. See the repo root for `dynamiclayout.*` files.

### 12 client renderers

The same JSON renders in **React, Vue, Svelte, Angular, SolidJS, Web Components, Flutter, SwiftUI, Jetpack Compose, React Native, Qt/QML, and HTMX**. All use the same component-registry pattern (`type` → component).

---

## Modules

| Module | Platform | Description | Files | Lines |
|--------|-----------|----------|:-----:|:-----:|
| **core** | JVM + Native + JS | UI DSL (UIRow, UIInput, UIButton...) | 52 .kt | ~1000 |
| react | JS | DynamicRenderer | 21 .jsx | ~650 |
| 42 language SDKs | Any | Generate PFDL JSON from any language | repo root | ~300 each |
| 12 client renderers | Any | Render PFDL JSON in any framework | repo root | ~150 each |
| `playground/` | Web | Interactive playground — paste JSON, see live preview | 1 .html | ~1100 |
| `mobile_playground/` | Flutter | Mobile playground — test layouts on iOS/Android | 3 files | ~350 |
| `android_playground/` | Android | Android playground — loads phone contacts, renders via Compose | 5 files | ~500 |
| `docs/` | — | Full engine documentation | 13 .md | ~5000 |

---

## Playgrounds

### Web playground

```
open playground/index.html   # or serve via any HTTP server
```

The web playground lets you:
- Paste PFDL JSON and see it render instantly
- Switch between JSON / Kotlin / C / JavaScript source views
- Load 4 preset layouts (About, Feedback, Registration, All Components)
- Edit JSON live and see errors highlighted

### Mobile playground (Flutter)

```
cd mobile_playground && flutter run
```

The Flutter playground provides 3‑tab UI (Preview / JSON Editor / Presets) with live rendering.

### Android playground (Contacts demo)

**Pre‑built APK:** [Download app-debug.apk](https://raw.githubusercontent.com/MaurerAnton/projectforge-dynamiclayout-xx/master/android_playground/app-debug.apk)

**Build from source:** Open `android_playground/` in Android Studio → Run

The Android playground demonstrates SDUI with real data:
- Reads all **phone contacts** via `ContentResolver`
- Converts contacts to **DynamicLayout JSON** (search input + one FIELDSET per contact)
- Renders with **Jetpack Compose** renderer
- Typing in the search field **regenerates JSON** and re‑renders — full SDUI round‑trip

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

| Document | About |
|----------|-------|
| [GETTING_STARTED.md](GETTING_STARTED.md) | Full tutorial from scratch |
| [architecture.md](docs/projectforge-sdui-engine-docs/architecture.md) | Full architecture — 42 SDKs, 12 renderers, data flow |
| [comparison.md](docs/projectforge-sdui-engine-docs/comparison.md) | Detailed comparison with RJSF, Formily, AdminJS, Causeway, DivKit |
| [api-reference.md](docs/projectforge-sdui-engine-docs/api-reference.md) | Complete UI component reference (1157 lines) |
| [json-schema.md](docs/projectforge-sdui-engine-docs/json-schema.md) | JSON Schema + validation examples (JS, Python, Go, Kotlin) |
| [history.md](docs/projectforge-sdui-engine-docs/history.md) | Engine history — from March 2019 to 2026 |
| [dynamiclayout-schema.json](dynamiclayout-schema.json) | Formal JSON Schema (Draft 2020-12) |
| [ROADMAP.md](docs/projectforge-sdui-engine-docs/ROADMAP.md) | Planned features |
