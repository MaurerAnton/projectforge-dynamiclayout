# Roadmap: DynamicLayout Engine

## Status Key

- ✅ Done
- 🟢 Next — high priority
- 🟡 Medium — medium priority
- 🔵 Long — long term

---

## ✅ 1. GitHub Actions CI

**Status: Done.** CI builds JVM, JavaScript, Native (linuxX64, linuxArm64), and React package on every push.

- [`.github/workflows/build.yml`](.github/workflows/build.yml) — 6 jobs: JVM, Native (linuxX64), Native (linuxArm64), JS, Tests, React
- All builds pass on `ubuntu-latest`
- Native ARM64 compiled via cross-compilation from x86_64 runner

---

## ✅ 2. React npm Package

**Status: Done.** `@dynamiclayout/react` package structure ready in `react/`:
- 21 JSX components (DynamicLayout, DynamicGroup, DynamicFieldset, DynamicInput, etc.)
- Vite build config for library mode
- package.json with peer dependencies (React 18, reactstrap)
- Builds successfully: `npm install && npm run build` → `dist/`

**Next:** publish to npm registry.

---

## ✅ 3. JSON Schema

**Status: Done.** [`dynamiclayout-schema.json`](dynamiclayout-schema.json) — Draft 2020-12 JSON Schema for the layout format:
- 33 UIElementType definitions
- Validation rules for containers, inputs, buttons, tables
- Example layout (`examples/about-layout.json`)

---

## ✅ 4. React.memo Optimization

**Status: Done.** Applied to container components:
- `DynamicGroup.jsx` — structural content comparison (type + key)
- `DynamicFieldset.jsx` — structural content comparison
- `DynamicInlineGroup.jsx` — structural content comparison
- `DynamicLayout/index.jsx` — context value stabilization with `useMemo`

---

## ✅ 5. Demo Project

**Status: Done.** [`dynamiclayout-demo/`](../dynamiclayout-demo/):
- Node.js server returning layout JSON for About and Registration Form pages
- Vite + React client importing DynamicLayout from local source
- One-command startup: `./run-demo.sh`

---

## ✅ 6. Engine Extraction

**Status: Done.** Core module extracted to `core/`:
- `commonMain/` — 52 Kotlin files, pure KMP (JVM + Native + JS)
- `jvmMain/` — `System.currentTimeMillis()`
- `nativeMain/` — `gettimeofday()`
- `jsMain/` — `Date.now()`
- Build: Gradle KMP + Makefile + build.sh

---

## ✅ 7. linuxArm64 Native Build

**Status: Done.** Cross-compilation from x86_64 CI runner:
- Task: `./gradlew linuxArm64MainKlibrary`
- Artifact: `dynamiclayout-linuxArm64` (175 KB .klib)
- Note: requires x86_64 host (kotlin-native prebuilt not available for ARM64 Linux)

---

## ✅ 8. Makefile + build.sh

**Status: Done.** Gradle-free native build on x86_64:
- `make native` / `./build.sh native` — .klib via kotlinc-native
- `make app` — native executable + run
- `make test` — 11 integration tests
- Falls back gracefully on ARM64 with Gradle instructions

---

## 🟢 9. Publish npm Package

**Status: Next step.** Publish `@dynamiclayout/react` to npm registry.

| Aspect | Detail |
|--------|--------|
| Impact | Anyone can `npm install @dynamiclayout/react` |
| Complexity | Low |
| What's needed | npm account, `npm publish` |

---

## ✅ 10. Cache Read-Only Layouts

**Status: Done.** Spring Cache configured for static About page:
- `AboutPageRest.kt` — layout generation cached in `buildLayout()` method
- `CacheConfiguration.kt` — `@EnableCaching` with `ConcurrentMapCacheManager`
- CSRF token not cached (generated outside cached method)
- Cache name: `dynamicLayouts`

---

## 🟡 11. Templates (define/use)

**Status: Planned.** Reduce JSON size by allowing reusable layout definitions.

```kotlin
layout.define("address") { /* UIRow, UICol, UIInput... */ }
layout.use("address")
layout.use("address")
```

| Aspect | Detail |
|--------|--------|
| Impact | 2-3x smaller JSON for repeated structures |
| Complexity | High |
| What's needed | Core DSL changes + Custom serializer |

---

## 🟡 12. Field-Level Reactivity

**Status: Planned.** Replace full DynamicLayout re-render with field-level context.
Currently each `setData()` re-renders all 150+ elements. Target: only re-render changed field.

| Aspect | Detail |
|--------|--------|
| Impact | Smooth 150+ field forms |
| Complexity | Medium |
| What's needed | React.memo (done) + context splitting |

---

## 🔵 13. kotlinx.serialisation

**Status: Planned.** Replace Gson with kotlinx.serialisation:
- Type-safe serialization
- No reflective Gson
- Multiplatform support
- Eliminates UISelectTypeSerializer

| Aspect | Detail |
|--------|--------|
| Impact | Type-safe, multiplatform, 2-3x faster |
| Complexity | High |
| What's needed | Gradle KMP (done) + migration |

---

## 🟢 16. Interactive Kotlin Editor in Playground

**Status: Next step.** Add a live Kotlin DSL editor that compiles to JSON and renders in real time.

### What to build
Extend the playground with a third mode: editable Kotlin source that gets compiled to JSON dynamically.

### Options

**Option A: Server-side compilation (recommended)**
Add a `/compile` endpoint to the demo Node.js server that:
1. Receives Kotlin DSL source code as a string
2. Calls the DynamicLayout Kotlin compiler (via subprocess or embedded)
3. Returns the compiled JSON

