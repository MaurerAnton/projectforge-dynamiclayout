# API Reference — UI-components DynamicLayout

## Legend

- **Kotlin class** — server class extending `UIElement`
- **React component** — client component registered in `DynamicRenderer`
- **JSON fields** — fields serialized to JSON (excluding `@Transient`)
- `'literal text` — prefix `'` in Kotlin makes LayoutUtils skip i18n translation

---

## 1. Containers (Layout Containers)

### ROW

Horizontal row (Bootstrap `Row`).

| Aspect | Value |
|--------|----------|
| Kotlin class | `UIRow` — `data class extends UIElement(ROW)` |
| React component | `DynamicGroup` → `<Row>` (reactstrap) |
| Recursive | ✅ `content[]` |

**JSON fields:**
```json
{
 "type": "ROW",
 "key": "el-1",
 "content": [ /* UIElement[] */ ]
}
```

---

### COL

Grid column (Bootstrap `Col`). Nested inside `ROW`.

| Aspect | Value |
|--------|----------|
| Kotlin class | `UICol` — `open class extends UIElement(COL)` |
| React component | `DynamicGroup` → `<Col>` (reactstrap) |
| Recursive | ✅ `content[]` |

**JSON fields:**
```json
{
 "type": "COL",
 "key": "el-2",
 "length": { "xs": 12, "md": 6, "lg": 4 },
 "offset": { "md": 2 },
 "content": [ /* UIElement[] */ ],
 "collapseTitle": "Section Title" // optional: collapsible block
}
```

**UILength:**
| Field | Type | Description |
|------|-----|----------|
| `xs` | `Int?` | Bootstrap col-xs (1-12) |
| `sm` | `Int?` | Bootstrap col-sm |
| `md` | `Int?` | Bootstrap col-md |
| `lg` | `Int?` | Bootstrap col-lg |
| `xl` | `Int?` | Bootstrap col-xl |

---

### FIELDSET

Group legend (`<fieldset><legend>`). Extends `UICol`, supports collapsing.

| Aspect | Value |
|--------|----------|
| Kotlin class | `UIFieldset` — `open class extends UICol(type=FIELDSET)` |
| React component | `DynamicFieldset` |
| Recursive | ✅ `content[]` |

**JSON fields:**
```json
{
 "type": "FIELDSET",
 "key": "el-3",
 "title": "Personal Data",
 "collapsed": false,
 "length": { "md": 12 },
 "content": [ /* UIElement[] */ ]
}
```

| Field | Type | Description |
|------|-----|----------|
| `title` | `String?` | Legend text. i18n-translated. |
| `collapsed` | `Boolean?` | `null` = not collapsible; `true` = collapsed by default; `false` = exped |
| `length` | `UILength?` | Column width |
| `offset` | `UILength?` | Column offset |
| `content` | `UIElement[]` | Child elements |

---

### GROUP

Element group (usually a Label+Input pair). Rendered as `FormGroup row`.

| Aspect | Value |
|--------|----------|
| Kotlin class | `UIGroup` — `data class extends UIElement(GROUP)` |
| React component | `DynamicGroup` → `<FormGroup row={true}>` |
| Recursive | ✅ `content[]` |

**JSON fields:**
```json
{
 "type": "GROUP",
 "key": "el-4",
 "content": [ /* UIElement[] */ ]
}
```

---

### INLINE_GROUP

Elements arranged in one line (inline-flex, no wrapping).

| Aspect | Value |
|--------|----------|
| Kotlin class | `UIInlineGroup` — `data class extends UIElement(INLINE_GROUP)` |
| React component | `DynamicInlineGroup` |
| Recursive | ✅ `content[]` |

**JSON fields:**
```json
{
 "type": "INLINE_GROUP",
 "key": "el-5",
 "content": [ /* UIElement[] */ ]
}
```

---

### FRAGMENT

Fragment out DOM wrapper (`React.Fragment`). Used inside GROUP/ROW for grouping out extra div.

| Aspect | Value |
|--------|----------|
| Kotlin class | no direct analogue (used in UIGroup) |
| React component | `DynamicGroup` → `<React.Fragment>` |
| Recursive | ✅ `content[]` |

---

### NAMED_CONTAINER

Named container accessible by `id`. Used for filters, additional sections.

