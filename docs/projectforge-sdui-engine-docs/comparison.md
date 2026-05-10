# Comparison with Competitors

## Summary Table

| Aspect | PFDL (DynamicLayout) | RJSF | Formily | AdminJS | Apache Causeway | DivKit |
|--------|-------------|------|---------|---------|-----------------|--------|
| **GitHub stars** | — | [~15 800](https://github.com/rjsf-team/react-jsonschema-form) | [~12 500](https://github.com/alibaba/formily) | [~8 900](https://github.com/SoftwareBrothers/adminjs) | [~890](https://github.com/apache/causeway) | [~2 600](https://github.com/divkit/divkit) |
| **Type** | [Server framework](architecture.md) | [Client library](https://rjsf-team.github.io/react-jsonschema-form/docs/) | [Client library](https://formilyjs.org/) | [Server framework](https://docs.adminjs.co/) | [Server framework](https://causeway.apache.org/docs/) | [Client renderer](https://divkit.tech/doc) |
| **Server language** | [42 languages](architecture.md) | [Any](https://rjsf-team.github.io/react-jsonschema-form/docs/#introduction) | [Any](https://formilyjs.org/) | [Node.js/TypeScript](https://docs.adminjs.co/installation/getting-started) | [Java](https://causeway.apache.org/docs/latest/starters/simpleapp/) | [Any](https://divkit.tech/doc#for-server-developer) |
| **Client language** | [React](https://github.com/MaurerAnton/projectforge-dynamiclayout/tree/master/projectforge-webapp) | [React](https://github.com/rjsf-team/react-jsonschema-form) | [React/Vue](https://formilyjs.org/guide/quick-start) | [React](https://github.com/SoftwareBrothers/adminjs/tree/master/src/frontend) | [Wicket](https://causeway.apache.org/docs/latest/viewers/wicket/about/) | [Native + Web](https://github.com/divkit/divkit) |
| **Full SDUI** | [✅ Yes](architecture.md) | ❌ Forms only | ❌ Forms only | ⚠️ CRUD | [✅ Yes](https://causeway.apache.org/) | [✅ Yes](https://divkit.tech/) |
| **UI generation** | [Explicit (42-language DSL)](architecture.md) | [From JSON Schema](https://rjsf-team.github.io/react-jsonschema-form/docs/usage/schema) | [From JSON Schema](https://formilyjs.org/guide/learn-formily) | [From metadata](https://docs.adminjs.co/basics/resource) | [Reflection (auto)](https://causeway.apache.org/docs/latest/core/overview/) | [From DivJson](https://divkit.tech/schema) |
| **Custom pages** | [Full control](examples.md) | ❌ Not possible | ❌ Not possible | [⚠️ Via components](https://docs.adminjs.co/ui-customization/writing-your-own-components) | [⚠️ Via XML/Wicket](https://causeway.apache.org/docs/latest/userguide/layout/) | [Via JSON](https://divkit.tech/doc) |
| **Bootstrap grid** | [✅ Row/Col + Fieldset](pipeline.md) | ⚠️ [uiSchema](https://rjsf-team.github.io/react-jsonschema-form/docs/api-reference/uiSchema) layout | ⚠️ [FormGrid](https://formilyjs.org/zh-CN/components/form-grid) | ✅ [Design system](https://docs.adminjs.co/ui-customization/adminjs-design-system) | ⚠️ [XML layout](https://causeway.apache.org/docs/latest/userguide/layout/) | Custom containers |
| **Enterprise table** | [✅ AgGrid built-in](architecture.md) | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Validation** | [Server-side (JPA + Spring)](pipeline.md) | [Client-side (AJV)](https://rjsf-team.github.io/react-jsonschema-form/docs/usage/validation) | [Client-side](https://formilyjs.org/guide/learn-formily#validation) | [Server-side](https://docs.adminjs.co/basics/resource#validating-data) | [Server-side](https://causeway.apache.org/docs/latest/userguide/validation/) | ❌ None |
| **i18n** | [Server-side (I18nResources)](architecture.md) | [Client-side](https://rjsf-team.github.io/react-jsonschema-form/docs/usage/validation#i18n) | [Built-in](https://formilyjs.org/guide/learn-formily#internationalization) | [Via i18n](https://docs.adminjs.co/i18n/i18n) | [Built-in](https://causeway.apache.org/docs/latest/userguide/i18n/) | ❌ None |
| **Access control** | [✅ Spring Security](pipeline.md) | ❌ | ❌ | [⚠️ External](https://docs.adminjs.co/basics/authentication) | [✅ SecMan](https://causeway.apache.org/docs/latest/extensions/secman/) | ❌ |
| **Enterprise-ready** | ✅ Yes | ⚠️ Forms only | ⚠️ Forms only | ⚠️ CRUD | ✅ Yes | ⚠️ Mobile focus |
| **Performance** | Medium | Medium | [High (reactive)](https://github.com/alibaba/formily/tree/master/packages/reactive) | Medium | Medium | [High (native)](https://divkit.tech/doc) |
| **License** | [GPLv3](https://github.com/MaurerAnton/projectforge-dynamiclayout/blob/master/LICENSE) | [Apache 2.0](https://github.com/rjsf-team/react-jsonschema-form/blob/main/LICENSE.md) | [MIT](https://github.com/alibaba/formily/blob/master/LICENSE) | [MIT](https://github.com/SoftwareBrothers/adminjs/blob/master/LICENSE) | [Apache 2.0](https://github.com/apache/causeway/blob/master/LICENSE) | [Apache 2.0](https://github.com/divkit/divkit/blob/main/LICENSE) |

---

## 1. RJSF (react-jsonschema-form)

**Links:** [GitHub](https://github.com/rjsf-team/react-jsonschema-form) — ⭐ 15 800 | [Docs](https://rjsf-team.github.io/react-jsonschema-form/docs/) | [Playground](https://rjsf-team.github.io/react-jsonschema-form/) | [npm](https://www.npmjs.com/package/@rjsf/core) | [Themes](https://github.com/rjsf-team/react-jsonschema-form#supported-themes)

### What is this

React library for building web forms from JSON Schema. The most popular form-generation library for React.

### How it works

```jsx
<Form schema={schema} validator={validator} onSubmit={...} />
```

JSON Schema describes data types and constraints (`type`, `enum`, `minLength`, `pattern`, `format`). `uiSchema` describes appearance (`ui:widget`, `ui:order`, `ui:classNames`). RJSF maps schema types to React components (StringField → `<input>`, BooleanField → `<checkbox>`, NumberField → `<input type="number">`).

**[10 UI theme packages](https://github.com/rjsf-team/react-jsonschema-form#supported-themes):** Bootstrap 3/5, Material UI 7, Ant Design 5, Chakra UI 3, Fluent UI 9, Mantine, Semantic UI 2, Daisy UI 5, Shad CN.

### Key numbers

- [15 800+ stars](https://github.com/rjsf-team/react-jsonschema-form), [2 300+ forks](https://github.com/rjsf-team/react-jsonschema-form/network/members), [2 000+ commits](https://github.com/rjsf-team/react-jsonschema-form/commits/master)
- [280 releases](https://github.com/rjsf-team/react-jsonschema-form/releases), latest [v6.5.2](https://github.com/rjsf-team/react-jsonschema-form/releases/tag/v6.5.2) (May 2026)
- [10+ theme packages](https://github.com/rjsf-team/react-jsonschema-form#supported-themes), [19+ widget types](https://rjsf-team.github.io/react-jsonschema-form/docs/api-reference/widgets), [15+ field types](https://rjsf-team.github.io/react-jsonschema-form/docs/api-reference/field-components)
- Used by: [Mozilla](https://github.com/mozilla), [NASA](https://www.nasa.gov/), [and others](https://github.com/rjsf-team/react-jsonschema-form/network/dependents)

### When to choose RJSF

- Need **forms only** — not a full page layout engine
- Any backend language — just serves JSON Schema + uiSchema
- Want standard JSON Schema — many tools, validators, editors exist
- Small to medium forms without complex layout requirements

### When NOT to choose

- Need full control over layout (Row/Col/Fieldset composition)
- Need complex multi-section pages with tables and custom widgets
- Need server-side validation mapped back to fields
- Need enterprise features: access control, audit trail, i18n

### How servers store UI

```json
{
  "schema": { "type": "object", "properties": { "name": { "type": "string" } } },
  "uiSchema": { "name": { "ui:autofocus": true, "ui:placeholder": "Enter name" } }
}
```

### Key difference from ProjectForge

RJSF is a **client library** — server provides data schema, client renders forms. ProjectForge is a **server framework** — server controls layout structure, data types, actions, validation, and translations. RJSF gives you forms; ProjectForge gives you complete pages.

### Features

- **19 widget types** — checkboxes, radio, select, date, email, password, file upload, color picker, range slider, etc. Mapped from JSON Schema types automatically.
- **Custom widgets** — register `MyCustomWidget` for any `ui:widget` value; full React component swap.
- **Custom fields** — replace entire ObjectField/ArrayField rendering, not just individual inputs.
- **Validation** — AJV-backed (JSON Schema validator); custom validators via `validate()` prop. Errors appear inline below fields.
- **Form data** — `formData` prop is a controlled plain object. `onChange` fires on every keystroke.
- **i18n** — via `translateString` prop or custom templates. No built-in translation file format.
- **Dependencies** — field A can depend on field B via `schema.dependencies` (show/hide, enum update).
- **Templates** — override `ObjectFieldTemplate`, `FieldTemplate`, `ArrayFieldTemplate` for layout customization.

### Weaknesses

- **No layout control** — `uiSchema` only allows ordering and widget choice. No Row/Col/Fieldset grid. Nested layouts require custom templates.
- **No server-driven actions** — buttons are plain `<button type="submit">` elements. No `ResponseAction`, no toast/redirect/modal.
- **Form-only** — no tables, no file attachment manager, no code editor, no progress bars.
- **Client-side validation only** — server validation errors must be manually mapped to fields.
- **No access control** — all fields/buttons visible to everyone. Must implement externally.
- **Theme fragmentation** — 10 themes maintained by different people; feature parity varies. Bootstrap 3 theme is most complete; newer themes (Shad CN) are less mature.
- **Bundle size** — `@rjsf/core` + theme adds ~150 KB gzipped for a form library.

### Comparison with ProjectForge

| Aspect | RJSF | ProjectForge |
|--------|------|-------------|
| **Approach** | Schema → UI (declarative) | Code → UI (imperative DSL) |
| **Layout** | uiSchema ordering | Row/Col/Fieldset Bootstrap grid |
| **Server control** | None — client renders | Server JSON controls everything |
| **Forms** | ✅ Best-in-class | ✅ Full form system |
| **Tables** | ❌ | ✅ AgGrid enterprise |
| **File handling** | ⚠️ Upload widget only | ✅ Full attachment list + drop area |
| **Actions** | Submit button only | ResponseAction: save/delete/redirect/toast/modal |
| **I18n** | Client-side callback | Server-side (I18nResources) |
| **Bundle size** | ~150 KB gzipped | Part of React app (no separate payload) |
| **Ecosystem** | 10 themes, JSON Schema tools | Spring Boot, JPA, 42-language SDKs |

### When to choose RJSF over ProjectForge

- Need **only forms** and already use JSON Schema
- Want theme selection (Material UI, Ant Design, etc.) out of the box
- Any backend (Python, Rust, Go) — just serve JSON Schema + uiSchema
- Standard JSON Schema ecosystem (editors, validators, code generators)
- Quick prototype — `<Form schema={s} />` is 1 line

---

## 2. JSON Forms (EclipseSource)

**Links:** [GitHub](https://github.com/eclipsesource/jsonforms) — ⭐ 1 800 | [Website](https://jsonforms.io/) | [Docs](https://jsonforms.io/docs/) | [Playground](https://jsonforms.io/examples/basic) | [npm](https://www.npmjs.com/package/@jsonforms/core)

### What is this

JSON Schema-based form framework with a separate UI Schema for layout control. Supports React, Angular, and Vue.

### How it works

```json
{
  "schema": { "type": "object", "properties": { "name": { "type": "string" } } },
  "uischema": {
    "type": "VerticalLayout",
    "elements": [
      { "type": "HorizontalLayout", "elements": [
        { "type": "Control", "scope": "#/properties/firstName" },
        { "type": "Control", "scope": "#/properties/lastName" }
      ]}
    ]
  }
}
```

Unlike RJSF, JSON Forms separates **data schema** (what data) from **UI schema** (how to display). UI Schema has first-class layout types: `VerticalLayout`, `HorizontalLayout`, `Group`, `Categorization` (tabs), `Label`.

### Key numbers

- [~1 800 GitHub stars](https://github.com/eclipsesource/jsonforms)
- [Framework bindings](https://jsonforms.io/docs/#frameworks): React, Angular, Vue (2 + 3)
- Maintained by [EclipseSource](https://eclipsesource.com/) (professional support available)
- [v3.7.0](https://github.com/eclipsesource/jsonforms/releases) (latest)

### When to choose JSON Forms

- Need forms with better layout control than RJSF
- Multi-framework support (React + Angular + Vue simultaneously)
- Want standard JSON Schema + dedicated UI Schema
- Tab-based forms via `Categorization`

### When NOT to choose

- Need more than forms (tables, image upload, custom widgets)
- Need server-driven UI — JSON Forms is client-rendered
- Need complex enterprise layouts with tables and grids

### Key difference from ProjectForge

JSON Forms has the **closest layout model** to ProjectForge (VerticalLayout ↔ ProjectForge's Col, HorizontalLayout ↔ Row, Group ↔ Fieldset, Categorization ↔ Tabs). But it's still form-only: no enterprise tables, no action system, no server-side validation mapping, no access control. ProjectForge goes further by making the layout fully server-controlled.

### Features

- **Layout primitives** — `VerticalLayout`, `HorizontalLayout`, `Group`, `Categorization` (tabs with categories). The only form library with first-class layout concepts.
- **Multi-framework** — identical JSON schemas render in React (Material UI), Angular (Material), and Vue (Vuetify). Switch frameworks without changing the JSON.
- **Rule system** — conditional visibility (`rule.condition` + `rule.effect: HIDE/SHOW/ENABLE/DISABLE`). Evaluated client-side on data change.
- **Custom renderers** — register per-UI-schema-element (`"options": {"renderer": "MyCustomControl"}`). Tester function determines which renderer to use.
- **Validation** — JSON Schema validation via AJV. Custom validators per field. Errors displayed inline.
- **Data binding** — `scope: "#/properties/name"` binds UI controls to JSON paths. Supports nested objects and arrays.
- **Enum support** — `oneOf`/`anyOf` JSON Schema keywords auto-render as dropdowns or radio buttons.
- **Professional support** — EclipseSource offers commercial support, training, and custom development.

### Weaknesses

- **Form-only** — no tables, no file management, no action buttons beyond form submission. Same limitations as RJSF.
- **Client-rendered** — the UI schema is evaluated entirely in the browser. Server has no control over visibility or behavior after initial render.
- **Steep learning curve** — separate data schema + UI schema + rule system means three JSON files to understand. Debugging why a control doesn't render requires tracing scope paths.
- **Smaller community** — 1 800 stars vs RJSF's 15 800. Fewer examples, fewer StackOverflow answers.
- **Rule performance** — complex rules (100+ conditions on large forms) evaluated on every data change. No incremental/dirty-checking optimization.
- **No action system** — no toast, redirect, modal. Form submission is a plain HTML POST.

### Comparison with ProjectForge

| Aspect | JSON Forms | ProjectForge |
|--------|-----------|-------------|
| **Layout model** | Vertical/Horizontal/Group/Categorization | Row/Col/Fieldset/Group/Inline |
| **Layout ↔ PFDL** | VerticalLayout ≈ Col, HorizontalLayout ≈ Row, Group ≈ Fieldset | — |
| **Frameworks** | React, Angular, Vue | React only |
| **Server control** | None — UI rendered client-side | Full — JSON from server |
| **Tables** | ❌ | ✅ AgGrid |
| **Actions** | Submit only | ResponseAction: all targets |
| **Tabs** | ✅ Categorization built-in | ⚠️ Via custom components |
| **Rules** | ✅ Built-in conditional engine | ⚠️ watchFields (server round-trip) |
| **File upload** | ❌ | ✅ DropArea + AttachmentList |
| **Enterprise** | Professional support available | Spring Security + JPA + i18n |

### When to choose JSON Forms over ProjectForge

- Need forms with **real layout control** (VerticalLayout, Categorization tabs) — the only library with this
- Multi-framework project — same JSON works in React, Angular, and Vue simultaneously
- Want conditional visibility rules evaluated on the client without server round-trips
- Professional support contract needed (EclipseSource)

---

## 3. Formily (Alibaba)

**Links:** [GitHub](https://github.com/alibaba/formily) — ⭐ 12 500 | [Docs](https://formilyjs.org/) | [Playground](https://formilyjs.org/zh-CN/guide/quick-start) | [npm](https://www.npmjs.com/package/@formily/core)

### What is this

Cross-platform form framework from Alibaba. Designed for high-performance forms with reactive field-level state management.

### How it works

```
JSON Schema → @formily/json-schema → @formily/core → @formily/react → DOM
```

Each field has its own **reactive state** via `@formily/reactive` (Proxy-based). Changing one field does not re-render the entire form — only the changed field updates (O(1) instead of O(n)).

### Key numbers

- [12 500+ stars](https://github.com/alibaba/formily)
- [Framework bindings](https://formilyjs.org/guide/quick-start): React + React Native + Vue 2 + Vue 3
- Built-in visual form builder ([Designable](https://designable-antd.formilyjs.org/))
- Used by: [Alibaba Group](https://www.alibaba.com/) internally

### When to choose Formily

- Very large forms (100+ fields) with complex field dependencies
- Need visual form builder (Designable)
- Need Reactive field-level rendering for performance
- Need cross-framework (React + Vue)

### When NOT to choose

- Server-driven UI (Formily is client-centric)
- Java/Spring backend (no native integration)
- Pages with tables, dashboards, custom layouts — Formily is form-only
- Small team — steep learning curve

### How servers store UI

```json
{
  "type": "object",
  "properties": {
    "name": {
      "type": "string",
      "x-component": "Input",
      "x-validator": { "required": true }
    }
  }
}
```

Formily extends JSON Schema with `x-*` properties (`x-component`, `x-reactions`, `x-validator`, `x-decorator`).

### Key difference from ProjectForge

Formily is the **most technically advanced form library** with reactive rendering, but it's still a client library. Server provides data schema; client handles all rendering. ProjectForge is server-driven: the server sends the complete UI definition, not just a data schema.

### Features

- **Reactive core** — `@formily/reactive` uses JavaScript Proxy to track field-level dependencies. When field A changes, only fields that depend on A re-render. Not the entire form. O(1) renders, not O(n).
- **X-properties** — extends JSON Schema with `x-component` (which React component), `x-decorator` (wrapper), `x-reactions` (dynamic behavior), `x-validator` (validation rules). All in-band in the JSON Schema.
- **Designable** — visual drag-and-drop form builder. Non-developers can design forms. Generates Formily JSON Schema. [Live demo](https://designable-antd.formilyjs.org/).
- **Cross-framework** — identical form schema renders in React (Ant Design), React Native, Vue 2, and Vue 3. Same core, different renderers.
- **Field reactions** — `x-reactions` supports declarative field dependencies: "when field A equals X, set field B to Y, make field C required, hide field D." All in JSON, no code.
- **Validation engine** — `x-validator` supports built-in rules (`required`, `max`, `min`, `pattern`, `email`, `url`) and custom validators. Async validation supported.
- **Array tables** — `ArrayTable`, `ArrayCards`, `ArrayTabs`, `ArrayCollapse` for repeatable form sections. Closest thing to a table in any form library.
- **Schema recursion** — `x-recursion` property enables self-referencing schemas (tree forms, nested repeatable sections).

### Weaknesses

- **Extreme complexity** — `@formily/core` + `@formily/react` + `@formily/antd` + `@formily/reactive` = 4+ packages. Learning curve steeper than any competitor.
- **Chinese-first documentation** — most docs, examples, and Designable UI are in Chinese. English docs exist but are less comprehensive.
- **Alibaba dependency** — developed and maintained by Alibaba. If they deprioritize it, the ecosystem shrinks. Less community ownership than RJSF.
- **Form-only** — despite ArrayTable/ArrayCards, it's still forms. No real data tables (sorting, filtering, pagination), no file attachment manager.
- **Client-centric** — no server-driven UI. Server serves JSON Schema; client decides rendering. No `ResponseAction`, no server-to-client action commands.
- **Bundle size** — reactive core + form renderer + UI library bindings add significant weight (~200+ KB gzipped).

### Comparison with ProjectForge

| Aspect | Formily | ProjectForge |
|--------|---------|-------------|
| **Reactivity** | ✅ Proxy-based field-level | ❌ Full tree re-render |
| **Performance** | O(1) renders per field change | O(n) renders per change |
| **Visual builder** | ✅ Designable (drag-and-drop) | ❌ None |
| **Form complexity** | 100+ field forms, complex deps | Medium forms |
| **Tables** | ⚠️ ArrayTable (repeatable) | ✅ AgGrid enterprise |
| **Server control** | None — client renders | Full — server JSON |
| **Learning curve** | Steep (4 packages, Proxy patterns) | Medium (Kotlin DSL) |
| **Language** | React/Vue only | 42 language SDKs → JSON |
| **Enterprise** | No built-in | Spring Security + JPA + i18n |

### When to choose Formily over ProjectForge

- **150+ field forms** with heavy cross-field dependencies — Formily's reactive engine is unmatched
- Need a **visual form builder** (Designable) for non-developers
- Form-centric application — not pages with tables, dashboards, or file management
- Already using Ant Design ecosystem
- Team has React + Proxy/observable experience

---

## 4. AdminJS

**Links:** [GitHub](https://github.com/SoftwareBrothers/adminjs) — ⭐ 8 900 | [Website](https://adminjs.co/) | [Docs](https://docs.adminjs.co/) | [npm](https://www.npmjs.com/package/adminjs) | [Demo](https://demo.adminjs.co/admin)

### What is this

Node.js admin panel framework that auto-generates CRUD interfaces from ORM models.

### How it works

```typescript
import AdminJS from 'adminjs';
import AdminJSMongoose from '@adminjs/mongoose';

const admin = new AdminJS({
  resources: [
    { resource: UserModel, options: {
      properties: { email: { type: 'string', isTitle: true } },
      listProperties: ['id', 'email', 'name'],
      actions: { edit: { isAccessible: (ctx) => isAdmin(ctx) } },
    }},
  ],
});
```

**[8 database adapters](https://docs.adminjs.co/basics/adapters):** TypeORM, Sequelize, Prisma, MikroORM, Mongoose, Objection, SQL, and a base adapter.

**[6 framework plugins](https://docs.adminjs.co/basics/plugins):** Express, NestJS, Hapi, Koa, Fastify.

### Key numbers

- [8 900+ stars](https://github.com/SoftwareBrothers/adminjs)
- [8 database adapters](https://docs.adminjs.co/basics/adapters), [6 framework plugins](https://docs.adminjs.co/basics/plugins)
- [React-based frontend](https://github.com/SoftwareBrothers/adminjs/tree/master/frontend) with Redux
- Enterprise version available ([AdminJS Pro](https://adminjs.co/pricing))

### When to choose AdminJS

- Node.js/TypeScript stack
- Need a quick admin panel over existing database tables
- CRUD operations cover most of your admin needs
- Want auto-generated list/show/edit/delete views

### When NOT to choose

- Java/Kotlin/Spring stack (Node.js only)
- Need fully custom pages beyond CRUD
- Need server-driven UI — AdminJS generates admin panels, not flexible layouts
- Need tablets, charts, or complex reporting pages

### How server stores UI

```json
{
  "resource": {
    "id": "User",
    "listProperties": ["id", "email", "name"],
    "actions": [{ "name": "list", "icon": "List" }],
    "properties": { "email": { "isTitle": true, "type": "string" } }
  }
}
```

The server returns **metadata** about resources, properties, and actions. The React frontend uses this metadata to render Bootstrap-based CRUD templates.

### Key difference from ProjectForge

AdminJS is a **CRUD generator** for Node.js. It's opinionated (resources → CRUD actions → templates). ProjectForge is a **full SDUI framework** for Java/Kotlin — you build every page explicitly, with complete control over layout and behavior.

### Features

- **Auto-generated CRUD** — point AdminJS at a database model, get list/show/edit/delete views instantly. Zero UI code for standard operations.
- **8 ORM adapters** — TypeORM, Sequelize, Prisma, MikroORM, Mongoose, Objection, SQL (Knex), and a base adapter for custom integrations. Pick your ORM, AdminJS adapts.
- **6 framework plugins** — Express, NestJS, Hapi, Koa, Fastify. Drop into existing Node.js apps.
- **Role-Based Access Control** — `isAccessible`, `isVisible` callbacks per action and per property. Integrates with any auth system (Passport, Auth0, custom JWT).
- **Custom components** — override any part of the admin panel with your own React components: custom pages, custom actions, custom property renderers.
- **Dashboard** — customizable home page with widgets and stats.
- **Theming** — built-in Design System (Bootstrap-based). CSS overrides via `branding.theme` config.
- **Enterprise (AdminJS Pro)** — hosted version with audit logs, advanced RBAC, theming editor, priority support.
- **Validation** — server-side only. Validators defined in resource options (`isRequired`, `isEmail`, custom functions). Errors returned to the React frontend and displayed inline.

### Weaknesses

- **CRUD-only** — admin panels are a solved problem. If your app needs custom workflows, dashboards, or complex multi-step forms, AdminJS doesn't help.
- **Node.js only** — no Java, Python, Go support. If your backend is Spring Boot, you can't use AdminJS.
- **Opinionated UI** — list pages, edit pages, show pages follow a rigid template. Layout customization is limited to property ordering and grouping.
- **No forms library** — doesn't compete with RJSF/Formily for form building. It generates simple forms from model metadata.
- **No tables library** — lists are rendered as basic HTML tables. No AgGrid, no sorting/filtering/pagination beyond what the ORM provides.
- **Server-rendered metadata** — the server sends resource metadata, not a complete UI layout. The React frontend interprets metadata into templates. Less flexible than raw layout JSON.

### Comparison with ProjectForge

| Aspect | AdminJS | ProjectForge |
|--------|---------|-------------|
| **Philosophy** | Auto-generate from models | Explicitly build every page |
| **Speed** | Instant CRUD (zero UI code) | Medium (each page manually) |
| **Custom pages** | ⚠️ Custom components, limited | ✅ Full control via Kotlin DSL |
| **Forms** | Auto-generated from model | Full form system with validation |
| **Tables** | ⚠️ Basic HTML tables | ✅ AgGrid enterprise |
| **Backend** | Node.js/TypeScript only | 42 language SDKs → JSON |
| **ORMs** | 8 adapters | JPA/Hibernate native |
| **Auth** | Callback-based (external) | Spring Security (built-in) |
| **Enterprise** | Pro version ($) | Spring Boot + JPA + i18n |
| **Learning curve** | Low (config-based) | Medium (Kotlin DSL) |

### When to choose AdminJS over ProjectForge

- **Node.js/TypeScript stack** — AdminJS is the best admin panel for the ecosystem
- Need a **quick admin panel** over existing database tables — 10 minutes to working CRUD
- CRUD operations cover 90%+ of admin needs
- Small team — auto-generation means no UI code to maintain
- Want commercial support (AdminJS Pro)

---

## Apache Causeway (formerly Apache Isis)

**Links:** [GitHub](https://github.com/apache/causeway) — ⭐ 890 | [Website](https://causeway.apache.org/) | [Docs](https://causeway.apache.org/docs/) | [Demo](https://demo.causeway.io/)

### What is this

Java framework implementing the **Naked Objects** pattern: the entire UI is auto-generated from domain objects through Java reflection and annotations. No controllers, no templates — just domain objects.

### How it works

```java
@Entity
@DomainObject
public class Person {
    @Property @Title
    public String getName() { ... }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public Person updateName(@Parameter(maxLength = 50) String name) { ... }
}
```

Causeway introspects all `@DomainObject` classes at startup, builds an in-memory metamodel, and renders the UI generically through **Apache Wicket**. Layout can be customized via XML files (`Xxx.layout.xml`).

### Key numbers

- [~890 GitHub stars](https://github.com/apache/causeway), [30 000+ commits](https://github.com/apache/causeway/commits/master)
- [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0), [Apache governance](https://www.apache.org/)
- [Production users](https://causeway.apache.org/community/powered-by/): Irish Department of Social Protection (since 2002), [NTT Data](https://www.nttdata.com/), [A1 Telekom](https://www.a1.group/)
- Two viewers: [Wicket](https://wicket.apache.org/) (HTML) + REST API ([Restful Objects spec](http://restfulobjects.org/))
- Security via [SecMan extension](https://causeway.apache.org/docs/latest/extensions/secman/)

### Closest competitor to ProjectForge

Apache Causeway is the **only direct competitor** in the "Java/Kotlin enterprise SDUI" space. Both solve the same problem: the server controls the UI.

| Aspect | Apache Causeway | ProjectForge |
|--------|----------------|-------------|
| **UI generation** | Automatic, via Java reflection | Explicit, via Kotlin/C/JS DSL |
| **Control** | Through annotations + XML layout | Through code |
| **Flexibility** | Limited by framework conventions | Full — UI = code |
| **Dev speed** | Very fast (write model only) | Medium (each page manually) |
| **UI framework** | Wicket (server-rendered) | React (client-rendered) |
| **Learning curve** | Need DDD + Causeway annotations | Need Kotlin, C, or JS DSL |
| **Enterprise table** | Basic collections | AgGrid (full-featured) |

### When to choose Apache Causeway

- Java/Spring stack, DDD-aligned team
- Domain stable — want UI to update automatically
- Object-action model fits your domain well
- Pixel-perfect UI is not critical
- Small team — Causeway generates complete UIs from domain classes

### When NOT to choose

- Need full control over every page's layout
- UI differs significantly from "object with fields + actions"
- Need complex wizards, multi-step flows, custom dashboards
- Need modern client-rendered UI (React, not Wicket)
- Need enterprise tables (AgGrid, filtering, sorting, export)

### How server stores UI

Causeway stores UI definitions in two forms:

**1. Java annotations:**
```java
@PropertyLayout(fieldSetId = "details", sequence = "1")
@ActionLayout(named = "Update")
```

**2. XML layout files:**
```xml
<bs:row>
  <bs:col span="6">
    <c:fieldSet name="General">
      <c:property id="name"/>
    </c:fieldSet>
  </bs:col>
</bs:row>
```

### Key difference

Causeway follows "domain dictates UI" (Naked Objects pattern). ProjectForge follows "developer builds UI explicitly." Causeway is faster for simple CRUD apps but limited for complex enterprise UIs. ProjectForge gives more control at the cost of more effort per page.

### Features

- **Naked Objects** — the full architectural pattern: write domain objects, UI auto-generates. No controllers, no templates, no DTOs. Just entities + annotations.
- **Apache Wicket viewer** — server-rendered HTML. SEO-friendly. No JavaScript required. Works with disabled JS.
- **Restful Objects viewer** — automatic REST API from domain objects. Full OpenAPI/Swagger generation. Use the same domain model for web and mobile.
- **Metamodel** — all `@DomainObject`, `@Property`, `@Action` annotations are introspected at startup into an in-memory metamodel. Layout, validation, and behavior are derived from this metamodel.
- **Layout customization** — `.layout.xml` files override auto-generated layouts per domain object. XML defines columns, tabs, fieldset groupings.
- **Security (SecMan)** — built-in role-based access control. Users, roles, permissions managed as domain objects themselves.
- **Audit trail** — `CommandLog` captures every action invocation. `ExecutionLog` records timing. Built-in, no configuration needed.
- **i18n** — translations via `.po` files. All UI text (labels, action names, messages) can be translated.
- **Production track record** — Irish Department of Social Protection runs on Causeway since 2002. 20+ years in production government systems.

### Weaknesses

- **UI looks dated** — Wicket renders plain Bootstrap HTML. No modern React/Vue SPA experience. Animations, transitions, live updates require custom JavaScript.
- **Naked Objects constraints** — domain must map naturally to "object + fields + actions" paradigm. Complex multi-step wizards, dashboards, or non-CRUD flows require workarounds.
- **Small community** — 890 stars, Apache governance means slower release cycles. Fewer tutorials, fewer StackOverflow answers.
- **XML layout** — `.layout.xml` files are verbose and error-prone. No type-safety, no IDE autocompletion. Contrast with Kotlin DSL.
- **Wicket dependency** — Wicket is an older, server-rendered framework. Fewer developers know it. Harder to hire for. Contrast with React ecosystem.
- **Basic tables** — object collections render as simple tables. No AgGrid-level sorting, filtering, column pinning, or Excel export. For data-heavy applications this is a significant limitation.
- **Reflection overhead** — metamodel building at startup scans all domain classes. For large applications (200+ entities), startup time increases noticeably.
- **No client-side rendering option** — server-rendered only. No React/Angular/Vue frontend. The Restful Objects viewer is an API, not a UI.

### When to choose Apache Causeway over ProjectForge

- **DDD-aligned team** — if your team thinks in domain objects and annotations, Causeway is a natural fit
- Domain model is **stable** — UI should auto-adapt as domain changes
- Small team that cannot afford dedicated UI developers — Causeway generates complete UIs
- SEO matters — server-rendered HTML without JavaScript dependency
- Government/enterprise that values Apache governance and 20+ year track record

---

## 5. DivKit (Yandex)

**Links:** [GitHub](https://github.com/divkit/divkit) — ⭐ 2 600 | [Website](https://divkit.tech/) | [Docs](https://divkit.tech/doc) | [Playground](https://divkit.tech/playground) | [JSON Schema](https://divkit.tech/schema)

### What is this

Open-source Server-Driven UI framework from Yandex. Describes UI in JSON ("DivJson") and renders natively on iOS, Android, and Web.

### How it works

```json
{
  "card": {
    "states": [{
      "state_id": 0,
      "variables": [{ "name": "count", "type": "integer", "value": 0 }],
      "div": {
        "type": "container",
        "items": [
          { "type": "text", "text": "@{count}" },
          { "type": "input", "text_variable": "count" }
        ]
      }
    }]
  }
}
```

### Key numbers

- [2 600+ stars](https://github.com/divkit/divkit), [185+ releases](https://github.com/divkit/divkit/releases) (latest: [v32.48.0](https://github.com/divkit/divkit/releases/tag/v32.48.0), May 2026)
- [4 700+ commits](https://github.com/divkit/divkit/commits/main)
- [Native clients](https://divkit.tech/doc): [Kotlin](https://github.com/divkit/divkit/tree/main/client/android) (Android), [Swift](https://github.com/divkit/divkit/tree/main/client/ios) (iOS), [TypeScript](https://github.com/divkit/divkit/tree/main/client/web) (Web/React)
- [Server-side JSON builders](https://divkit.tech/doc): TypeScript, Kotlin, Python
- Produced by [Yandex](https://yandex.com/), used in Yandex Browser, Search, Music, Market, Bank, Alice

### Features

- **Containers** — vertical, horizontal, overlap (z‑stack). **Gallery** (image carousel), **grid** (fixed-column layout), **pager** (swipeable pages), **tabs** (top/bottom tab bar), **slider** (horizontal scroll).
- **Variables** — `@{count + 1}`, `@{min(alpha, 0.5)}`, `@{condition ? a : b}`. Arithmetic, string concat, ternary. Evaluated client-side in native code.
- **State machine** — `states[]` array with `state_id` values. Transitions triggered by actions. Each state can have a different `div` tree. Enables multi-screen flows in a single DivJson.
- **Animations** — `fade_in`, `fade_out`, `slide_in`, `slide_out`, `set` transitions between states. Duration, delay, interpolator configurable per transition.
- **Templates** — reusable `"templates": {"userCard": {...}}` block with `$field` parameterization. Instantiate with `"type": "template", "template": "userCard", "args": {"name": "..."}`. Reduces payload size.
- **Actions** — `set_variable`, `set_state`, `clear_focus`, `submit`, `download`. Actions execute on tap, long press, swipe, or programmatically via `@{var}` triggers.
- **Custom components** — register native iOS/Android extensions. DivKit dispatches unknown types to your extension handler. Write platform-specific code once.
- **Text/rich formatting** — `<b>`, `<i>`, `<br>`, links, colors, font sizes, alignment. Inline formatting within a single text element.
- **Images** — network images with placeholder, scale type, aspect ratio, corner radius, tint color. GIF support.
- **Server JSON builders** — TypeScript (`@divkitframework/json-builder`), Kotlin, Python packages for generating DivJson server-side.
- **Ready for A/B testing** — Yandex uses DivKit to serve different UIs to different user segments. The server decides which DivJson to return.

### Weaknesses

- **Mobile-first, web-second** — the Web renderer uses Svelte (not React). Feature parity between native and web is incomplete. Complex animations may not render on web.
- **Limited form inputs** — only `input` (text), `textarea` (multiline), `select` (dropdown). No date picker, no time picker, no autocomplete, no rich text, no file upload, no star rating. Yandex apps pair DivKit with custom native form components.
- **No enterprise tables** — no data grid, no AgGrid, no sorting/filtering/pagination. Gallery and grid are for image/content layout, not tabular data.
- **No validation** — DivKit has no concept of form validation. No required fields, no pattern matching, no error states. Apps must implement validation natively.
- **No i18n** — no built-in translation system. App must handle localization before feeding data to DivJson or use variables.
- **No access control** — all UI elements are visible to everyone. No permission-based show/hide.
- **Yandex governance** — developed by Yandex for Yandex products. Roadmap is driven by their internal needs. Community contributions accepted but priorities are Yandex-first.
- **Complexity ceiling** — state machines + variables + templates + animations make DivJson files hard to reason about at scale. Debugging state transitions requires the playground.
- **JSON payload size** — full DivJson documents can be large (10-50 KB). Templates help compress repeated structures, but initial payloads are heavy.

### Comparison with ProjectForge

| Aspect | DivKit | ProjectForge |
|--------|-------|-------------|
| **Platform** | iOS, Android, Web (Svelte) | Web (React) |
| **Rendering** | Native | React DOM |
| **Templates** | ✅ Built-in with params | ❌ Not yet |
| **Variables** | ✅ `@{var + 1}` expressions | ❌ Not yet |
| **Animations** | ✅ State transitions | ❌ None |
| **Complex forms** | ⚠️ Basic input fields | ✅ Full form system |
| **Tables** | ❌ Gallery/grid only | ✅ AgGrid enterprise |
| **Form inputs** | input, textarea, select | input, select, checkbox, radio, date, time, rating, editor, autocomplete, creatable select, dropdown, file upload, attachment list |
| **Enterprise integration** | ❌ None | ✅ Spring Security, JPA, i18n |
| **Validation** | ❌ None | ✅ Server-side (JPA + Spring) |
| **i18n** | ❌ None | ✅ Server-side (I18nResources) |
| **A/B testing** | ✅ Architecture supports it | ❌ |

### When to choose DivKit over ProjectForge

- **Mobile-first SDUI** for iOS + Android with native rendering — DivKit is unmatched
- Need **server-side A/B testing** — deploy different UIs to different users instantly
- Content-rich apps (cards, feeds, media galleries) — DivKit's layout primitives excel here
- Remote UI updates without App Store review cycles
- Static screens with basic inputs — not complex enterprise forms

### How server stores UI

```json
{
  "templates": { ... },
  "card": {
    "variables": [ ... ],
    "states": [{
      "div": {
        "type": "container",
        "items": [ ... ]
      }
    }]
  }
}
```

Server returns DivJson with the full UI description. Templates are reusable JSON definitions with `$param` substitution, reducing payload size.

### Key difference

DivKit is a **mobile-first SDUI** framework with native rendering on iOS/Android. ProjectForge is a **web-first enterprise SDUI** framework with React on the browser. DivKit excels at animations, templates, and cross-platform mobile. ProjectForge excels at complex forms, enterprise tables, and server integration.

---

## 6. ProjectForge DynamicLayout

**Links:** [GitHub](https://github.com/MaurerAnton/projectforge-dynamiclayout) | [Docs](.) | [Getting Started](https://github.com/MaurerAnton/projectforge-dynamiclayout/blob/master/GETTING_STARTED.md) | [Architecture](architecture.md)

### What is this

Server-Driven UI framework built into the ProjectForge platform. JSON format with **42 SDKs** — from Kotlin (production) and C89 (embedded) to Solidity (EVM) and Fortran (HPC). All produce the same JSON; the React client (`DynamicRenderer`) renders it.

```
42 language SDKs → identical JSON → DynamicRenderer (React) → DOM
```

### How it works

```kotlin
// Kotlin DSL (production, Spring Boot + JPA + i18n)
val layout = UILayout("user.edit")
layout.add(UIFieldset("user.details").add(
  UIInput(id = "name", maxLength = 50),
  UIInput(id = "email", required = true),
))
layout.addAction(UIButton.createSaveButton("save"))
```

```c
// C89 API (embedded, microcontrollers)
#include "dynamiclayout-c89.h"
char buf[8192]; DL b = dl_begin(buf, sizeof(buf), "Device Diagnostics");
dl_fieldset(&b, "Configuration");
dl_input(&b, "name", "Device Name", 1);
dl_button(&b, "save", "Save", "primary", 1);
dl_end(&b);
```

```python
# Python (any backend)
from dynamiclayout import Layout
page = Layout('Feedback')
page.fieldset('Your Details').input('name', 'Name', required=True)
page.input('email', 'Email', required=True).end()
page.button('send', 'Send', 'primary', default=True)
print(page.json())
```

All 42 SDKs produce the same `{type, key, ...}` JSON — the React client doesn't care where it came from.

### Key numbers

- [ProjectForge](https://projectforge.org/) — proprietary enterprise framework
- [30 registered components](https://github.com/MaurerAnton/projectforge-dynamiclayout) in `DynamicRenderer`
- 33 `UIElementType` values, [59 Kotlin UI files](https://github.com/MaurerAnton/projectforge-dynamiclayout/tree/master/projectforge-rest), [90 React components](https://github.com/MaurerAnton/projectforge-dynamiclayout/tree/master/projectforge-webapp)
- [dynamiclayout.h](https://github.com/MaurerAnton/projectforge-dynamiclayout/blob/master/dynamiclayout.h) — single-header C99/C++98 library (300 lines, zero deps) + [41 more SDKs](https://github.com/MaurerAnton/projectforge-dynamiclayout) (Go, Python, Rust, Java, C#, Swift, Zig, Fortran, COBOL, Solidity…)
- [JsLayout](https://github.com/MaurerAnton/projectforge-dynamiclayout/blob/master/playground/index.html) — live JavaScript DSL for playground/prototyping
- Stack: any language → [JSON](https://www.json.org/) → [React](https://react.dev/)
- [GPLv3 license](https://github.com/MaurerAnton/projectforge-dynamiclayout/blob/master/LICENSE)

### When to choose ProjectForge

- **Enterprise Java/Kotlin + Spring Boot** stack
- Need **full SDUI** — server controls layout, data, actions, and validation
- Complex forms + enterprise tables ([AgGrid](https://ag-grid.com/)) + custom page layouts
- Server-side validation and access control ([Spring Security](https://spring.io/projects/spring-security))
- Single stack (Kotlin/Java) for UI definition and business logic

### When NOT to choose

- Need forms only — [RJSF](#1-rjsf-react-jsonschema-form) or [JSON Forms](#2-json-forms-eclipsesource) are simpler
- Node.js/Python/Go backend — any language can [generate JSON](https://github.com/MaurerAnton/projectforge-dynamiclayout) (42 SDKs), but production enterprise features (JPA, Spring Security, i18n) are JVM-only
- Mobile apps — [DivKit](#5-divkit-yandex) handles iOS/Android natively
- SEO required — client-rendered only, no server-side HTML
- Small forms or quick prototypes — overkill for simple use cases

### How server stores UI

```json
{
  "id": "user.edit",
  "version": "1.0",
  "title": "Edit user",
  "content": [{
    "type": "COL",
    "elements": [{
      "type": "FIELDSET",
      "title": "Details",
      "elements": [
        { "type": "INPUT", "id": "name", "maxLength": 50 },
        { "type": "INPUT", "id": "email", "required": true }
      ]
    }]
  }],
  "actions": [{
    "id": "save",
    "responseAction": { "type": "SAVE", "url": "/api/user", "method": "POST" }
  }]
}
```

### Key difference

ProjectForge is the **only framework** in this comparison that combines: full SDUI (server controls everything) + Kotlin/Spring native integration + enterprise table ([AgGrid](https://ag-grid.com/)) + complete form system (30+ UI components) + server-side validation and access control + **42 language SDKs** generating identical JSON.

---

## Detailed Comparison by Criteria

### 1. Server-Driven UI (completeness)

```
Formily       ████░░░░░░░░░░░░░░░░ — form only, client-driven
RJSF          ████░░░░░░░░░░░░░░░░ — form only, no layout control
JSON Forms    ████░░░░░░░░░░░░░░░░ — form only, no actions/dynamic behavior
AdminJS       ██████████░░░░░░░░░░ — CRUD templates only, no custom layout
DivKit        ████████████████████ — pure SDUI, but mobile-focused
Causeway      ████████████████████ — auto-generated UI from domain model
ProjectForge  ████████████████████ — server controls layout, actions, validation, i18n
```

### 2. Form + Table capability

```
RJSF/Formily ████░░░░░░░░░░░░░░░░ — ArrayField/ArrayTable, basic
DivKit       ████░░░░░░░░░░░░░░░░ — gallery/grid only, no data tables
AdminJS      ██████████░░░░░░░░░░ — CRUD lists only, no advanced tables
Causeway     ████████░░░░░░░░░░░░ — basic collection display
Telerik/DevExtreme — licensed products at $1k+/developer
ProjectForge ████████████████████ — full form system + AgGrid enterprise tables
```

### 3. Enterprise features

| Feature | ProjectForge | Causeway | AdminJS | DivKit | RJSF | Formily |
|---------|:------------:|:--------:|:-------:|:------:|:----:|:-------:|
| **Spring Boot** | ✅ Native | ✅ Native | ❌ | ❌ | ❌ | ❌ |
| **JPA/Hibernate** | ✅ Built-in | ✅ Built-in | ❌ | ❌ | ❌ | ❌ |
| **Spring Security** | ✅ Full integration | ⚠️ External | ⚠️ External | ❌ | ❌ | ❌ |
| **Audit trail** | ✅ History per entity | ✅ Command log | ❌ | ❌ | ❌ | ❌ |
| **i18n** | ✅ Server-side | ✅ Built-in | ⚠️ | ❌ | ⚠️ | ✅ |
| **AgGrid** | ✅ Built-in | ❌ | ❌ | ❌ | ❌ | ❌ |
| **SEO** | ❌ Client-only | ✅ Server HTML | ✅ Server HTML | ❌ | ❌ | ❌ |

---

## Weaknesses of ProjectForge DynamicLayout

### 1. No JSON Schema specification

The JSON format is not formally specified. You cannot validate DynamicLayout JSON on the client, generate layouts from other languages (Python, Go, PHP), or write a type-safe client without reading source code. RJSF and DivKit have published JSON Schema specifications.

### 2. No templates

In DivKit, you can define `"templates": { "userCard": { ... } }` once and reuse with `$param` overrides. In ProjectForge, every layout is serialized from scratch. Repeated structures (address forms, contact cards) are duplicated in every JSON response.

### 3. No variables or expressions

DivKit supports `@{count + 1}`, `@{min(alpha, 0.5)}`, `@{condition ? a : b}`. ProjectForge only has `data[id]` on the client. The server cannot describe dynamic behavior without a round-trip (watchFields → server → ResponseAction).

### 4. Full React tree re-render

Each `setData()` call re-renders the entire DynamicLayout tree. There is no field-level reactivity (Formily solves this at the library level). With 150+ fields, the re-render cost is noticeable.

### 5. No layout caching

The same read-only page for 100 users is generated 100 times. Compare with Spring `@Cacheable` support (which is [now added](../issues.md) but not widely applied).

### 6. Tight to JVM ecosystem (production)

The KMP extraction and C API (`dynamiclayout.h`) enable non-JVM JSON generation, but the production-grade Kotlin layer (Spring Boot, JPA, i18n, access control) is JVM-only. You get validation, auto-detection, and security only when running on the JVM.

### 7. Client-side only

Unlike Causeway (server-rendered HTML via Wicket) and AdminJS (SSR-capable), ProjectForge requires JavaScript to render. Empty page without JS, zero SEO, first-load delay while React hydrates.

### 8. React dependency

DynamicRenderer is tied to React. Unlike JSON Forms (React + Angular + Vue) or DivKit (iOS + Android + Web), ProjectForge layouts can only be consumed by React.

---

## Verdict

### When ProjectForge is the best choice

- **Enterprise Java/Kotlin + Spring Boot** stack
- Need **full SDUI** — server controls not just data but layout, actions, and validation
- Complex forms + enterprise tables (AgGrid) + custom page layouts
- Server-side validation and access control built into UI
- Multiple language targets — generate JSON from [42 language SDKs](architecture.md) (Kotlin production, C embedded, Python/Rust/Go backends, Fortran HPC)

### When to consider alternatives

- **RJSF** / **JSON Forms** — if you need only JSON Schema-based forms in React/Angular/Vue
- **Formily** — if you have 100+ field forms with complex reactive dependencies
- **AdminJS** — if your stack is Node.js and you need a quick CRUD admin panel
- **Apache Causeway** — if your domain is stable and you want zero UI code
- **DivKit** — if you need SDUI for iOS + Android mobile apps

### ProjectForge vs Apache Causeway: main choice for Java projects

| Criterion | ProjectForge | Causeway |
|-----------|-------------|----------|
| You want | Control every page explicitly | Auto-generated UI from domain |
| Your domain | Complex, many custom screens | Fits object model naturally |
| UI | React, modern, fully custom | Wicket, Bootstrap, template-like |
| Team size | Resources for UI code | Minimal UI developers needed |
| Change speed | Medium (UI = code) | High (UI = annotations + XML) |
| Frontend tech | React | Wicket (server-rendered HTML) |
| Tables | AgGrid enterprise | Basic object collections |
