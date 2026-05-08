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

## 🟡 10. Cache Read-Only Layouts

**Status: Planned.** Add Spring Cache config to enable `@Cacheable` on `AboutPageRest.kt`.

| Aspect | Detail |
|--------|--------|
| Impact | Reduces server load for static pages |
| Complexity | Low |
| What's needed | `@EnableCaching` in ProjectForge config |

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

## 🔵 14. Variables + Expressions

**Status: Planned.** Expression language similar to DivKit's `@{var + 1}`.
Allows server-side to describe dynamic behavior without round-tripping to server.

| Aspect | Detail |
|--------|--------|
| Impact | Fewer server round-trips, richer dynamic UI |
| Complexity | High |
| What's needed | Core DSL + React renderer changes |

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
| 10 | Cache layouts | 🟡 Medium | Medium | Low |
| 11 | Templates | 🟡 Medium | Medium | High |
| 12 | Field-level reactivity | 🟡 Medium | High | Medium |
| 13 | kotlinx.serialisation | 🔵 Long | Medium | High |
| 14 | Variables + expressions | 🔵 Long | Low | High |
