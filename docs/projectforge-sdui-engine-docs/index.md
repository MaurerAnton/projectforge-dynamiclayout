# ProjectForge Server-Driven UI Engine

## What is this?

ProjectForge DynamicLayout Engine is a **server-driven UI (SDUI) framework** built into ProjectForge. The server (Kotlin/Spring) defines the user interface as a JSON structure, and the React client (`DynamicRenderer`) renders it recursively into the DOM.

```
Kotlin DSL  →  JSON (Gson)  →  DynamicRenderer (React)  →  DOM
```

## Key features

- **30 registered UI components** — from simple (Label, Input) to complex (AgGrid, AttachmentList)
- **Server-driven actions** — the server defines what happens when a button is clicked (redirect, modal, save, delete, toast)
- **Automatic validation** — `@Column` JPA annotations are translated into `maxLength`, `required`
- **i18n** — all labels are translated server-side via `I18nResources.properties`
- **User access** — buttons are shown/hidden based on user permissions
- **Watch fields** — changing one field can rebuild other fields
- **Server-side rendering** — the server sends the layout, React only renders (Zero Business Logic on the client)

## Statistics

| Metric | Value |
|---------|-------|
| UIElementType (component types) | 33 |
| Registered in DynamicRenderer | 30 |
| Kotlin UI files | 59 |
| React components (dynamicLayout) | 90 |
| Kotlin data classes | 22 |
| Customized components (domain-specific) | 28 |

## Documentation

### For new users
| File | About |
|------|-------|
| [GETTING_STARTED.md](https://github.com/MaurerAnton/projectforge-dynamiclayout/blob/master/GETTING_STARTED.md) | Tutorial from scratch: backend + frontend + examples |
| [examples.md](examples.md) | Examples: from "About" to complex forms |
| [json-schema.md](json-schema.md) | JSON Schema for the layout format |

### For engine developers
| File | About |
|------|-------|
| [architecture.md](architecture.md) | Architecture, layers, components, data flow |
| [pipeline.md](pipeline.md) | Full pipeline: from Kotlin DSL to React rendering |
| [api-reference.md](api-reference.md) | Complete UI component reference |
| [performance.md](performance.md) | Performance, JSON size, optimization |
| [comparison.md](comparison.md) | Comparison with RJSF, Formily, AdminJS, Causeway, DivKit |

## Source locations

| Component | Path |
|-----------|------|
| Kotlin UI classes | `projectforge-rest/.../ui/` |
| REST controllers | `projectforge-rest/.../rest/` |
| LayoutUtils | `projectforge-rest/.../ui/LayoutUtils.kt` |
| DynamicRenderer (React) | `projectforge-webapp/.../dynamicLayout/` |
| FormPage (React entry) | `projectforge-webapp/.../page/form/FormPage.jsx` |
| MenuItemDefId | `projectforge-business/.../menu/builder/MenuItemDefId.kt` |
| MenuRest | `projectforge-rest/.../rest/MenuRest.kt` |
