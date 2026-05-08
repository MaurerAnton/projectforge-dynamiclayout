# Architecture

## Three layers

```
┌──────────────────────────────────────────────────────────────────────┐
│  LAYER 1: Kotlin DSL (server)                                        │
│                                                                      │
│  AbstractPagesRest                                                   │
│    └─ createEditLayout() / createListLayout()                        │
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

## 1. Kotlin DSL (59 files)

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
├── UISelectTypeSerializer.kt  # Custom UISelect<T> serializer (removed in v2)
├── Serializers.kt             # Domain object serializers (PFUser, Group, Task...)
└── Deserializers.kt           # Deserializers (Int, Long, BigDecimal, Date)

rest/dto/
└── FormLayoutData.kt          # DTO: { data, ui, serverData, variables }
```

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
- Each element in `layout[]` contains at least `type` and `key`
- `type` = one of 30 registered values
- `key` = unique identifier for React (generated as `el-N`)
- Containers (ROW, COL, GROUP, FIELDSET, INLINE_GROUP) have a `content[]` field
- `translations` — flat map of i18n key→value for React components
- `userAccess` determines which buttons are shown on the edit page

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

1. `DynamicLayout` (index.jsx) creates `DynamicLayoutContext.Provider`
2. `renderLayout(layout)` — calls `DynamicRenderer(content)`
3. `DynamicRenderer` iterates over `content[]`, for each element:
   - Gets `type` and looks up component in `components[type]`
   - Passes `{key, ...props}` to the component
4. Container component (ROW/COL/GROUP/FIELDSET/INLINE_GROUP) recursively calls `renderLayout(content)` for children
5. Input components (INPUT/SELECT/CHECKBOX) read data via context and call `setData({[id]: value})` on change

### Component registry (DynamicRenderer.jsx:70-99)

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

**Total: 30 registered types** (33 in UIElementType, 30 have React components; `FILTER_ELEMENT`, `AG_GRID_COLUMN_DEF`, `TABLE_COLUMN` are auxiliary, not rendered directly).

## Data Flow (full cycle)

```
1. User clicks menu item
   → MenuItemDefId.ABOUT.url = "react/about/dynamic"
   → React Router: /react/:category/:type/:id?

2. FormPage.jsx mounts
   → GET /rs/about/dynamic
   → Server: AboutPageRest.getForm() builds UILayout
   → LayoutUtils.process() — keys, translations
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