| Aspect | Value |
|--------|----------|
| Kotlin class | `UINamedContainer` — `data class extends UIElement(NAMED_CONTAINER)` |
| React component | not directly — content is rendered in the layout |
| Recursive | ✅ `content[]` |

**JSON fields:**
```json
{
 "type": "NAMED_CONTAINER",
 "key": "nc-1",
 "id": "filterSettings",
 "content": [ /* UIElement[] */ ]
}
```

---

### LIST

Repeatable list. Each element gets its own data scope variable `elementVar`.

| Aspect | Value |
|--------|----------|
| Kotlin class | `UIList` — `data class extends UIElement(LIST)` |
| React component | `DynamicList` |
| Recursive | ✅ `content[]` |

**JSON fields:**
```json
{
 "type": "LIST",
 "key": "el-6",
 "listId": "positions",
 "elementVar": "pos",
 "positionLabel": "Position",
 "content": [ /* UIElement[] template for each element */ ]
}
```

| Field | Type | Description |
|------|-----|----------|
| `listId` | `String` | Path in data: `data.listId` |
| `elementVar` | `String` | Element variable name (id prefix) |
| `positionLabel` | `String` | Label before number (i18n) |
| `content` | `UIElement[]` | Template for each list element list |

---

## 2. Input Elements (Input Elements)

### INPUT

Input field. Determines type via `dataType`.

| Aspect | Value |
|--------|----------|
| Kotlin class | `UIInput` — `data class extends UIElement(INPUT), UILabelledElement, IUIId` |
| React component | `DynamicInputResolver` → dispatcher by `dataType` |
| dataType SUB-dispatch | see table below |

**JSON fields:**
```json
{
 "type": "INPUT",
 "key": "el-7",
 "id": "email",
 "dataType": "STRING",
 "label": "E-mail",
 "required": true,
 "maxLength": 255,
 "focus": true,
 "autoComplete": "EMAIL",
 "inputMode": null,
 "pattern": null,
 "color": null,
 "additionalLabel": null,
 "tooltip": "Enter your email address"
}
```

| Field | Type | Description |
|------|-----|----------|
| `id` | `String` | Data key: `data.id` |
| `dataType` | `UIDataType` | Data type (defines sub-component) |
| `label` | `String?` | Label. i18n or `'literal` |
| `required` | `Boolean?` | Required field |
| `maxLength` | `Int?` | Maximum length (from `@Column`) |
| `focus` | `Boolean?` | Auto-focus |
| `color` | `UIColor?` | Color (danger, success, etc.) |
| `autoComplete` | `AutoCompleteType?` | Browser autocomplete: `USERNAME`, `CURRENT_PASSWORD`, `NEW_PASSWORD`, `OFF`, `ONE_TIME_CODE` |
| `inputMode` | `InputMode?` | HTML inputMode: `NUMERIC` |
| `pattern` | `String?` | HTML pattern for validation |
| `autoCompletionUrl` | `String?` | Auto-completion URL (STRING + text) |
| `autoCompletionUrlParams` | `Map<String,String>?` | Auto-completion URL parameters |
| `additionalLabel` | `String?` | Additional label |
| `tooltip` | `String?` | Tooltip |

**UIDataType — React dispatch:**

| dataType | React component | Description |
|----------|-----------------|----------|
| `STRING` | `DynamicInput` / `DynamicAutoCompletion` | Text (auto-completion if `autoCompletionUrl` is set) |
| `DATE` | `DynamicDateInput` | Date picker |
| `TIMESTAMP` | `DynamicTimestampInput` | Date time picker |
| `TIME` | `DynamicTimeInput` | Time picker |
| `INT` / `LONG` | `DynamicInput` (type=number) | Integer |
| `DECIMAL` / `NUMBER` / `AMOUNT` | `DynamicInput` (type=number) | Decimal |
| `PASSWORD` | `DynamicInput` (type=password) | Password |
| `TASK` | `DynamicTaskSelect` | Task selector (tree) |
| `USER` | `DynamicObjectSelect` | User selector |
| `GROUP` | `DynamicObjectSelect` | Group selector |
| `EMPLOYEE` | `DynamicObjectSelect` | Employee selector |
| `COST1` | `DynamicObjectSelect` | Kost1 selector |
| `COST2` | `DynamicObjectSelect` | Kost2 selector |
| `KONTO` | `DynamicObjectSelect` | Account selector |
| `BOOLEAN` | (via `UICheckbox`) | (handled separately) |

