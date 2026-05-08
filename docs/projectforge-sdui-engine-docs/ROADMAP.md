# Roadmap: DynamicLayout Engine

## Status Key

- ✅ Done
- 🟢 Planned — high priority
- 🟡 Planned — medium priority
- 🔵 Planned — long term

---

## ✅ 1. GitHub Actions CI

**Status: Done.** CI builds JVM, JavaScript, Native (linuxX64), and React package on every push.

- [`.github/workflows/build.yml`](.github/workflows/build.yml) — 5 jobs: JVM, Native (linuxX64), JS, Tests, React
- Native ARM64 (linuxArm64) not yet enabled in CI (requires prebuilt kotlin-native for linux-aarch64)

---

## ✅ 2. npm Package (React)

**Status: Done.** `@dynamiclayout/react` package structure is ready in `react/`:
- 21 JSX components
- Vite build config
- package.json with peer dependencies

**Next step:** publish to npm registry.

---

## ✅ 3. JSON Schema

**Status: Done.** [`dynamiclayout-schema.json`](dynamiclayout-schema.json) — Draft 2020-12 JSON Schema for the layout format.

Includes:
- Type definitions for all 33 UIElementType values
- Validation rules for containers, inputs, buttons, tables
- Example layout included

---

## ✅ 4. React.memo Optimization

**Status: Done.** Applied to:
- `DynamicGroup.jsx` — structural content comparison
- `DynamicFieldset.jsx` — structural content comparison
- `DynamicInlineGroup.jsx` — structural content comparison
- `DynamicLayout/index.jsx` — context value stabilization

---

## ✅ 5. Demo Project

**Status: Done.** [`dynamiclayout-demo/`](../dynamiclayout-demo/) — runnable demo:
- Node.js server returning layout JSON (`server-node/`)
- Vite + React client with DynamicLayout import
- One-command startup: `./run-demo.sh`

---

## ✅ 6. Engine Extraction

**Status: Done.** Core module extracted to `core/`:
- `commonMain/` — 52 Kotlin files, platform-independent UI DSL
- `jvmMain/` — JVM platform (System.currentTimeMillis)
- `nativeMain/` — Native platform (gettimeofday)
- `jsMain/` — JS platform (Date.now)
- `spring/` — Spring Boot auto-configuration (optional)
- Build: Gradle KMP + Makefile + build.sh

---

## 🟢 7. Publish npm Package

Publish `@dynamiclayout/react` to npm registry.

| Aspect | Detail |
|--------|--------|
| Impact | Usability — anyone can `npm install` |
| Complexity | Low |
| Prerequisites | GitHub Actions, npm token |

---

## 🟢 8. Enable linuxArm64 in CI

Add `linkDebugKotlinNativeLinuxArm64` to the CI native matrix.
Requires: Kotlin 2.1+ with system LLVM support, or cross-compilation setup.

---

## 🟢 9. Native Build via Makefile

Complete `core/Makefile` + `core/build.sh` for Gradle-free native compilation.
Currently works on x86_64 with `kotlinc-native`.

---

## 🟡 10. Cache Read-Only Layouts

Add `@Cacheable` to `AboutPageRest.kt` (already has the annotation).
Requires Spring Cache configuration to be enabled.

| Aspect | Detail |
|--------|--------|
| Impact | Reduces server load for static pages |
| Complexity | Low |
| Prerequisites | Spring Boot Cache |

---

## 🟡 11. Templates (define/use)

Reduce JSON size by allowing reusable layout templates, similar to DivKit.
Design: `layout.define("card") { ... }` / `layout.use("card")`.

| Aspect | Detail |
|--------|--------|
| Impact | 2-3x smaller JSON for repeated structures |
| Complexity | High |
| Prerequisites | Core DSL changes |

---

## 🟡 12. Field-Level Reactivity

Replace full DynamicLayout re-render with field-level context updates.
Currently each `setData` re-renders the entire page. Target: only re-render changed fields.

| Aspect | Detail |
|--------|--------|
| Impact | Smooth 150+ field forms |
| Complexity | Medium |
| Prerequisites | React.memo (done) + context split |

---

## 🔵 13. kotlinx.serialisation

Replace Gson with kotlinx.serialisation for type-safe serialization.
Removes UISelectTypeSerializer, Jackson annotations, and reflective Gson.

| Aspect | Detail |
|--------|--------|
| Impact | Type-safe, multiplatform, 2-3x faster |
| Complexity | High |
| Prerequisites | Gradle KMP setup (done) |

---

## 🔵 14. Variables + Expressions

Add expression language support (like DivKit's `@{var + 1}`).
Allows server-side logic without round-trips.

| Aspect | Detail |
|--------|--------|
| Impact | Fewer server round-trips, richer dynamic UI |
| Complexity | High |
| Prerequisites | Core DSL + React renderer changes |

---

## Summary

| # | Task | Status | Priority | Complexity |
|:--|------|--------|:--------:|:----------:|
| 1 | GitHub Actions CI | ✅ Done | — | Low |
| 2 | npm package | ✅ Done | — | Low |
| 3 | JSON Schema | ✅ Done | — | Medium |
| 4 | React.memo | ✅ Done | — | Low |
| 5 | Demo project | ✅ Done | — | Medium |
| 6 | Engine extraction | ✅ Done | — | High |
| 7 | Publish npm package | 🟢 Next | High | Low |
| 8 | linuxArm64 CI | 🟢 Next | Medium | Medium |
| 9 | Native Makefile | 🟢 Next | Low | Low |
| 10 | Cache layouts | 🟡 Medium | Medium | Low |
| 11 | Templates | 🟡 Medium | Medium | High |
| 12 | Field-level reactivity | 🟡 Medium | High | Medium |
| 13 | kotlinx.serialisation | 🔵 Long | Medium | High |
| 14 | Variables + expressions | 🔵 Long | Low | High |
