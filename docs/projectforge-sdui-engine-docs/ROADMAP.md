# Roadmap: DynamicLayout Engine — Extraction into a Separate Project

## Legend

- ✅ — can be tested locally without deployment
- ⚠️ — only testable together with ProjectForge
- 🧪 — needs a separate demo project for testing

---

## 1. 🟢 Set up CI (GitHub Actions)

### What to do
Create `.github/workflows/` with pipelines:
- **Core (Kotlin)**: `gradle build` + `gradle test`
- **Spring**: `gradle :spring:build`
- **React (npm)**: `npm ci && npm test && npm run build`

### Why
Without CI, changes cannot be verified as not breaking existing code.

### How to implement
```yaml
# .github/workflows/ci.yml
name: CI
on: [push, pull_request]
jobs:
  core:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: java-version: '17' distribution: 'temurin'
      - run: ./gradlew :core:build
  react:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: react
    steps:
      - uses: actions/setup-node@v4
      - run: npm ci && npm test && npm run build
```

### ✅ Local testing
```bash
cd dynamiclayout && ./gradlew :core:build
cd dynamiclayout/react && npm test
```

---

## 2. 🟢 Publish npm package for React

### What to do
Extract DynamicRenderer (React) into a separate npm package `@dynamiclayout/react`.

### Why
So the React renderer can be used in any project without copying files.

### How to implement

**`react/package.json`:**
```json
{
  "name": "@dynamiclayout/react",
  "version": "0.1.0",
  "main": "dist/index.js",
  "peerDependencies": { "react": "^18.0.0", "reactstrap": "^9.0.0" }
}
```

**What the package includes:**

```
react/
├── src/
│   ├── index.jsx                 # DynamicLayout — context provider
│   ├── context.tsx                # DynamicLayoutContext
│   ├── components/
│   │   ├── DynamicRenderer.jsx    # Registration + rendering
│   │   ├── DynamicGroup.jsx       # ROW / COL / GROUP
│   │   ├── DynamicFieldset.jsx    # FIELDSET
│   │   ├── DynamicInlineGroup.jsx
│   │   ├── DynamicLabel.jsx
│   │   ├── DynamicButton.jsx
│   │   ├── DynamicAlert.jsx
│   │   ├── DynamicSpacer.jsx
│   │   ├── DynamicBadge.jsx
│   │   ├── DynamicBadgeList.jsx
│   │   ├── DynamicProgress.jsx
│   │   ├── DynamicList.jsx
│   │   └── input/
│   │       ├── DynamicInputResolver.jsx
│   │       ├── DynamicInput.jsx
│   │       ├── DynamicCheckbox.jsx
│   │       ├── DynamicTextArea.jsx
│   │       ├── DynamicEditor.jsx
│   │       ├── DynamicDateInput.jsx
│   │       ├── DynamicTimeInput.jsx
│   │       ├── DynamicTimestampInput.jsx
│   │       ├── DynamicRadioButton.jsx
│   │       ├── DynamicRating.jsx
│   │       ├── DynamicReadonlyField.jsx
│   │       ├── DynamicAutoCompletion.jsx
│   │       └── DynamicValidationManager.jsx
│   └── select/
│       ├── DynamicReactSelect.jsx
│       ├── DynamicReactCreatableSelect.jsx
│       ├── DynamicObjectSelect.jsx
│       └── task/
│           └── index.jsx
├── package.json
├── tsconfig.json
└── vite.config.js
```

**What is NOT included:**
- `customized/` (28 domain-specific components — stay in ProjectForge)
- `table/` (DynamicTable, DynamicAgGrid — tightly coupled with PF)
- `upload/` (file upload)

These components stay in ProjectForge and are connected via `registerComponent()`.

### ✅ Local testing
```bash
cd dynamiclayout/react
npm install
npm run build
```

---

## 3. 🟢 JSON Schema

### What to do
Create a formal JSON Schema file `dynamiclayout-schema.json` and maintain it.

### Why
Without a schema, it's unclear what JSON DynamicRenderer expects. With a schema:
- Validate JSON on the client (AJV) and server
- Generate layouts from other languages (Python, Go, PHP)
- Auto-completion in IDEs (VS Code, IntelliJ)

### How to implement
A draft already exists in `json-schema.md`. Needs:
1. Extract to `dynamiclayout-schema.json`
2. Add to the repository
3. Write a test that validates a sample layout

### ✅ Local testing
```bash
npm install -g ajv-cli
ajv validate -s dynamiclayout-schema.json -d test-layout.json
```

---

## 4. 🟢 React.memo on DynamicGroup and DynamicFieldset

### What to do
Wrap `DynamicGroup`, `DynamicFieldset`, `DynamicInlineGroup` in `React.memo`.

### Why
Currently, any `setData()` re-renders the entire DynamicLayout — all 150+ elements. `React.memo` skips re-rendering when props haven't changed.

### How to implement

```jsx
export default React.memo(DynamicGroup,
    (prev, next) => prev.key === next.key && prev.content === next.content
);
```

**Important:** content comparison should be structural (compare `key` and `type` of each child), not reference-based.

### ✅ Local testing
```bash
cd projectforge-webapp
npm test
# Manual: Open a page with 50+ fields, check that field editing
# does not re-render the whole layout
# React DevTools → Profiler → record render
```

---

## 5. 🟡 Write README for the new repository

### What to do
README with:
- What is DynamicLayout
- Quick start (5 lines of code)
- How to integrate with Spring Boot
- How to use the React renderer
- Links to documentation in `docs/`

### Why
Without a README, no one will understand the purpose of this repository.