---

### TEXTAREA

Multi-line text input.

| Aspect | Value |
|--------|----------|
| Kotlin class | `UITextArea` — `data class extends UIElement(TEXTAREA), UILabelledElement, IUIId` |
| React component | `DynamicTextArea` |

**JSON fields:**
```json
{
 "type": "TEXTAREA",
 "key": "el-8",
 "id": "description",
 "label": "Description",
 "maxLength": 1000,
 "rows": 3,
 "maxRows": 10,
 "tooltip": "Enter description"
}
```

| Field | Type | Description |
|------|-----|----------|
| `id` | `String` | Data key |
| `label` | `String?` | Label |
| `maxLength` | `Int?` | Maximum length |
| `rows` | `Int?` | Number of rows (default 3) |
| `maxRows` | `Int?` | Maximum rows (default 10) |
| `additionalLabel` | `String?` | Additional label |
| `tooltip` | `String?` | Tooltip |

---

### CHECKBOX

Checkbox.

| Aspect | Value |
|--------|----------|
| Kotlin class | `UICheckbox` — `data class extends UIElement(CHECKBOX), UILabelledElement` |
| React component | `DynamicCheckbox` |

**JSON fields:**
```json
{
 "type": "CHECKBOX",
 "key": "el-9",
 "id": "active",
 "label": "Active",
 "color": "success",
 "inline": false,
 "tooltip": "Mark as active"
}
```

| Field | Type | Description |
|------|-----|----------|
| `id` | `String` | Data key |
| `label` | `String?` | Label |
| `color` | `UIColor?` | Color |
| `inline` | `Boolean` | Inline-display (default false) |
| `tooltip` | `String?` | Tooltip |

---

### SELECT

Dropdown list (react-select).

| Aspect | Value |
|--------|----------|
| Kotlin class | `UISelect<T>` — `extends UIElement(SELECT), UILabelledElement, IUIId` |
| React component | `DynamicReactSelect` |
| Serializer | `@JsonSerialize(using = UISelectTypeSerializer::class)` — custom |

**JSON fields:**
```json
{
 "type": "SELECT",
 "key": "el-10",
 "id": "country",
 "label": "Country",
 "required": true,
 "multi": false,
 "valueProperty": "code",
 "labelProperty": "name",
 "values": [
 { "id": "us", "displayName": "United States" },
 { "id": "de", "displayName": "Germany" }
 ],
 "favorites": [
 { "id": "de", "name": "Germany" }
 ],
 "autoCompletion": {
 "type": "CUSTOMER",
 "url": "/rs/customer/autocomplete",
 "urlparams": { "search": "searchTerm" },
 "minChars": 2
 }
}
```

| Field | Type | Description |
|------|-----|----------|
| `id` | `String` | Data key |
| `label` | `String?` | Label |
| `required` | `Boolean?` | Required |
| `multi` | `Boolean?` | Multi-select |
| `values` | `UISelectValue[]` | Options list: `{id, displayName}` |
| `valueProperty` | `String` | Value field (default "id") |
| `labelProperty` | `String` | Display field (default "displayName") |
| `favorites` | `Favorite[]` | Favorites: `{id, name}` |
| `autoCompletion` | `AutoCompletion` | Auto-completion config |

**AutoCompletion:**
```json
{
 "type": "USER",
 "url": "/rs/user/autocomplete",
 "urlparams": { "q": "search" },
 "minChars": 2,
 "values": [ { "value": 1, "label": "John Doe" } ]
}
```

---

### CREATABLE_SELECT

Select with create-new capability (react-creatable-select).

| Aspect | Value |
|--------|----------|
| Kotlin class | `UICreatableSelect` — `extends UIElement(CREATABLE_SELECT), UILabelledElement` |
| React component | `DynamicReactCreatableSelect` |

**JSON fields:**
```json
{
 "type": "CREATABLE_SELECT",
 "key": "el-11",
 "id": "tags",
 "label": "Tags",
 "required": false
}
```

| Field | Type | Description |
|------|-----|----------|
| `id` | `String` | Data key |
| `label` | `String?` | Label |
| `required` | `Boolean?` | Required |

---

### RADIOBUTTON

