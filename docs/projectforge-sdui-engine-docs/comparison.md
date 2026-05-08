# Comparison with Competitors

## Summary Table

| Aspect | ProjectForge | RJSF | Formily | AdminJS | Apache Causeway | DivKit |
|--------|-------------|------|---------|---------|-----------------|--------|
| **GitHub stars** | N/A (private) | ~15 800 | ~12 500 | ~8 900 | ~890 | ~2 600 |
| **Type** | Server framework | Client library | Client library | Server framework | Server framework | Client renderer |
| **Server language** | Kotlin/Java | Any (JSON) | Any (JSON) | Node.js/TypeScript | Java | Any (JSON) |
| **Client language** | React | React | React/Vue | React | Wicket | Native + React |
| **Full SDUI** | ✅ Yes | ❌ Forms only | ❌ Forms only | ⚠️ CRUD | ✅ Yes | ✅ Yes |
| **UI generation** | Explicit (Kotlin DSL) | From JSON Schema | From JSON Schema | From metadata | Reflection (auto) | From JSON (DivJson) |
| **Custom pages** | Full control | ❌ | ❌ | ⚠️ Via components | ⚠️ Via XML/Wicket | Via JSON |
| **Bootstrap grid** | ✅ Row/Col (+Fieldset) | ⚠️ uiSchema layout | ⚠️ FormGrid | ✅ Custom design system | ⚠️ XML layout | Custom containers |
| **AgGrid integration** | ✅ Built-in | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Validation** | Server-side (JPA + Spring) | Client-side (AJV) | Client-side | Server-side | Server-side | ❌ |
| **i18n** | Server-side (I18nResources) | Client-side (translateString) | Built-in | Via i18n | Built-in | ❌ No built-in |
| **Access control** | ✅ Spring Security + UserAccess | ❌ | ❌ | ⚠️ External | ✅ SecMan | ❌ |
| **Enterprise-ready** | ✅ Yes | ⚠️ Forms | ⚠️ Forms | ⚠️ CRUD | ✅ Yes | ⚠️ Mobile focus |
| **Performance** | Medium | Medium | High (reactive) | Medium | Medium | High (native) |

---

## 1. RJSF (react-jsonschema-form)

