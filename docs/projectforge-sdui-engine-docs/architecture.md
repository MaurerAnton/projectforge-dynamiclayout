# Architecture

> **TL;DR:** Three layers — Kotlin DSL on the server defines the UI, JSON carries it over HTTP, and React's `DynamicRenderer` recursively renders it into the DOM. [Open the playground](https://raw.githack.com/MaurerAnton/projectforge-dynamiclayout/master/playground/index.html) and paste any JSON from this doc to see it live.

## Three layers

```
┌──────────────────────────────────────────────────────────────────────┐
│  LAYER 1: Kotlin DSL (server)                                        │
│                                                                      │
│  AbstractPagesRest                                                   │
│    └─ createEditLayout() / getForm()                                 │
│         └─ UILayout, UIRow, UICol, UIInput, UIButton, ...           │
│              └─ UILayout (serialized to JSON via Gson)               │
│                                                                      │
│  LayoutUtils.process(layout)                                         │
│    ├─ addCommonTranslations()                                         │
│    ├─ processAllElements() — keys, translations, auto-detection      │
│    └─ ready UILayout with translations, userAccess, uid              │
└──────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ GET /rs/<category>/dynamic
                                    ▼
┌──────────────────────────────────────────────────────────────────────┐
│  LAYER 2: JSON (transport)                                           │
│                                                                      │
│  {                                                                   │
│    "data": { ... },                                                  │
│    "ui": {                                                           │
│      "title": "address.edit",                                         │
│      "layout": [{ "type": "ROW", "content": [...] }],                │
│      "actions": [{ "type": "BUTTON", "id": "save", ... }],           │
│      "translations": { "save": "Save", "cancel": "Cancel" },         │
│      "userAccess": { "update": true, "delete": false },              │
│      "uid": "layout1234567890"                                       │
│    }                                                                 │
│  }                                                                   │
└──────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ FormPage.jsx fetch → Redux
                                    ▼
┌──────────────────────────────────────────────────────────────────────┐
│  LAYER 3: DynamicRenderer (React)                                    │
│                                                                      │
│  FormPage → DynamicLayout → DynamicRenderer                          │
│    └─ <DynamicLayoutContext.Provider>                                 │
│         ├─ renderLayout(layout) → DynamicRenderer(content)           │
│         │    └─ content.map({type, key, ...props})                    │
│         │         └─ components[type]({...props})                    │
│         │              └─ <DynamicFieldset>, <DynamicInput>, ...    │
│         └─ <DynamicActionGroup> (buttons)                            │
└──────────────────────────────────────────────────────────────────────┘
```

The three-layer diagram above maps exactly to how you build in the playground: you write Layer-2 JSON (left pane) and Layer 3 renders it instantly (right pane). In production, Layer 1 (Kotlin) generates the JSON automatically.

---

## 1. Kotlin DSL (59 files)

The Kotlin DSL is the **programmatic API** for defining UI on the server. Every UI element has a corresponding Kotlin data class. These classes are serialized to JSON via Gson and sent to the client.

> **Playground:** The playground's Kotlin view (top-right dropdown → "Kotlin") shows the Kotlin DSL source that would generate each preset. Paste the JSON into the playground and switch views to see how the same layout looks in Kotlin.

### Directory structure `ui/`