Radio button. Each button is a separate element sharing a `name`..

| Aspect | Value |
|--------|----------|
| Kotlin class | `UIRadioButton` — `data class extends UIElement(RADIOBUTTON), UILabelledElement` |
| React component | `DynamicRadioButton` |

**JSON fields:**
```json
{
 "type": "RADIOBUTTON",
 "key": "el-12",
 "id": "gender",
 "name": "gender",
 "value": "male",
 "label": "Male"
}
```

| Field | Type | Description |
|------|-----|----------|
| `id` | `String` | Data key |
| `name` | `String` | Group name (default = id) |
| `value` | `Any` | Value of this option |
| `label` | `String?` | Label |
| `tooltip` | `String?` | Tooltip |

---

### RATING

Star rating.

| Aspect | Value |
|--------|----------|
| Kotlin class | `UIRatingStars` — `data class extends UIElement(RATING), UILabelledElement` |
| React component | `DynamicRating` |

**JSON fields:**
```json
{
 "type": "RATING",
 "key": "el-13",
 "id": "satisfaction",
 "label": "Satisfaction",
 "values": ["1", "2", "3", "4", "5"]
}
```

| Field | Type | Description |
|------|-----|----------|
| `id` | `String` | Data key |
| `label` | `String?` | Label |
| `values` | `String[]` | Options (array size = number of stars) |
| `tooltip` | `String?` | Tooltip |

---

### READONLY_FIELD

Read-only display field (not editable).

| Aspect | Value |
|--------|----------|
| Kotlin class | `UIReadOnlyField` — `data class extends UIElement(READONLY_FIELD), UILabelledElement` |
| React component | `DynamicReadonlyField` |

**JSON fields:**
```json
{
 "type": "READONLY_FIELD",
 "key": "el-14",
 "id": "createdAt",
 "label": "Created",
 "dataType": "TIMESTAMP",
 "canCopy": true,
 "coverUp": false,
 "value": "2024-01-15 10:30"
}
```

| Field | Type | Description |
|------|-----|----------|
| `id` | `String?` | Data key |
| `dataType` | `UIDataType` | Formatting type |
| `label` | `String?` | Label |
| `canCopy` | `Boolean?` | Show copy button |
| `coverUp` | `Boolean?` | Mask (for passwords) |
| `value` | `String?` | Fixed value (if not from data) |
| `tooltip` | `String?` | Tooltip |

---

## 3. Display Elements (Display Elements)

### LABEL

Text label.

| Aspect | Value |
|--------|----------|
| Kotlin class | `UILabel` — `data class extends UIElement(LABEL), UILabelledElement` |
| React component | `DynamicLabel` |

**JSON fields:**
```json
{
 "type": "LABEL",
 "key": "el-15",
 "label": "Full Name",
 "labelFor": "fullName",
 "dataType": "STRING",
 "tooltip": "Enter your full name"
}
```

| Field | Type | Description |
|------|-----|----------|
| `label` | `String?` | Text. i18n key or `'literal` |
| `labelFor` | `String?` | Which field this label is for |
| `dataType` | `UIDataType?` | Data type |
| `tooltip` | `String?` | Tooltip |

---

### ALERT

Notification (Bootstrap Alert). Supports icon, markdown.

| Aspect | Value |
|--------|----------|
| Kotlin class | `UIAlert` — `data class extends UIElement(ALERT)` |
| React component | `DynamicAlert` |

**JSON fields:**
```json
{
 "type": "ALERT",
 "key": "el-16",
 "message": "Operation completed successfully",
 "title": "Success",
 "color": "success",
 "id": "errorMessage",
 "markdown": false,
 "icon": ["fas", "check"]
}
```

| Field | Type | Description |
|------|-----|----------|
| `message` | `String?` | Message text (i18n). If `id` is specified, reads from `data[id]` |
| `title` | `String?` | Title |
| `color` | `UIColor?` | Color: `danger`, `info`, `success`, `warning`, etc. |
| `id` | `String?` | Data key (overrides `message`) |
| `markdown` | `Boolean?` | Render as Markdown |
| `icon` | `String[2]?` | FontAwesome: `["fas", "info-circle"]` |

---

### BADGE

Badge (Bootstrap Badge).

| Aspect | Value |
|--------|----------|
| Kotlin class | `UIBadge` — `data class extends UIElement(BADGE)` |
| React component | `DynamicBadge` |

