# Pipeline: from Kotlin DSL to React Render

## Complete Processing Flow

```
Kotlin DSL                     JSON (Gson)                  React
─────────────────      ──────────────────────      ─────────────────────
UILayout()             →  { "title": "...",       →  <DynamicLayout>
  .add(UIRow())              "layout": [                renderLayout()
  .add(UIInput())                 { "type":... }            → DynamicRenderer()
  ...                       ],                               → components[type]
LayoutUtils.process()       "actions": [],                    → <DynamicFieldset>
  → keys "el-1"             "translations": {...},             → <DynamicInput>
  → translations             "userAccess": {...}                  → <Input>
  → auto-detection        }
```

## Step 1. Building UILayout (AbstractPagesRest)

Each REST controller extends `AbstractPagesRest` and overrides `createEditLayout()` or `createListLayout()`.

```kotlin
class AboutPageRest : AbstractDynamicPageRest() {
    @GetMapping("dynamic")
    fun getForm(request: HttpServletRequest): FormLayoutData {
        val layout = UILayout("about.title")

        layout.add(UIFieldset(title = "'About ProjectForge")
            .add(UILabel("'ProjectForge v8.2"))
            .add(UILabel("'License: GPLv3"))
        )

        LayoutUtils.process(layout)
        return FormLayoutData(null, layout, createServerData(request))
    }
}
```

When using `AbstractPagesRest.createEditLayout()`, `LayoutUtils.processEditPage()` is also called — it adds Save, Cancel, Delete buttons based on user permissions.

## Step 2. LayoutUtils.process() — Post-Processing

File: `LayoutUtils.kt` (510 lines)

### 2.1. addCommonTranslations()

Adds base i18n keys to `layout.translations`:
```
"calendar.today", "cancel", "finish", "save", "select.placeholder", "yes"
```

### 2.2. processAllElements()

Recursively traverses all elements in `layout.layout`, `layout.actions`, `layout.namedContainers`, `layoutBelowActions`.

For each UIElement:

#### a) Assigning keys (key)
```kotlin
element.key = "el-${++counter}"  // el-1, el-2, el-3, ...
```

#### b) i18n key translation
For each `UILabelledElement`:
- If `label == null` → looks for `@PropertyInfo` annotation on the field and gets `i18nKey`
- If `label != null` and starts with `'` → takes text as-is (literal)
- If `label != null` without `'` → calls `translate(label)` via `I18nHelper`
- Same for `additionalLabel` and `tooltip`

Similarly for:
- `UIFieldset.title`
- `UITableColumn.title`
- `UIAgGridColumnDef.headerName`
- `UIAlert.title` / `message`
- `UIButton.title` (if null — auto-lookup by id: "save" → "Save", "cancel" → "Cancel")

#### c) Additional translations
- For `UIInput` with `dataType == TASK`: adds task translations
- For `UIAttachmentList`: adds attachment translations

### 2.3. Named containers

`nc-1`, `nc-2`, ... — keys for `layout.namedContainers`.

### 2.4. processEditPage() — Adding Buttons

Called after `process()` if this is an edit page. Adds buttons based on `layout.userAccess`:

```
userAccess.cancel != false   → Cancel     (POST /rs/.../cancel)
userAccess.update == true    → Save       (PUT /rs/.../save)
userAccess.insert == true    → Create     (PUT /rs/.../save)
userAccess.history == true   → Show "History" tab
userAccess.delete == true    → Mark as Deleted / Force Delete / Delete
isDeleted()                  → Undelete
cloneSupported               → Clone
```

### 2.5. processListPage() — List Page Buttons

Adds `Reset` and `Search` buttons for list pages. Restores AgGrid column preferences from the database (`agGridSupport.restoreColumnsFromUserPref`).

## Step 3. JSON Serialization (Gson)

Library: **Gson** (Google JSON).

### Normal Serialization
Most classes are `data class`: fields are automatically serialized in camelCase.

### @Transient / @JsonIgnore
Fields excluded from JSON:
- `layoutContext` — JPA metadata reference (only needed server-side)
- `ignoreAdditionalLabel` / `ignoreTooltip` — flags
- `reference` — UIElement reference

### Custom Serializers

**UISelectTypeSerializer** — for `UISelect<T>`:
- Manually writes fields: `id`, `type`, `key`, `required`, `multi`, `label`, `values[]`, `favorites[]`, `autoCompletion`
- Needed because `values` is a generic `List<UISelectValue<T>>` and Gson cannot correctly infer the type