```
ui/
├── UIElementType.kt          # 33 component types
├── UIElement.kt              # Base class (type, key, cssClass)
├── UILayout.kt               # Root layout (title, layout[], actions[], translations)
├── UIRow.kt                  # ROW — container row
├── UICol.kt                  # COL — container column (Bootstrap grid)
├── UIFieldset.kt             # FIELDSET — group with legend
├── UIGroup.kt                # GROUP — element group
├── UIInlineGroup.kt          # INLINE_GROUP — inline elements
├── UILabel.kt                # LABEL — text
├── UIInput.kt                # INPUT — input field
├── UITextArea.kt             # TEXTAREA — multi-line input
├── UICheckbox.kt             # CHECKBOX — checkbox
├── UISelect.kt               # SELECT — dropdown (custom serializer)
├── UICreatableSelect.kt      # CREATABLE_SELECT — select with create
├── UIRadioButton.kt          # RADIOBUTTON — radio button
├── UIRatingStars.kt          # RATING — star rating
├── UIReadOnlyField.kt        # READONLY_FIELD — read-only field
├── UIButton.kt               # BUTTON — button (14 factory methods)
├── UIAlert.kt                # ALERT — notification
├── UIBadge.kt                # BADGE — badge
├── UIBadgeList.kt            # BADGE_LIST — badge list
├── UIProgress.kt             # PROGRESS — progress bar
├── UISpacer.kt               # SPACER — spacer
├── UIDropArea.kt             # DROP_AREA — file upload zone
├── UIEditor.kt               # EDITOR — code editor (Ace)
├── UIList.kt                 # LIST — repeatable item list
├── UINamedContainer.kt       # NAMED_CONTAINER — named container
├── UICustomized.kt           # CUSTOMIZED — custom component (28 sub-types)
├── UITable.kt                # TABLE — table
├── UITableColumn.kt          # TABLE_COLUMN — table column
├── UIAgGrid.kt               # AG_GRID / AG_GRID_LIST_PAGE — AgGrid
├── UIAgGridColumnDef.kt      # AG_GRID_COLUMN_DEF — AgGrid column
├── UIAttachmentList.kt       # ATTACHMENT_LIST — file attachment list
├── UILength.kt               # Bootstrap sizes (xs, sm, md, lg, xl)
├── UIColor.kt                # Bootstrap colors (9 values)
├── UIIconType.kt             # FontAwesome icons
├── UIDataType.kt             # Data types (STRING, DATE, USER, TASK, etc.)
├── ResponseAction.kt         # Action after button press
├── ValidationError.kt        # Validation error
├── LayoutUtils.kt            # Layout post-processing (510 lines)
├── LayoutBuilder.kt          # Row/Col helper
├── ElementsRegistry.kt       # Field auto-detection registry
├── ElementInfo.kt            # Field metadata
├── LayoutContext.kt          # Layout context
├── AutoCompletion.kt         # Auto-completion
├── CssClassnames.kt          # Bootstrap CSS constants
├── UIDataTypeUtils.kt        # Java type → UIDataType mapping
└── filter/                   # Filters for list pages
    ├── UIFilterElement.kt
    ├── UIFilterListElement.kt
    ├── UIFilterTimestampElement.kt
    ├── UIFilterObjectElement.kt
    └── UIFilterBooleanElement.kt
```

### Supporting

```
rest/json/
├── UISelectTypeSerializer.kt  # Custom UISelect<T> serializer
├── Serializers.kt             # Domain object serializers (PFUser, Group, Task...)
└── Deserializers.kt           # Deserializers (Int, Long, BigDecimal, Date)

rest/dto/
└── FormLayoutData.kt          # DTO: { data, ui, serverData, variables }
```

### How Kotlin DSL maps to JSON

Every Kotlin `UI*` class is a Gson-serializable data class. Gson serializes properties — fields that have a `@JsonProperty` annotation or follow the bean convention. Here is the exact mapping for the key classes:

```kotlin
// UILayout — root container
data class UILayout(
    var title: String? = null,         // → "title": "address.edit"
    var content: MutableList<UIElement>? = null,  // → "layout": [...]
    var actions: MutableList<UIButton>? = null,   // → "actions": [...]
    var pageMenu: UIPageMenu? = null,  // → "pageMenu": {...}
)

// UIInput — single input field
data class UIInput(
    var id: String,                    // → "id": "email"
    var label: String? = null,         // → "label": "Email"
    var dataType: UIDataType = STRING, // → "dataType": "STRING"
    var maxLength: Int = 255,          // → "maxLength": 255
    var required: Boolean = false,     // → "required": false
)
```

> **Playground:** Open the playground and load the "All Components" preset. Switch to the "Kotlin" view to see the Kotlin DSL code that would produce that exact JSON layout. You can edit the JSON and observe how each change would affect the Kotlin source.

---

## 2. JSON (transport format)

The server returns `FormLayoutData` — a DTO with four fields:

```json
{
  "data": { ... },
  "ui": {
    "title": "address.edit",
    "layout": [ ... ],
    "actions": [ ... ],
    "layoutBelowActions": [ ... ],
    "pageMenu": [ ... ],
    "namedContainers": [ ... ],
    "translations": { "key": "value", ... },
    "userAccess": { "insert": true, "update": true, "delete": false, "cancel": true },
    "uid": "layout<timestamp>",
    "showHistory": true,
    "watchFields": ["field1", "field2"]
  },
  "serverData": { ... },
  "variables": { ... }
}
```

### Main contracts

