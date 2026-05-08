# ProjectForge DynamicLayout Engine — Completed and Remaining

## Engine Size

| Component | Files | Lines of code | Disk size |
|-----------|:-----:|:-------------:|:---------:|
| Kotlin UI (59 .kt) | 59 | 5 917 | 211 KB |
| React DynamicLayout (90 jsx/tsx/ts) | 90 | 9 532 | 377 KB |
| CSS/SCSS | 3 | 158 | — |
| JS (AgGrid localization) | 1 | 600 | — |
| **Total** | **153** | **~16 207** | **~588 KB** |

---

## What Has Been Done

### "About ProjectForge" Menu Item

| File | Change |
|------|--------|
| `I18nResources.properties` (line 1784) | Added `menu.help.about=About ProjectForge` |
| `I18nResources_de.properties` (line 1870) | Added `menu.help.about=Über ProjectForge` |
| `MenuItemDefId.kt` (line 75) | Added `HELP_ABOUT` |
| `MenuRest.kt` (line 67) | Added to `myAccountMenu` after FEEDBACK |
| `AboutPageRest.kt` (new file) | REST controller for GET /rs/about/dynamic |

**Result:** The "About ProjectForge" item appears in the user dropdown menu (top right) after rebuild. The page shows version, build info, GPLv3 license, and links.

### Engine Documentation (12 files, 3151+ lines, 143 KB)

```
docs/
├── index.md           — Introduction and overview
├── history.md         — Full timeline from March 2019 to React 18
├── architecture.md    — 3 layers, 59 Kotlin files, 91 React files
├── pipeline.md        — 6 steps: Kotlin DSL → JSON → React → callAction
├── api-reference.md   — All 33 types, fields, JSON keys
├── json-schema.md     — Draft 2020-12 JSON Schema
├── examples.md        — 6 examples: From About to AgGrid
├── performance.md     — JSON size, timing, bottlenecks
├── comparison.md      — RJSF, Formily, AdminJS, Causeway, DivKit
├── issues.md          — What has been done, what remains
├── REFACTORING.md     — Extraction strategy
└── ROADMAP.md         — Priorities and timelines
```

---

## What Remains / Can Be Done

### 🔴 Now — Most Useful

1. **Published npm package** — `@dynamiclayout/react` needs publishing
2. **JSON Schema** — commit `dynamiclayout-schema.json` to the repo
3. **React.memo** — already applied to DynamicGroup, DynamicFieldset, DynamicInlineGroup
4. **Full native CI** — linuxArm64 build on GitHub Actions (cross-compile)

### 🟡 Medium

5. **Cache read-only layouts** — `@Cacheable` on About controller
6. **Templates (define/use)** — reduce JSON size, like DivKit
7. **Field-level reactivity** — React.memo + field-level context

### 🔵 Long Term

8. **kotlinx.serialisation** — replace Gson for type-safe JSON + multiplatform
9. **Variables + expressions** — DivKit-like `@{var + 1}` expressions
10. **Publish as standalone repo** — final polish before public release

## Priorities by Impact

| Task | Impact | Complexity | Timeframe |
|------|--------|:----------:|:---------:|
| npm package | Usability | Low | Days |
| JSON Schema | Standardization | Medium | Weeks |
| Templates | 2x smaller JSON | High | Months |
| Variables | Fewer round-trips | High | Months |
| Field-level | Performance | Medium | Weeks |
| kotlinx.serialisation | Type-safe, multiplatform | High | Months |