Playground → POST Kotlin code → Server compiles → Returns JSON → Playground renders

```kotlin
// User writes this in playground:
val layout = UILayout("'Hello")
layout.add(UIFieldset(title = "'Test")
    .add(UIInput("name", label = "'Name"))
)
LayoutUtils.process(layout)
return DynamicLayoutJson.encode(layout)
```

```json
// Server returns this:
{ "ui": { "title": "Hello", "layout": [...] } }
```

**Option B: Kotlin/JS transpilation**
Compile a subset of the DynamicLayout DSL to JavaScript using Kotlin/JS.
Full Kotlin compilation in browser via Kotlin/Wasm or Kotlin/JS.

| Aspect | Option A (server) | Option B (client) |
|--------|:-----------------:|:-----------------:|
| Setup complexity | Medium | High |
| Latency | ~100ms (network) | ~10ms (local) |
| Full Kotlin support | ✅ Yes | ⚠️ Subset |
| Infrastructure | Node.js server | Kotlin/JS build |
| Cost | One endpoint | Complex toolchain |

### Current state
JSON/Kotlin toggle already implemented in playground:
- JSON mode: editable textarea with live preview
- Kotlin mode: read-only DSL source for each preset
- Next step: make Kotlin editable and compilable

**Status: Planned.** Expression language similar to DivKit's `@{var + 1}`.
Allows server-side to describe dynamic behavior without round-tripping to server.

| Aspect | Detail |
|--------|--------|
| Impact | Fewer server round-trips, richer dynamic UI |
| Complexity | High |
| What's needed | Core DSL + React renderer changes |

---

## ✅ 15. Interactive Playground

**Status: Done.** A single HTML page (`playground/index.html`) that works in any browser:

- **Left pane:** JSON editor with syntax highlighting via monospace textarea
- **Right pane:** Live DynamicLayout renderer with all 19 registered components
- **Preset dropdown:** Load any example (About, Feedback, Registration)
- **Error display:** JSON parse errors shown as error bar
- **Status bar:** Character count, JSON validity indicator
- **Reset Data button:** Clear form data without reloading
- **No build step:** Just open `playground/index.html` in a browser
- **CDN dependencies:** React 18, Bootstrap 5 loaded from unpkg

### Layout
```
┌─────────────────────────────────────────────────┐
│  📝 Layout JSON     [Preset ▼] [Reset Data]    │
├─────────────────────┬───────────────────────────┤
│  { "ui": {          │ 🔍 Live Preview           │
│    "layout": [      │ ┌──────────────────┐      │
│      ...            │ │ Name: [___]      │      │
│    ]                │ │ Email: [___]     │      │
│  } }                │ │ [Submit] [Cancel]│      │
│                     │ └──────────────────┘      │
│  538 chars  JSON ✓  │ components: 3             │
└─────────────────────┴───────────────────────────┘
```
```

| Aspect | Detail |
|--------|--------|
| Impact | Interactive testing, onboarding |
| Complexity | Low |
| What's needed | React + DynamicLayout import |

---

## ✅ 17. C/C++ Header — Generate Layout JSON from C

**Status: Done.**`dynamiclayout.h` — single-header C library for generating DynamicLayout JSON from C/C++ code.

### What it provides
- **`DL_Builder`** — stateful JSON builder
- **`dl_begin()` / `dl_end()`** — start/finish layout
- **`dl_fieldset()`** — add FIELDSET with variadic children
- **`dl_about()`** — quick helper for static sections
- Handles JSON escaping, indentation, comma placement

### C example
```c
DL_Builder b = dl_begin(json, 4096, "My Page");
dl_about(&b, "Info", "Hello from C!");
dl_about(&b, "License", "GPLv3");
dl_end(&b);
// json now contains valid DynamicLayout JSON
```

### Example output
```json
{
  "ui": { "title": "My Page", "layout": [
    { "type": "FIELDSET", ... },
    { "type": "FIELDSET", ... }
  ]}
}
```

### How to use
```bash
gcc -o myapp myapp.c -I/path/to/dynamiclayout
```

### Next steps
- Full C++ wrapper with RAII and type-safe builders
- Support for all 33 UIElementType values
- Automated tests comparing C and Kotlin output

---

## Complete Summary

| # | Task | Status | Priority | Complexity |
|:--|------|--------|:--------:|:----------:|
| 1 | GitHub Actions CI | ✅ Done | — | Low |
| 2 | React npm package | ✅ Done | — | Low |
| 3 | JSON Schema | ✅ Done | — | Medium |
| 4 | React.memo | ✅ Done | — | Low |
| 5 | Demo project | ✅ Done | — | Medium |
| 6 | Engine extraction | ✅ Done | — | High |
| 7 | linuxArm64 CI | ✅ Done | — | Medium |
| 8 | Makefile build | ✅ Done | — | Low |
| 9 | Publish npm package | 🟢 Next | High | Low |
| 10 | Cache layouts | ✅ Done | — | Low |
| 11 | Templates | 🟡 Medium | Medium | High |
| 12 | Field-level reactivity | 🟡 Medium | High | Medium |
| 13 | kotlinx.serialisation | 🔵 Long | Medium | High |
| 14 | Variables + expressions | 🔵 Long | Low | High |
| 15 | Interactive playground | ✅ Done | — | Low |
| 16 | Kotlin editor in playground | 🟢 Next | High | Medium |
| 17 | C/C++ generation | ✅ Done | — | Medium |