- Every element in `layout[]` has at least `type` and `key`
- `type` — one of 30 registered [`UIElementType`](#component-registry) values
- `key` — unique identifier for React's reconciliation (generated as `el-N`)
- Containers (`ROW`, `COL`, `GROUP`, `FIELDSET`, `INLINE_GROUP`) have a `content[]` field with nested children
- `translations` — flat map of i18n key→value. The server fills this via `LayoutUtils.process()`; the client never needs to translate.
- `userAccess` — determines which buttons (`save`/`delete`/`cancel`) are visible in the action bar. Derived from Spring Security permissions.
- `uid` — unique layout identifier; client uses it to detect stale state (e.g. if user navigated away while fetch was in flight).
- `watchFields` — array of field IDs. When any of these fields change value, the client re-fetches the layout from the server. Used for dependent fields (e.g. country → state dropdown).

### JSON Schema of a layout element

```jsonc
{
  "type": "STRING",       // UIElementType enum value
  "key": "string",        // unique, e.g. "el-5"
  "id?": "string",        // data binding id (INPUT, SELECT, CHECKBOX)
  "label?": "string",     // human-readable label
  "content?": [{}],       // nested elements (ROW, COL, FIELDSET, GROUP)
  "dataType?": "STRING",  // UIDataType for INPUT elements
  "required?": false,     // validation hint
  "maxLength?": 255       // validation hint from @Column
  // ... component-specific fields
}
```

> **Playground:** The playground's JSON editor validates your JSON in real-time. Unknown `type` values are rendered as red error spans. Paste the JSON above into the editor and modify `type` to see the error.

---

## 3. DynamicRenderer (React, 90 files)

### Structure

```
dynamicLayout/
├── index.jsx                          # DynamicLayout — context provider
├── context.tsx                        # DynamicLayoutContext TypeScript interface
├── components/
│   ├── DynamicRenderer.jsx            # Component registry + render loop
│   ├── DynamicGroup.jsx               # ROW / COL / GROUP / FRAGMENT
│   ├── DynamicFieldset.jsx            # FIELDSET
│   ├── DynamicInlineGroup.jsx         # INLINE_GROUP
│   ├── DynamicLabel.jsx               # LABEL
│   ├── DynamicButton.jsx              # BUTTON
│   ├── DynamicAlert.jsx               # ALERT
│   ├── DynamicSpacer.jsx              # SPACER
│   ├── DynamicBadge.jsx               # BADGE
│   ├── DynamicBadgeList.jsx           # BADGE_LIST
│   ├── DynamicProgress.jsx            # PROGRESS (progress bar)
│   ├── DynamicList.jsx                # LIST
│   ├── DynamicPageMenu.jsx            # Page menu
│   ├── input/
│   │   ├── DynamicInputResolver.jsx   # INPUT — dispatcher by dataType
│   │   ├── DynamicInput.jsx           # Text/number/password input
│   │   ├── DynamicCheckbox.jsx        # CHECKBOX
│   │   ├── DynamicTextArea.jsx        # TEXTAREA
│   │   ├── DynamicEditor.jsx          # EDITOR (Ace)
│   │   ├── DynamicDateInput.jsx       # DATE
│   │   ├── DynamicTimeInput.jsx       # TIME
│   │   ├── DynamicTimestampInput.jsx  # TIMESTAMP
│   │   ├── DynamicRadioButton.jsx     # RADIOBUTTON
│   │   ├── DynamicRating.jsx          # RATING
│   │   ├── DynamicReadonlyField.jsx   # READONLY_FIELD
│   │   ├── DynamicAutoCompletion.jsx  # Auto-completion
│   │   ├── DynamicDropArea.jsx        # DROP_AREA
│   │   ├── DynamicAttachmentList.jsx  # ATTACHMENT_LIST
│   │   └── DynamicValidationManager.jsx # Validation wrapper
│   ├── select/
│   │   ├── DynamicReactSelect.jsx             # SELECT
│   │   ├── DynamicReactCreatableSelect.jsx    # CREATABLE_SELECT
│   │   ├── DynamicObjectSelect.jsx            # USER/GROUP/EMPLOYEE/COST
│   │   └── task/
│   │       └── index.jsx                      # TASK (task tree)
│   ├── table/
│   │   ├── DynamicTable.jsx           # TABLE
│   │   ├── DynamicTableHead.jsx       # Table header
│   │   ├── DynamicTableRow.jsx        # Table row
│   │   ├── DynamicListPageTable.jsx   # TABLE_LIST_PAGE
│   │   ├── DynamicListPageTableHead.jsx # Sortable header
│   │   ├── DynamicListPageTableRow.jsx # Row with navigation
│   │   ├── DynamicAgGrid.jsx          # AG_GRID
│   │   ├── DynamicListPageAgGrid.jsx  # AG_GRID_LIST_PAGE
│   │   ├── DynamicAgGridDiffCell.jsx  # Diff cell
│   │   ├── DynamicAgGridCustomizedCell.jsx # Customized cell
│   │   ├── ImportStatusCell.jsx       # Import status
│   │   └── MultilineCell.jsx          # Multi-line cell
│   ├── customized/
│   │   └── index.jsx                  # CUSTOMIZED — dispatcher (28 sub-types)
│   │   └── components/                # 38 domain-specific files
│   └── upload/                        # File upload (4 files)
├── action/
│   └── DynamicActionGroup.jsx         # Action button group
```

### How rendering works

1. `DynamicLayout` (index.jsx) creates `DynamicLayoutContext.Provider`, making `data`, `setData`, `ui`, `validationErrors`, and `renderLayout` available to every descendant.
2. `renderLayout(layout)` passes the `content[]` array to `DynamicRenderer`.
3. `DynamicRenderer` iterates over `content[]`, for each element:
   - Reads `type`, looks up the component in `components[type]`
   - Passes `{key, ...props}` to the component
4. Container components (`ROW`, `COL`, `GROUP`, `FIELDSET`, `INLINE_GROUP`) **recursively** call `renderLayout(content)` for their children. This recursion is how deeply nested layouts work.
5. Input components (`INPUT`, `SELECT`, `CHECKBOX`) read their `data[id]` from context and call `setData({[id]: value})` on every change. This triggers a re-render of the entire tree.

> **Playground:** The playground implements the exact same rendering loop. Open it and load "All Components" to see the full component tree rendering. Click on any input field — the playground's `DynamicLayoutContext` manages state exactly like the production code does.

<a id="component-registry"></a>

### Component registry (`DynamicRenderer.jsx:70-99`)

```
ALERT              → DynamicAlert
ATTACHMENT_LIST    → DynamicAttachmentList
BADGE              → DynamicBadge
BADGE_LIST         → DynamicBadgeList
BUTTON             → DynamicButton
CHECKBOX           → DynamicCheckbox
COL                → DynamicGroup (→ <Col>)
CREATABLE_SELECT   → DynamicReactCreatableSelect
CUSTOMIZED         → DynamicCustomized (dispatches by id)
DROP_AREA          → DynamicDropArea
EDITOR             → DynamicEditor
FIELDSET           → DynamicFieldset
FRAGMENT           → DynamicGroup (→ <React.Fragment>)
GROUP              → DynamicGroup (→ <FormGroup>)
INLINE_GROUP       → DynamicInlineGroup
INPUT              → DynamicInputResolver (dispatches by dataType)
LABEL              → DynamicLabel
LIST               → DynamicList
PROGRESS           → CustomizedJobsMonitor
RADIOBUTTON        → DynamicRadioButton
RATING             → DynamicRating
READONLY_FIELD     → DynamicReadonlyField
ROW                → DynamicGroup (→ <Row>)
SELECT             → DynamicReactSelect
SPACER             → DynamicSpacer
TABLE              → DynamicTable
TABLE_LIST_PAGE    → DynamicListPageTable
AG_GRID            → DynamicAgGrid
AG_GRID_LIST_PAGE  → DynamicListPageAgGrid
TEXTAREA           → DynamicTextArea
```

**Total: 30 registered types** (33 in `UIElementType` enum, 30 have React components; `FILTER_ELEMENT`, `AG_GRID_COLUMN_DEF`, `TABLE_COLUMN` are auxiliary — used inside other components but never rendered directly).

> **Playground:** The playground registers the same components. Open it and type `"type": "AG_GRID"` to see the AgGrid render. Type an unknown type like `"type": "FOO"` to see the red error span — this is identical to production error handling.

### DynamicInputResolver — dispatching by dataType

The `INPUT` type is a **dispatcher**: the component rendered depends on `dataType`. This allows a single `UIInput(id="startDate", dataType=DATE)` to automatically render a date picker — the server never explicitly says "use a date picker."

```
INPUT (type) + dataType → selected component:
  STRING       → DynamicInput (text)
  INTEGER      → DynamicInput (number)
  LONG         → DynamicInput (number)
  BIG_DECIMAL  → DynamicInput (number with decimal)
  PASSWORD     → DynamicInput (type="password")
  DATE         → DynamicDateInput
  TIME         → DynamicTimeInput
  TIMESTAMP    → DynamicTimestampInput
  BOOLEAN      → DynamicCheckbox (auto)
  STRING_ARRAY → DynamicAutoCompletion
```

> **Playground:** Load the "All Components" preset. Find the `INPUT` element with `"dataType": "DATE"` — the playground renders a date input. Change `dataType` to `"PASSWORD"` and observe the input switching to `type="password"` instantly.

### Validation flow

1. **Server side:** `LayoutUtils.process()` reads JPA annotations (`@Column(nullable=false)`, `@Size(max=50)`) and sets `required`/`maxLength`/`pattern` on each `UIInput`. These are serialized into the JSON.
2. **Client side:** `DynamicValidationManager` wraps every input component. When the user submits, the client sends changed values to the server.
3. **Server validates:** Spring Boot validates the entity. Validation errors are returned as `ResponseAction` with `validationErrors[]`.
4. **Client binds:** `DynamicValidationManager` matches `validationErrors[].fieldId` to `id` on each input, displays the error tooltip below the field.

```
User types email              Server-side JPA validation
        │                              │
        ▼                              ▼
DynamicInput → setData({email:"b@d"}) → POST /rs/user/save
                                              │
                                              ▼
                              @Column(unique=true) → "Email already used"
                                              │
                                              ▼
                        ResponseAction { validationErrors: [
                          { fieldId: "email", message: "Email already used" }
                        ]}
                                              │
                                              ▼
                        DynamicValidationManager → error below <input>
```

> **Playground:** The playground implements validation visualization. The "All Components" preset includes fields with `required: true` — try submitting with empty fields to see error spans. The playground doesn't call a real server so validation happens against hardcoded rules, but the rendering is identical.

### ResponseAction — server-driven behavior

After a button press, the server returns a `ResponseAction` that tells the client what to do next. The client never decides the next step — the server does.

| Target type | Effect | Example |
|-------------|--------|---------|
| `UPDATE_DATA` | Update `data` in context, re-render | After save, show updated entity |
| `REDIRECT` | Navigate to a URL | After delete, go to list page |
| `TOAST` | Show a toast notification | "Saved successfully" |
| `MODAL` | Open a modal dialog | Confirmation dialogs |
| `RELOAD` | Re-fetch the current layout | After a dependent field changes |
| `CUSTOM` | Execute custom client code | Download file, open new tab |

```json
// Button definition (server-generated)
{
  "type": "BUTTON",
  "id": "save",
  "label": "Save",
  "responseAction": {
    "type": "SAVE",
    "url": "/rs/user/save",
    "method": "POST",
    "targetType": "UPDATE_DATA"
  },
  "color": "primary"
}
```

> **Playground:** The playground buttons simulate `ResponseAction`. Click "Save" in any preset — the playground renders a toast. Click "Cancel" — the playground simulates a redirect. All button behavior is driven by the `responseAction` in JSON.

### Bootstrap grid layout

The layout system is built on Bootstrap's responsive grid:

```
ROW (UIRow)          — <Row> wrapper
  └── COL (UICol)    — <Col xs|sm|md|lg|xl={...}>, takes optional `length`
       └── FIELDSET   — <fieldset> with <legend>
            └── INPUT — individual form field
```

- `UILength` enum: `XS`, `SM`, `MD`, `LG`, `XL`, `AUTO` — maps to Bootstrap breakpoints
- A `ROW` can contain multiple `COL`s; each `COL` can have its own children
- `FIELDSET` groups related fields visually with a legend header
- `INLINE_GROUP` places children side-by-side without column wrapping

```kotlin
// Two-column form row
layout.add(UIRow()
    .add(UICol(length = UILength(SM, 6)).add(UIInput("name")))
    .add(UICol(length = UILength(SM, 6)).add(UIInput("email")))
)
```

Generates:
```json
{
  "type": "ROW",
  "content": [
    { "type": "COL", "content": [{ "type": "INPUT", "id": "name" }] },
    { "type": "COL", "content": [{ "type": "INPUT", "id": "email" }] }
  ]
}
```

> **Playground:** Paste the JSON above into the playground. You'll see two input fields side by side. Change the second `COL` to a nested `FIELD`/`FIELDSET` to see how nesting works. The playground renders Bootstrap classes exactly like production.

### Watch fields — server round-trip on field change

`watchFields` enables dynamic form behavior: when a user changes field A, the form re-fetches from the server to update field B's options.

```
"watchFields": ["country"]
       │
       ▼
User changes "country" → setData({country: "DE"})
       │
       ▼
FormPage detects change → POST /rs/address/dynamic?country=DE
       │
       ▼
Server rebuilds layout → state dropdown now shows German states
       │
       ▼
Client replaces layout — only state field updates visually
```

> **Playground:** The playground does not support watch fields (no server round-trip available), but the concept is demonstrated in the "All Components" preset — look for fields marked with validation dependencies.

---

## Data Flow (full cycle)

```
1. User clicks menu item
   → MenuItemDefId.ABOUT.url = "react/about/dynamic"
   → React Router: /react/:category/:type/:id?

2. FormPage.jsx mounts
   → GET /rs/about/dynamic
   → Server: AboutPageRest.getForm() builds UILayout
   → LayoutUtils.process() — keys, translations, auto-detection
   → Returns FormLayoutData { data: null, ui: { layout, actions, translations } }

3. FormPage → <DynamicLayout ui={ui}>
   → DynamicLayoutContext.Provider
   → renderLayout(layout) = DynamicRenderer(content)
   → DynamicRenderer: [{type:"FIELDSET", key:"el-1", ...}]
     → components["FIELDSET"]({key:"el-1", ...})
     → <DynamicFieldset>
       → renderLayout(content)
       → [{type:"LABEL", key:"el-2", label:"Hello"}]
         → <DynamicLabel label="Hello" />

4. User clicks button
   → DynamicButton → callAction(props)
   → POST /rs/<category>/<action>
   → Server processes, returns ResponseAction
   → Client: redirect, update data, show toast, etc.
```

> **Playground:** The playground follows the same flow starting from step 3 — it has `DynamicLayout`, `DynamicLayoutContext`, `renderLayout`, and `DynamicRenderer`. The only difference: no server round-trip (step 1–2 and 4 are simulated in the playground's JavaScript).

---

## Key design decisions

### 1. Zero business logic on the client

The React client never decides anything. It renders whatever JSON the server sends. There are no if-conditions, no routing logic, no permission checks in JavaScript. This is the core SDUI principle:

- Server decides **what** to show → JSON `layout[]`
- Client decides **how** to show it → React components

### 2. Server-side i18n

Translations are resolved on the server and sent as a flat `translations` map in the JSON. The client never knows what language the user is in — it just reads `translations[button.label]`. This means:

- Translation files stay on the server (`I18nResources.properties`)
- No client-side translation bundles needed
- New languages added without client code changes

### 3. String-based type dispatch

The entire rendering is driven by a string-to-component map. There are no switch statements, no if-chains. Adding a new component means:
1. Add to `UIElementType` enum (Kotlin)
2. Add to `components[type]` registry (React/playground)

This is why the playground can render the same JSON as production — it uses the same `type` dispatch pattern.

### 4. Auto-detection from JPA

`ElementsRegistry.kt` introspects JPA entity classes at startup and builds a map of every field's metadata:

```kotlin
// For each entity field annotated with @PropertyInfo:
registry["User.email"] = ElementInfo(
    dataType = UIDataType.STRING,
    maxLength = 100,           // from @Column(length=100)
    required = true,            // from @Column(nullable=false)
    translatedLabel = "email",  // from I18nResources
)
```

When building a layout, developers call `ElementsRegistry.get("User.email")` to auto-fill `UIInput` properties. This is what `createEditLayout()` uses — it creates a complete edit form from JPA metadata with zero manual field configuration.

---

## Summary

| Concept | Server (Kotlin) | Transport (JSON) | Client (React) |
|---------|----------------|------------------|----------------|
| **Layout** | `UILayout`, `UIRow`, `UICol`, `UIFieldset` | `layout[]` array of elements | `DynamicRenderer` + container components |
| **Form fields** | `UIInput(id, dataType, required, maxLength)` | `{type:"INPUT", id:"email", dataType:"STRING"}` | `DynamicInputResolver` dispatches by dataType |
| **Actions** | `UIButton(id, responseAction)` | `{type:"BUTTON", id:"save", responseAction:{...}}` | `DynamicButton` → `callAction()` |
| **Validation** | `ValidationError(fieldId, message)` from Spring | `validationErrors[]` in response | `DynamicValidationManager` binds to fields |
| **i18n** | `I18nHelper.translate(key)` | `translations: {"key":"Value"}` | Read from context, never translate |
| **Access** | Spring Security → `userAccess{}` map | `userAccess: {update:true, delete:false}` | Show/hide buttons |
| **Data** | Entity objects (JPA) | `data: {id:..., email:...}` | `DynamicLayoutContext → data[id]` |