**JSON fields:**
```json
{
 "type": "BADGE",
 "key": "el-17",
 "title": "New",
 "color": "danger",
 "pill": true
}
```

| Field | Type | Description |
|------|-----|----------|
| `title` | `String?` | Text |
| `color` | `UIColor?` | Color |
| `pill` | `Boolean` | Pill shape (default true) |

---

### BADGE_LIST

Badge list.

| Aspect | Value |
|--------|----------|
| Kotlin class | `UIBadgeList` — `extends UIElement(BADGE_LIST)` |
| React component | `DynamicBadgeList` |

**JSON fields:**
```json
{
 "type": "BADGE_LIST",
 "key": "el-18",
 "badgeList": [
 { "title": "Active", "color": "success", "pill": true },
 { "title": "Verified", "color": "info", "pill": true }
 ]
}
```

---

### PROGRESS

Progress bar.

| Aspect | Value |
|--------|----------|
| Kotlin class | `UIProgress` — `data class extends UIElement(PROGRESS), IUIId` |
| React component | `DynamicProgress` (used inside `CustomizedJobsMonitor`) |

**JSON fields:**
```json
{
 "type": "PROGRESS",
 "key": "el-19",
 "id": "jobProgress",
 "title": "Processing...",
 "value": 45,
 "color": "primary",
 "info": "45% completed",
 "infoColor": "info",
 "animated": true,
 "cancelConfirmMessage": "Cancel this job?"
}
```

| Field | Type | Description |
|------|-----|----------|
| `id` | `String` | Data key for state |
| `title` | `String?` | Title |
| `value` | `Int?` | Percentage (0-100) |
| `color` | `UIColor?` | Bar color |
| `info` | `String?` | Info text under bar (markdown) |
| `infoColor` | `UIColor?` | Color info |
| `cancelConfirmMessage` | `String?` | Cancel confirmation |
| `animated` | `Boolean?` | Animation |

---

### SPACER

Spacer.

| Aspect | Value |
|--------|----------|
| Kotlin class | `UISpacer` — `data class extends UIElement(SPACER)` |
| React component | `DynamicSpacer` |

**JSON fields:**
```json
{
 "type": "SPACER",
 "key": "el-20",
 "width": 2
}
```

| Field | Type | Description |
|------|-----|----------|
| `width` | `Int?` | Width in em (default 1) |

---

## 4. Button Actions

### BUTTON

Button action.

| Aspect | Value |
|--------|----------|
| Kotlin class | `UIButton` — `extends UIElement(BUTTON)`. Private constructor, 14 factory methods |
| React component | `DynamicButton` |

**JSON fields:**
```json
{
 "type": "BUTTON",
 "key": "el-21",
 "id": "save",
 "title": "Save",
 "color": "primary",
 "outline": false,
 "default": true,
 "disabled": false,
 "tooltip": "Save changes",
 "confirmMessage": "Are you sure?",
 "responseAction": {
 "url": "/rs/address/save",
 "targetType": "POST"
 }
}
```

| Field | Type | Description |
|------|-----|----------|
| `id` | `String` | Identifier (defines action on the server) |
| `title` | `String?` | Button text. If null — auto-lookup by id |
| `color` | `UIColor?` | Bootstrap color |
| `outline` | `Boolean?` | Outline-only |
| `default` | `Boolean?` | Default button (Enter) |
| `disabled` | `Boolean?` | Disabled |
| `tooltip` | `String?` | Tooltip |
| `confirmMessage` | `String?` | Confirmation before action (i18n) |
| `responseAction` | `ResponseAction` | URL, method and action type |

**Factory methods UIButton (Kotlin):**