⭐ 15 800 | [GitHub](https://github.com/rjsf-team/react-jsonschema-form)

### What is this

React library for generating forms from JSON Schema. The most popular in its niche.

### How it works

```jsx
<Form schema={schema} validator={validator} onSubmit={...} />
```

JSON Schema describes the data (types, constraints), `uiSchema` describes the appearance. RJSF maps types to React components (StringField → input, BooleanField → checkbox, etc.).

### When to choose RJSF

- Need **forms only** (not full SDUI)
- Server in any language, client in React
- Want standard JSON Schema (many tools available)
- Small project without complex server-side UI logic

### When NOT to choose

- Need full control over layout (Row/Col/Fieldset)
- Need complex pages, not just forms
- Need server-side validation mapped to fields
- Enterprise-level access control in UI

### Key difference from ProjectForge

RJSF is a **client library** — the server only provides data. ProjectForge is a **server framework** where the server controls not just data but also UI structure and actions.

---

## 2. Formily (Alibaba)

⭐ 12 500 | [GitHub](https://github.com/alibaba/formily)

### What is this

Cross-platform (React/Vue) form framework from Alibaba. Focused on performance through reactive state.

### How it works

```
JSON Schema (with x-extensions) → @formily/json-schema → @formily/core → @formily/react → DOM
```

Each field has its own reactive state (via `@formily/reactive` using Proxy), so changing one field does not re-render the entire form.

### When to choose Formily

- Very **large and complex forms** (100+ fields) — reactivity provides a boost
- Forms with complex **field dependencies** (`x-reactions`)
- Need a visual form builder (Designable)
- Need Vue and React support simultaneously

### When NOT to choose

- Server-driven UI (Formily is a client tool)
- Java/Spring backend (no native integration)
- Pages that are not forms

### Key difference

Formily is **client-centric**. The server sends JSON Schema, but all rendering, validation, and linkage logic lives on the client. ProjectForge is **server-centric**: the server decides what and how to display.

---

## 3. AdminJS

⭐ 8 900 | [GitHub](https://github.com/SoftwareBrothers/adminjs)

### What is this

Node.js admin panel that auto-generates a CRUD interface from ORM models.

### How it works

```
ORM model → AdminJS resource → REST API → React frontend
```

Define resources (bound to TypeORM/Prisma/Mongoose), get list/show/edit/delete views.

### When to choose AdminJS

- Typical admin panel over a database (CRUD)
- Server on Node.js/TypeScript
- Quick start — minimal code

### When NOT to choose

- Java/Spring ecosystem (AdminJS is Node.js only)
- Complex custom pages beyond CRUD
- Need full SDUI

### Key difference

AdminJS is a **CRUD generator** for Node.js. ProjectForge is a **full SDUI framework** for Java/Kotlin. AdminJS is great for rapid admin prototyping; ProjectForge is for enterprise applications with fine-grained UI control.

---

## 4. Apache Causeway (formerly Apache Isis)

⭐ 890 | [GitHub](https://github.com/apache/causeway) | [Website](https://causeway.apache.org/)

### What is this

Java framework implementing the **Naked Objects** pattern: UI is fully generated from domain objects through reflection.

### How it works

```java
@DomainObject
public class Person {
    @Property @Title
    public String getName() { ... }
    
    @Action
    public void sayHello() { ... }
}
```

No controllers, templates, or UI code — the framework analyzes annotations and builds Wicket pages automatically.

### Closest competitor to ProjectForge

Apache Causeway is the **only direct competitor** in the "Java/Kotlin enterprise SDUI" space. Both solve the same problem: the server manages the UI. But their approaches differ fundamentally:

| Aspect | Apache Causeway | ProjectForge |
|--------|----------------|--------------|
| **UI generation** | Automatic, via reflection | Explicit, via Kotlin DSL |
| **Control** | Through annotations and XML layout | Through code (UILayout, UIRow, UIInput...) |
| **Flexibility** | Limited by framework | Full, because UI = code |
| **Development speed** | Very fast (write only the model) | Medium (each page manually) |
| **UI framework** | Wicket (React via REST API) | React |
| **Learning curve** | Need DDD + Causeway API | Need to know Kotlin DSL |

### When to choose Apache Causeway

- Domain changes frequently and you want the UI to update **automatically**
- Domain fits well into the object-action model
- Pixel-perfect UI is not critical

### When NOT to choose

- Need **full control** over every page
- UI differs significantly from "object with fields"
- Complex custom workflows (wizards, multi-step forms)

### Key difference

Causeway follows the "domain dictates UI" path (Naked Objects). ProjectForge follows "developer explicitly builds UI for each screen." Both approaches have merit, but ProjectForge gives more control at the cost of more effort.

---

## 5. DivKit (Yandex)

⭐ 2 600 | [GitHub](https://github.com/divkit/divkit)

### What is this

SDUI framework from Yandex for mobile and web applications. Describes UI in JSON ("DivJson") and renders natively.

### How it works

```json
{
  "card": {
    "states": [{
      "div": {
        "type": "container",
        "items": [
          { "type": "text", "text": "Hello" },
          { "type": "input", "text_variable": "name" }
        ]
      }
    }]
  }
}
```

The server sends JSON, native clients (iOS/Android/Web) render it.

### When to choose DivKit

- Cross-platform mobile app (iOS + Android)
- Want to change UI without App Store releases
- Static screens, cards, feeds

### When NOT to choose

- Web-only application (DivKit for web uses Svelte, not React)
- Complex enterprise forms (no date picker, autocomplete, file upload)
- Java/Spring ecosystem

### Key difference

DivKit is **mobile SDUI** (native rendering on iOS/Android). ProjectForge is **web enterprise SDUI** (React in the browser). DivKit excels at animations and templates; ProjectForge excels at forms, tables, and enterprise integration.

---

## Detailed Comparison by Criteria

### 1. Server-Driven UI (completeness)

```
ProjectForge     ████████████████████ 100% (server controls everything)
Causeway         ████████████████████ 100% (server generates UI)
AdminJS          ██████████░░░░░░░░░░ 50%   (CRUD templates + custom)
DivKit           ████████████████████ 100% (pure SDUI)
RJSF             ████░░░░░░░░░░░░░░░░ 20%   (form only)
Formily          ████░░░░░░░░░░░░░░░░ 20%   (form only)
```

### 2. Custom Page Flexibility

```
ProjectForge     ████████████████████ (Kotlin DSL — full code)
Causeway         ████████░░░░░░░░░░░░ (annotations + XML + Wicket)
AdminJS          ██████░░░░░░░░░░░░░░ (component overrides)
DivKit           ██████████░░░░░░░░░░ (DivJson + custom components)
RJSF             ██░░░░░░░░░░░░░░░░░░ (form fields only)
Formily          ████░░░░░░░░░░░░░░░░ (form fields + void nodes)
```

### 3. Enterprise Integration (Java/Spring)

```
ProjectForge     ████████████████████ (Spring Boot, JPA, Security)
Causeway         ████████████████████ (Spring Boot, own security)
AdminJS          ██░░░░░░░░░░░░░░░░░░ (Node.js only)
DivKit           ██░░░░░░░░░░░░░░░░░░ (any backend, no integration)
RJSF             ██████░░░░░░░░░░░░░░ (any backend, no integration)
Formily          ████░░░░░░░░░░░░░░░░ (any backend, no integration)
```

### 4. Tables and Grids

```
ProjectForge     ████████████████████ (AgGrid, Table + filter, sort, export)
Causeway         ████████░░░░░░░░░░░░ (basic collections)
AdminJS          ██████████░░░░░░░░░░ (lists + filters)
DivKit           ██████░░░░░░░░░░░░░░ (gallery, grid, pager)
RJSF             ██████░░░░░░░░░░░░░░ (ArrayField)
Formily          ████████░░░░░░░░░░░░ (ArrayTable, Editable)
```

### 5. Rendering Performance

```
ProjectForge     ████████░░░░░░░░░░░░ (React/Redux, full re-render)
Causeway         ████████░░░░░░░░░░░░ (Wicket server-side)
AdminJS          ██████████░░░░░░░░░░ (React/Redux)
DivKit           ████████████████████ (native rendering)
RJSF             ████████░░░░░░░░░░░░ (React, full re-render)
Formily          ████████████████████ (reactive, field-level rendering)
```

### 6. Community and Ecosystem

```
RJSF             ⭐ 15 800 — largest community
Formily          ⭐ 12 500 — Alibaba-backed, active development
AdminJS          ⭐ 8 900  — Node.js community
DivKit           ⭐ 2 600  — Yandex, Russian-speaking community
Causeway         ⭐ 890    — Apache, niche but stable
ProjectForge     Private — single user = Micromata
```

---

## Weaknesses of ProjectForge DynamicLayout

### 1. No JSON Schema (main problem)

The JSON format is not specified. You cannot validate JSON on the client, generate layouts from other languages (Python, Go, PHP), or write a type-safe client. RJSF and DivKit have formal specifications; ProjectForge only has source code.

### 2. No templates

In DivKit, you can define a template once and reuse it with parameters. In ProjectForge, each layout is generated from scratch, copying the same Row/Col for every screen. This increases JSON size and serialization time.

### 3. No variables or expressions

In DivKit: `@{count + 1}`, `@{min(1.0, max(alpha, 0.0))}`. In ProjectForge — only `data` + `setData` on the client. The server cannot describe dynamic behavior without a round-trip to the server (watchFields).

### 4. Full React tree re-render

Any `setData()` call re-renders the entire DynamicLayout. No React.memo, no selectors, no virtualization. On pages with 150+ elements, this is noticeable. Formily only updates the changed field (field-level reactive).

### 5. Server-side build performance

Each request: UILayout construction + LayoutUtils.process() + Gson serialization. At ~55ms per request, this is fine for 50-200 users, but at 10k+ RPS, caching is needed.

### 6. No layout caching

The same page for 100 users is generated 100 times. Read-only pages (About) could be cached — but this isn't implemented.

### 7. Tight coupling to Gson

Custom serializers (UISelectTypeSerializer, Domain Serializers) are tied to Gson. Migrating to Jackson or kotlinx.serialisation would require rewriting all serializers.

### 8. High learning curve

Developers need to know Kotlin DSL, the AbstractPagesRest pattern, and LayoutUtils. For RJSF, it's just `{ schema, uiSchema }`. For Causeway, just write a Java class with annotations.

### 9. Client-side only

Unlike Causeway (server-side HTML) and DivKit (native), ProjectForge requires JavaScript. Without JS — empty page, zero SEO.

### 10. React dependency

DynamicRenderer is tied to React. Unlike DivKit (iOS/Android/Web) or JSON Forms (React/Angular/Vue), it can't be used with Vue, Svelte, or native platforms.

### Bottleneck Summary

| Problem | Impact | Fixable? |
|---------|--------|----------|
| No JSON Schema | Cannot use from other languages | ✅ Medium |
| No templates | Bloated JSON | ❌ Hard |
| No variables/expressions | Many server round-trips | ❌ Hard |
| Full re-render | Slow on 150+ elements | ✅ Easy (React.memo) |
| No caching | Unnecessary server load | ✅ Easy (Spring @Cacheable) |
| Server build ~55ms | Doesn't scale to 10k+ RPS | ✅ Medium |
| Gson dependency | Hard to switch serializer | ❌ Hard |
| Client-side only | No SEO | ❌ Architectural |
| React dependency | No other platforms | ❌ Architectural |

---

## Verdict

### When ProjectForge is the best choice

- Enterprise Java/Kotlin + Spring Boot
- Need **full SDUI** (server manages UI)
- Complex forms + tables (AgGrid) + custom pages
- Server-side validation and access control built into UI
- Single stack (Java/Kotlin) for UI and business logic

### When to consider alternatives

- **RJSF** — if you need only JSON Schema forms, simple and standard
- **Formily** — if forms are very large (100+ fields) with complex dependencies
- **AdminJS** — if your stack is Node.js and you need a quick CRUD admin
- **Apache Causeway** — if your domain is stable and you want minimal UI code
- **DivKit** — if you need a mobile app with SDUI (iOS/Android)

### ProjectForge vs Apache Causeway: the main choice for Java projects

| Criterion | ProjectForge | Causeway |
|-----------|-------------|----------|
| You want | Control over every page | Auto-generated UI |
| Your domain | Complex, many custom screens | Fits object model well |
| UI | React, modern, custom | Wicket, Bootstrap, template-like |
| Team size | Has resources for UI code | Minimal UI developers |
| Change speed | Low-medium (UI = code) | High (UI = annotations) |
