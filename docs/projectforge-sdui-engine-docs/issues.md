# ProjectForge DynamicLayout — Completed and Remaining

## Engine Size

| Component | Files | Lines of code | Disk size |
|-----------|:-----:|:-------------:|:---------:|
| Core Kotlin (DSL) | 52 .kt | ~1 000 | ~200 KB |
| Spring integration | 3 .kt | ~140 | ~20 KB |
| React components | 21 .jsx | ~650 | ~100 KB |
| Docs | 12 .md | ~3 300 | ~150 KB |
| **Total repo** | **~90** | **~5 000** | **~500 KB** |

---

## What Has Been Done

### Engine Extracted to Standalone Repo

| Module | Location | Description |
|--------|----------|-------------|
| **Core** | `core/src/commonMain/` | 52 .kt, pure Kotlin Multiplatform (JVM + Native + JS) |
| **Spring** | `spring/` | Auto-configuration, I18nProvider, MetadataProvider |
| **React** | `react/` | npm package `@dynamiclayout/react` with 21 components |
| **Build** | `core/Makefile` + `build.sh` | Gradle-free native compilation |

### GitHub CI

| Job | Status |
|-----|--------|
| JVM jar | ✅ |
| JavaScript | ✅ |
| Native (linuxX64) | ✅ |
| React npm | ✅ |
| Tests | ✅ |

### ProjectForge Modifications

| Change | Files |
|--------|-------|
| `@Serializable` on 41 UI classes | `projectforge-rest/.../ui/*.kt` |
| kotlinx.serialisation dependency | `build.gradle.kts`, version catalog |
| KotlinxSerializationModule | Jackson adapter for `@Serializable` classes |
| UISelectTypeSerializer removed | Replaced by `@Serializable` |
| React.memo on container components | DynamicGroup, DynamicFieldset, DynamicInlineGroup |
| Context stabilization | DynamicLayout/index.jsx context value |

### "About ProjectForge" Menu Item

| File | Change |
|------|--------|
| `I18nResources.properties` | Added `menu.help.about=About ProjectForge` |
| `I18nResources_de.properties` | Added `menu.help.about=Über ProjectForge` |
| `MenuItemDefId.kt` | Added `HELP_ABOUT` |
| `MenuRest.kt` | Added to `myAccountMenu` |
| `AboutPageRest.kt` | New REST controller |

### Documentation (English, 12 files)

All engine documentation in `docs/projectforge-sdui-engine-docs/`:
architecture, pipeline, API reference, comparison, performance, history, roadmap, etc.

---

## What Remains

### 🟢 Next Steps

1. **Publish npm package** — `npm publish` for `@dynamiclayout/react`
2. **Enable linuxArm64 native build** — cross-compile from x86_64 CI runner
3. **Complete Makefile build** — `make native` without Gradle

### 🟡 Medium Term

4. **Cache read-only layouts** — `@Cacheable` on AboutPageRest
5. **Templates (define/use)** — reduce JSON, like DivKit
6. **Field-level reactivity** — avoid full page re-render

### 🔵 Long Term

7. **kotlinx.serialisation** — replace Gson entirely
8. **Variables + expressions** — `@{var + 1}` server-side logic
9. **Publish to Maven Central** — JAR for `org.dynamiclayout:core`