| Method | id | Color | Type |
|-------|----|------|-----|
| `createCancelButton()` | `cancel` | `SECONDARY` | Cancel |
| `createSaveButton()` | `save` | `PRIMARY` | Save |
| `createUpdateButton()` | `update` | `PRIMARY` | Save |
| `createCreateButton()` | `create` | `PRIMARY` | Save |
| `createDeleteButton()` | `deleteIt` | `DANGER` | Delete |
| `createForceDeleteButton()` | `forceDelete` | `DANGER` | ForceDelete |
| `createMarkAsDeletedButton()` | `markAsDeleted` | `DANGER` | MarkAsDeleted |
| `createUndeleteButton()` | `undelete` | `WARNING` | Undelete |
| `createCloneButton()` | `clone` | `INFO` | Clone |
| `createResetButton()` | `reset` | `SECONDARY` | FilterReset |
| `createSearchButton()` | `search` | `PRIMARY` | List |
| `createBackButton()` | - | `SECONDARY` | Back |
| `createDangerButton()` | - | `DANGER` | - |
| `createPrimaryButton()` | - | `PRIMARY` | - |
| `createSecondaryButton()` | - | `SECONDARY` | - |
| `createLinkButton()` | - | `LINK` | - |
| `createDownloadButton()` | - | `INFO` | - |
| `createExportButton()` | - | `SUCCESS` | - |
| `createAddButton()` | - | `SUCCESS` | - |

**ResponseAction.targetType:**

| TargetType | Client action |
|------------|------------------|
| `REDIRECT` | Navigate to `url` |
| `MODAL` | Open modal window |
| `CLOSE_MODAL` | Close modal window |
| `TOAST` | Show toast notification |
| `UPDATE` | Update current page data |
| `DOWNLOAD` | download file |
| `NOTHING` | Do nothing |
| `RELOAD` | Reload page |
| `PUT` / `POST` / `DELETE` | HTTP-request |
| `GET` | HTTP GET |
| `CHECK_AUTHENTICATION` | Check authentication |

---

## 5. Table Elements

### TABLE

Simple table.

| Aspect | Value |
|--------|----------|
| Kotlin class | `UITable` — `open class extends UIElement(TABLE)` |
| React component | `DynamicTable` |

**JSON fields:**
```json
{
 "type": "TABLE",
 "key": "el-22",
 "id": "items",
 "columns": [
 { "id": "name", "title": "Name", "sortable": true },
 { "id": "amount", "title": "Amount", "dataType": "DECIMAL" }
 ],
 "rowClickPostUrl": "/rs/item/{id}",
 "refreshUrl": "/rs/item/refresh",
 "refreshIntervalSeconds": 30
}
```

---

### TABLE_LIST_PAGE

Table on list page (with row navigation).

| Aspect | Value |
|--------|----------|
| Kotlin class | `UITable` `listPageTable = true` → `type = TABLE_LIST_PAGE` |
| React component | `DynamicListPageTable` |

JSON structure is similar to TABLE. Difference: row click leads to `list.categories[].stardEditPage`.

---

### TABLE_COLUMN

Table column.

| Aspect | Value |
|--------|----------|
| Kotlin class | `UITableColumn` — `data class extends UIElement(TABLE_COLUMN)` |

**JSON fields:**
```json
{
 "type": "TABLE_COLUMN",
 "key": "el-23",
 "id": "fullName",
 "title": "Full Name",
 "titleIcon": ["fas", "user"],
 "dataType": "STRING",
 "sortable": true,
 "formatter": "USER"
}
```

**Available formatters:** `ADDRESS_BOOK`, `AUFTRAG_POSITION`, `DATE`, `EMPLOYEE`, `COST1`, `COST2`, `CUSTOMER`, `GROUP`, `KONTO`, `PROJECT`, `RATING`, `TASK_PATH`, `TIMESTAMP_MINUTES`, `USER`

---

### AG_GRID

Enterprise table using AgGrid.

| Aspect | Value |
|--------|----------|
| Kotlin class | `UIAgGrid` — `open class extends UIElement(AG_GRID)` |
| React component | `DynamicAgGrid` |

**JSON fields (main):**
```json
{
 "type": "AG_GRID",
 "key": "el-24",
 "id": "users",
 "columnDefs": [
 { "field": "name", "headerName": "Name", "sortable": true, "filter": "agTextColumnFilter" }
 ],
 "rowSelection": { "mode": "multiRow", "enableClickSelection": true },
 "pagination": true,
 "paginationPageSize": 25,
 "rowClickRedirectUrl": "/react/user/edit/{id}",
 "height": "500px"
}
```

---

### AG_GRID_LIST_PAGE

AgGrid on list page bulk action buttons.

| Aspect | Value |
|--------|----------|
| Kotlin class | `UIAgGrid` `listPageTable = true` → `type = AG_GRID_LIST_PAGE` |
| React component | `DynamicListPageAgGrid` |