**Domain serializers** — for domain objects:
- `PFUserDOSerializer` → `{id, displayName, username}`
- `GroupDOSerializer` → `{id, displayName, name}`
- `TaskDOSerializer` → `{id, displayName, title}`
- And others

### Enum Serialization
- `UIColor`: `@JsonProperty("danger")` → `"danger"`, `"primary"`, `"info"`, etc.
- `UIIconType`: `@JsonValue` on `icon: Array<String>` → `["fas", "check"]`
- `UIInput.AutoCompleteType`: `@JsonValue` on `htmlName` → `"username"`, `"current-password"`

### Result JSON

```json
{
  "data": { "id": 42, "name": "John", "email": "john@example.com" },
  "ui": {
    "title": "address.edit",
    "uid": "layout1712345678901",
    "layout": [ { "type": "ROW", "key": "el-1", "content": [...] } ],
    "actions": [ { "type": "BUTTON", "key": "el-5", "id": "save", "title": "Save" } ],
    "translations": { "cancel": "Cancel", "save": "Save" },
    "userAccess": { "update": true, "delete": false },
    "showHistory": true,
    "watchFields": ["name"]
  },
  "serverData": { "i18n": { "locale": "en" } }
}
```

## Step 4. FormPage — Fetching Data (React)

File: `FormPage.jsx`

```jsx
function FormPage() {
    const { type, category: currentCategory, id, tab } = useParams();
    const { data, isFetching, ui, validationErrors, variables } = category;

    useEffect(() => {
        onNewFormPage(currentCategory, id, getServiceURL(
            `${currentCategory}/${type || 'dynamic'}`,
            { id, ...searchParams }
        ));
    }, [currentCategory, id]);

    if (ui === undefined || ui.title === undefined)
        return <LoadingContainer loading />;

    return <DynamicLayout callAction={onCallAction} data={data} ui={ui}
        validationErrors={validationErrors} variables={variables} />;
}
```

## Step 5. DynamicRenderer — Recursive Rendering

File: `DynamicRenderer.jsx`

```jsx
const components = {};

export const registerComponent = (type, tag) => {
    components[type] = tag;
};

function DynamicRenderer(content) {
    return content.map(({ type, key, ...props }) => {
        const Tag = components[type];
        if (!Tag) return <span key={key}>Unknown type: {type}</span>;
        return <Tag key={key} type={type} {...props} />;
    });
}
```

Container components (ROW, COL, GROUP, FIELDSET, INLINE_GROUP) recursively call `renderLayout(content)` for their children.

## Step 6. Action Handling (callAction)

When a user clicks a button:

1. `DynamicButton` calls `callAction(props)` from context
2. This triggers `POST /rs/<category>/<actionId>`
3. Server processes the request and returns `ResponseAction`
4. Client handles `ResponseAction.targetType`:
   - `REDIRECT` → navigate to URL
   - `MODAL` → open modal
   - `CLOSE_MODAL` → close modal
   - `TOAST` → show toast message
   - `UPDATE` → refresh current page data
   - `DOWNLOAD` → download file
   - `NOTHING` → do nothing
   - `RELOAD` → reload page
5. `validationErrors` in the response are displayed on the corresponding fields
6. `variables` in the response update the variable state

## Complete File Map

| Step | File | Function |
|------|------|----------|
| 1 | `AbstractPagesRest.kt` | Base REST controller for CRUD pages |
| 1 | `*PageRest.kt` (e.g., `AddressPagesRest.kt`) | Concrete page: creates UILayout |
| 2 | `LayoutUtils.kt` | process(), processEditPage(), processListPage() |
| 2 | `ElementsRegistry.kt` | Auto-detection from JPA/@PropertyInfo |
| 3 | Gson (spring-boot-starter-json) | JSON serialization |
| 3 | `UISelectTypeSerializer.kt` | Custom UISelect serializer |
| 4 | `FormPage.jsx` | Fetch + Redux + DynamicLayout |
| 4 | `form.js` (actions) | loadFormPage, callAction |
| 4 | `categories.js` (reducer) | form.categories state |
| 5 | `DynamicLayout/index.jsx` | Context Provider |
| 5 | `DynamicRenderer.jsx` | Recursive rendering |
| 6 | `form.js` (actions) → callAction | POST /rs/.../actionId |
| 6 | `ResponseAction.kt` | Response handling |