### Structure
```markdown
# DynamicLayout — Server-Driven UI Engine for Kotlin + React

DynamicLayout is an SDUI engine for enterprise applications.
The server describes UI in Kotlin → serializes to JSON →
the React client renders it recursively.

## Modules
- `core` — Kotlin DSL, no dependencies
- `spring` — Spring Boot auto-configuration
- `react` — npm package `@dynamiclayout/react`

## Quick Start

// Kotlin
val layout = UILayout("hello")
layout.add(UILabel("'Hello World"))
LayoutUtils.process(layout, i18nProvider)
return FormLayoutData(ui = layout)
```

### ✅ Local testing
Just `cat README.md` — documentation, nothing to test.

---

## 6. 🟡 Create a demo project

### What to do
A small Spring Boot + React project using DynamicLayout without ProjectForge.

### Why
- Proof that the engine works independently
- Example for new users
- Integration tests

### Structure
```
dynamiclayout-demo/
├── server/
│   ├── build.gradle.kts
│   └── src/main/kotlin/demo/
│       ├── DemoApplication.kt
│       ├── DemoPageRest.kt
│       └── application.properties
├── client/
│   ├── package.json
│   ├── vite.config.ts
│   └── src/
│       ├── App.jsx
│       └── main.jsx
└── README.md
```

### 🧪 Local testing
```bash
cd dynamiclayout-demo/server && ./gradlew bootRun &
cd dynamiclayout-demo/client && npm run dev
# Open http://localhost:5173
```

---

## 7. 🟡 Cache read-only layouts (@Cacheable)

### What to do
Add `@Cacheable` to REST controller methods that return static pages.

### Why
Currently, the same About page for 100 users is generated 100 times. A 5-minute cache reduces server load 100x.

### How to implement

```kotlin
@RestController
@RequestMapping("${Rest.URL}/about")
class AboutPageRest : AbstractDynamicPageRest() {
    @Cacheable("dynamicLayouts")
    @GetMapping("dynamic")
    fun getForm(request: HttpServletRequest): FormLayoutData { ... }
}
```

### ⚠️ Local testing
```bash
cd projectforge && ./gradlew bootRun
# First request: ~50ms. Second request: ~2ms (from cache)
```

---

## 8. 🟡 Add HELP_ABOUT to MenuConfiguration.kt

### What to do
Add a visibility property for the new menu item.

### Why
So admins can hide the "About ProjectForge" menu item via `projectforge.properties`.

### How to implement

```kotlin
@Value("\${projectforge.menu.visibility.about}")
private var aboutVisibility: String? = null
```

### ⚠️ Local testing
```bash
cd projectforge && ./gradlew bootRun
# projectforge.properties → projectforge.menu.visibility.about=hidden
```

---

## 9. 🔵 Templates (define/use)

### What to do
Add `define()` / `use()` mechanism to UILayout, similar to DivKit templates.

### Why
Currently, each layout is fully serialized. If the same form appears multiple times (e.g., an address in different pages), the same Row/Col structures are duplicated in JSON. Templates would reduce JSON size 2-3x.

### How to implement

```kotlin
val layout = UILayout("address.edit")
layout.define("addressFields") { /* ... */ }
layout.use("addressFields")
layout.use("addressFields")
```

### ✅ Local testing
Unit tests — verify that define/use correctly resolves into a flat layout.

---

## 10. 🔵 Field-level reactivity

### What to do
Instead of re-rendering the entire DynamicLayout on every change, re-render only the changed field.

### Why
On a page with 150 fields, every keystroke re-renders the entire layout. Formily solves this with field-level reactive (O(1) instead of O(n)).

### How to implement
1. Create `FieldContext` instead of a single `DynamicLayoutContext`
2. Each field reads only its own `data[id]`, not the entire `data`
3. `React.memo` with the correct comparer

### ✅ Local testing
Compare React DevTools Profiler before and after.

---

## 11. 🔵 kotlinx.serialisation

### What to do
Replace Gson with kotlinx.serialisation.

### Why
- **Type-safe** — no reflective serialization
- **Multiplatform** — core module can run on JS/Native
- **Faster** — codegen instead of reflection (2-3x)
- **Remove custom serializers** — `@Serializable` handles generics

### How to implement
```kotlin
@Serializable
data class UIInput(val id: String, val dataType: UIDataType = UIDataType.STRING, ...)
```

### ✅ Local testing
```kotlin
@Test fun `serialize UIInput`() {
    val input = UIInput(id = "name", dataType = UIDataType.STRING)
    val json = Json.encodeToString(input)
    assertTrue(json.contains(""""id":"name""""))
}
```

---

## Summary Table

| # | Task | Can be tested locally? | Depends on other tasks? | Time estimate |
|---|------|:----------------------:|:------------------------:|:-------------:|
| 1 | GitHub Actions CI | ✅ yes | — | 2-4 hours |
| 2 | npm package React | ✅ yes | 1 (CI) | 1 day |
| 3 | JSON Schema | ✅ yes | — | 4 hours |
| 4 | React.memo | ✅ yes | — | 2 hours |
| 5 | README | ✅ yes | — | 2 hours |
| 6 | Demo project | 🧪 demo | 1, 2, 5 | 2 days |
| 7 | @Cacheable | ⚠️ with PF | — | 2 hours |
| 8 | MenuConfiguration | ⚠️ with PF | — | 30 minutes |
| 9 | Templates | ✅ yes | — | 2 weeks |
| 10 | Field-level reactivity | ✅ yes | 4 | 1 week |
| 11 | kotlinx.serialisation | ✅ yes | 1 | 2 weeks |

## Recommended Order

**Day 1:** CI + npm package + JSON Schema + React.memo + README
**Day 2:** @Cacheable + MenuConfiguration
**Day 3-4:** Demo project
**Week 2+:** Templates, Field-level reactivity, kotlinx.serialisation