---

### AG_GRID_COLUMN_DEF

AgGrid column.

| Aspect | Value |
|--------|----------|
| Kotlin class | `UIAgGridColumnDef` — `open class` (NOT a subclass of UIElement) |

**JSON fields (main):**
```json
{
 "field": "name",
 "headerName": "Name",
 "headerTooltip": "User name",
 "sortable": true,
 "filter": "agTextColumnFilter",
 "width": 150,
 "minWidth": 80,
 "maxWidth": 300,
 "cellRenderer": "formatter",
 "valueFormatter": "USER",
 "pinned": "left",
 "hide": false,
 "wrapText": false
}
```

---

## 6. Specialized Elements

### ATTACHMENT_LIST

Attachment manager (upload, download, delete).

| Aspect | Value |
|--------|----------|
| Kotlin class | `UIAttachmentList` — `extends UIElement(ATTACHMENT_LIST)` |
| React component | `DynamicAttachmentList` |

**JSON fields:**
```json
{
 "type": "ATTACHMENT_LIST",
 "key": "el-25",
 "category": "address",
 "id": 42,
 "listId": "attachments",
 "maxSizeInKB": 10000,
 "readOnly": false,
 "uploadDisabled": false,
 "downloadOnRowClick": true,
 "showExpiryInfo": true,
 "serviceBaseUrl": "/react/attachment/dynamic",
 "restBaseUrl": "/rs/attachments"
}
```

---

### DROP_AREA

Drag-and-drop file upload area.

| Aspect | Value |
|--------|----------|
| Kotlin class | `UIDropArea` — `data class extends UIElement(DROP_AREA)` |
| React component | `DynamicDropArea` |

**JSON fields:**
```json
{
 "type": "DROP_AREA",
 "key": "el-26",
 "title": "Drop files here",
 "uploadUrl": "/rs/attachment/upload",
 "tooltip": "Drag & drop files",
 "id": "dropArea"
}
```

---

### EDITOR

Code editor (Ace).

| Aspect | Value |
|--------|----------|
| Kotlin class | `UIEditor` — `extends UIElement(EDITOR)` |
| React component | `DynamicEditor` |

**JSON fields:**
```json
{
 "type": "EDITOR",
 "key": "el-27",
 "id": "scriptContent",
 "mode": "kotlin",
 "height": "600px"
}
```

| Field | Type | Description |
|------|-----|----------|
| `id` | `String` | Data key |
| `mode` | `String` | Mode: `kotlin`, `groovy` (default kotlin) |
| `height` | `String?` | Height (default 600px) |

---

### CUSTOMIZED

Custom domain component. `id` determines which React component renders.

| Aspect | Value |
|--------|----------|
| Kotlin class | `UICustomized` — `data class extends UIElement(CUSTOMIZED)` |
| React component | `DynamicCustomized` — dispatcher by `id` |

**JSON fields:**
```json
{
 "type": "CUSTOMIZED",
 "key": "el-28",
 "id": "color-chooser",
 "values": { "colors": ["#ff0000", "#00ff00"] }
}
```

**28 registered CUSTOMIZED id:**

| id | React-component | Purpose |
|----|----------------|------------|
| `access.table` | `AccessTableComponent` | Access control table |
| `address.edit.image` | `CustomizedAddressImage` | Address image editor |
| `address.imagePreview` | `CustomizedImageDataPreview` | Image preview |
| `address.import.dialogWrapper` | `AddressImportDialogWrapper` | Address import |
| `address.phoneNumbers` | `CustomizedAddressPhoneNumbers` | Address phones |
| `address.phoneNumber` | `CustomizedAddressPhoneNumber` | Single phone |
| `address.textParser` | `AddressImportReconciler` | Address text parsing |
| `email` | `CustomizedEMail` | Email-component |
| `color-chooser` | `CustomizedColorChooser` | Color chooser |
| `book.lendOutComponent` | `BookLendOut` | Book lending |
| `calendar.recurrency` | `CalendarEventRecurrency` | Event recurrence |
| `calendar.reminder` | `CalendarEventReminder` | Event reminder |
| `calendar.editExternalSubscription` | `CalendarEditExternalSubscription` | External subscription |
| `calendar.subscriptionInfo` | `CalendarSubscriptionInfo` | Subscription info |
| `cost.number` | `CostNumberComponent` | Cost center number |
| `cost.number24` | `CostNumber24Component` | Cost center number (24) |
| `dayRange` | `DayRange` | Day range picker |
| `image` | `CustomizedImage` | Image |
| `invoice.incomingPosition` | `IncomingInvoicePositionsComponent` | Incoming invoice positions |
| `invoice.outgoingPosition` | `OutgoingInvoicePositionsComponent` | Outgoing invoice positions |
| `jira.issuesLinks` | `JiraIssuesLinks` | JIRA issue links |
| `jobs.monitor` | `CustomizedJobsMonitor` | Job monitor with progress bar |
| `task.consumption` | `CustomizedConsumptionBar` | Consumption bar task  |
| `timesheet.edit.taskAndKost2` | `TimesheetEditTaskAndKost2` | Task + Kost2 selector |
| `timesheet.edit.templatesAndRecent` | `TimesheetTemplatesAndRecent` | Timesheet templates |
| `vacation.entries` | `VacationTable` | Vacation table |
| `vacation.statistics` | `VacationStatistics` | Vacation statistics |
| `webauthn.authenticate` | `WebAuthnAuthenticate` | WebAuthn authentication |
| `webauthn.register` | `WebAuthnRegister` | WebAuthn registration |

---

## 7. Auxiliary Types

### FILTER_ELEMENT

Filter element for list pages. Not rendered directly — used in layout for filter configuration..

| Aspect | Value |
|--------|----------|
| Kotlin class | `UIFilterElement` (abstract), subclasses: `UIFilterListElement`, `UIFilterTimestampElement`, `UIFilterObjectElement`, `UIFilterBooleanElement` |
| React component | No direct component (filters processed via AgGrid filterModel) |

**JSON fields:**
```json
{
 "type": "FILTER_ELEMENT",
 "key": "el-29",
 "id": "status",
 "filterType": "LIST",
 "label": "Status",
 "defaultFilter": true,
 "values": [ { "id": "active", "displayName": "Active" } ],
 "multi": true
}
```

**FilterType:** `STRING`, `DATE`, `TIMESTAMP`, `BOOLEAN`, `OBJECT`, `LIST`

---

## 8. Auxiliary enums (Enums)

### UIColor

| JSON value | Bootstrap class |
|------------|-----------------|
| `"danger"` | danger (red) |
| `"dark"` | dark (dark) |
| `"info"` | info (blue) |
| `"light"` | light (light) |
| `"link"` | link (link) |
| `"primary"` | primary (blue) |
| `"secondary"` | secondary (gray) |
| `"success"` | success (green) |
| `"warning"` | warning (yellow) |

### UIIconType

| JSON value | FontAwesome |
|------------|-------------|
| `["fas", "check"]` | fa-check |
| `["fas", "info"]` | fa-info |
| `["fas", "paperclip"]` | fa-paperclip |
| `["far", "star"]` | fa-star (regular) |
| `["fas", "times"]` | fa-times |
| `["fas", "user-lock"]` | fa-user-lock |

### UIDataType

`STRING`, `AMOUNT`, `BOOLEAN`, `COST1`, `COST2`, `CUSTOMIZED`, `DATE`, `DECIMAL`, `EMPLOYEE`, `GROUP`, `INT`, `KONTO`, `LOCALE`, `LONG`, `PASSWORD`, `PICTURE`, `STRING`, `TASK`, `TIME`, `TIMESTAMP`, `TIMEZONE`, `USER`

---

## 9. Root Object: UILayout

```json
{
 "title": "page.title",
 "uid": "layout<timestamp>",
 "layout": [ UIElement, ... ],
 "actions": [ UIElement, ... ],
 "layoutBelowActions": [ UIElement, ... ],
 "pageMenu": [ MenuItem, ... ],
 "namedContainers": [ UINamedContainer, ... ],
 "translations": { "key": "value", ... },
 "userAccess": {
 "history": true,
 "insert": false,
 "update": true,
 "delete": false,
 "cancel": true,
 "editHistoryComments": false
 },
 "showHistory": true,
 "hideSearchFilter": false,
 "excelExportSupported": false,
 "multiSelectionSupported": false,
 "watchFields": ["field1", "field2"],
 "historyBackButton": null,
 "restBaseUrl": null
}
```
