# Real-World DynamicLayout Components

These are the actual React components from the ProjectForge codebase that render DynamicLayout JSON. Each component is registered in `DynamicRenderer.jsx` by `type` string.

---

## DynamicRenderer — The Core Engine

**File:** `DynamicRenderer.jsx`

The central registry and render loop. Maps JSON `type` to React component:

```jsx
const components = {};

export const registerComponent = (type, tag) => {
    components[type] = tag;
};

export default function DynamicRenderer(content) {
    if (!content) return null;

    return content.map(({ type, key, ...props }) => {
        const Tag = components[type];
        if (!Tag) {
            return <span key={key}>Type {type} is not implemented.</span>;
        }
        return <Tag key={key} type={type} {...props} />;
    });
}
```

**29 registered types:**

| type string | React Component | Import Source |
|-------------|----------------|---------------|
| `ALERT` | `DynamicAlert` | `./DynamicAlert` |
| `ATTACHMENT_LIST` | `DynamicAttachmentList` | `./input/DynamicAttachmentList` |
| `BADGE` | `DynamicBadge` | `./DynamicBadge` |
| `BADGE_LIST` | `DynamicBadgeList` | `./DynamicBadgeList` |
| `BUTTON` | `DynamicButton` | `./DynamicButton` |
| `CHECKBOX` | `DynamicCheckbox` | `./input/DynamicCheckbox` |
| `COL` | `DynamicGroup` | `./DynamicGroup` |
| `CREATABLE_SELECT` | `DynamicReactCreatableSelect` | `./select/DynamicReactCreatableSelect` |
| `CUSTOMIZED` | `DynamicCustomized` | `./customized` |
| `DROP_AREA` | `DynamicDropArea` | `./input/DynamicDropArea` |
| `EDITOR` | `DynamicEditor` | `./input/DynamicEditor` |
| `FIELDSET` | `DynamicFieldset` | `./DynamicFieldset` |
| `FRAGMENT` | `DynamicGroup` | `./DynamicGroup` |
| `GROUP` | `DynamicGroup` | `./DynamicGroup` |
| `INLINE_GROUP` | `DynamicInlineGroup` | `./DynamicInlineGroup` |
| `INPUT` | `DynamicInputResolver` | `./input/DynamicInputResolver` |
| `LABEL` | `DynamicLabel` | `./DynamicLabel` |
| `LIST` | `DynamicList` | `./DynamicList` |
| `PROGRESS` | `CustomizedJobsMonitor` | `./customized/components/CustomizedJobsMonitor` |
| `RADIOBUTTON` | `DynamicRadioButton` | `./input/DynamicRadioButton` |
| `RATING` | `DynamicRating` | `./input/DynamicRating` |
| `READONLY_FIELD` | `DynamicReadonlyField` | `./input/DynamicReadonlyField` |
| `ROW` | `DynamicGroup` | `./DynamicGroup` |
| `SELECT` | `DynamicReactSelect` | `./select/DynamicReactSelect` |
| `SPACER` | `DynamicSpacer` | `./DynamicSpacer` |
| `TABLE` | `DynamicTable` | `./table/DynamicTable` |
| `TABLE_LIST_PAGE` | `DynamicListPageTable` | `./table/DynamicListPageTable` |
| `AG_GRID` | `DynamicAgGrid` | `./table/DynamicAgGrid` |
| `AG_GRID_LIST_PAGE` | `DynamicListPageAgGrid` | `./table/DynamicListPageAgGrid` |
| `TEXTAREA` | `DynamicTextArea` | `./input/DynamicTextArea` |

---

## DynamicLayout Context

**File:** `context.tsx`

The context provides `data`, `setData`, `validationErrors`, `renderLayout`, `callAction` to every DynamicLayout component. All components read their values from this context.

```typescript
interface DynamicLayout {
    data: Record<string, any>;
    setData: (update: Record<string, any>) => void;
    validationErrors: Array<{ fieldId?: string; message?: string }>;
    renderLayout: (content: LayoutElement[]) => React.ReactNode;
    callAction: (action: ActionDefinition) => void;
    ui: UILayout;
    variables: Record<string, any>;
    setVariables: (vars: Record<string, any>) => void;
    isFetching: boolean;
}
```

---

## Component Examples

### DynamicLabel

**File:** `DynamicLabel.jsx`

Renders a `<Label>` with optional tooltip via `UncontrolledTooltip`.

```jsx
import { UncontrolledTooltip } from 'reactstrap';
import { Label } from '../../../design';
import TooltipIcon from '../../../design/TooltipIcon';

function DynamicLabel({ label, tooltip }) {
    const id = tooltip ? String.idify(label) : undefined;
    return (
        <Label className="ui-label" id={id}>
            {label}
            {tooltip && (
                <>
                    <TooltipIcon />
                    <UncontrolledTooltip placement="auto" target={id}>
                        {tooltip}
                    </UncontrolledTooltip>
                </>
            )}
        </Label>
    );
}
```

**JSON input:**
```json
{ "type": "LABEL", "key": "el-1", "label": "Name", "tooltip": "Enter your name" }
```

---

### DynamicButton

**File:** `DynamicButton.jsx`

The most complex component. Handles confirmation modals, disabled state during fetch, tooltips, and `callAction` dispatch.

```jsx
function DynamicButton(props) {
    const {
        confirmMessage, id, title, tooltip, disabled,
        default: isDefault = false, responseAction = {},
        handleButtonClick, ...stylingProps
    } = props;

    const [showConfirmMessage, setShowConfirmMessage] = React.useState(false);
    const { callAction, ui, isFetching } = React.useContext(DynamicLayoutContext);

    const handleClick = (event) => {
        event.preventDefault();
        event.stopPropagation();
        if (confirmMessage) { setShowConfirmMessage(true); return; }
        if (handleButtonClick) { handleButtonClick(); return; }
        callAction(props);
    };

    // Confirmation modal with Enter key support
    // Disabled when `isFetching` or `disabled` prop is set

    return (
        <>
            {confirmMessage && (
                <Modal isOpen={showConfirmMessage} toggle={toggleShowConfirmMessage}>
                    <ModalHeader toggle={toggleShowConfirmMessage} />
                    <ModalBody>{confirmMessage}</ModalBody>
                    <ModalFooter>
                        <Button onClick={toggleShowConfirmMessage}>{ui.translations.cancel}</Button>
                        <Button onClick={confirmAction}>{ui.translations.yes}</Button>
                    </ModalFooter>
                </Modal>
            )}
            <Button {...stylingProps} onClick={handleClick} type={type}
                    disabled={disabled || isFetching}>
                <span id={buttonId}>{title}</span>
            </Button>
            {tooltip && <UncontrolledTooltip target={buttonId}>{tooltip}</UncontrolledTooltip>}
        </>
    );
}
```

**JSON input:**
```json
{ "type": "BUTTON", "id": "save", "title": "Save", "color": "primary", "default": true }
```

**Uses from context:**
- `callAction` — dispatches the button action (POST /rs/...)
- `isFetching` — disables during server calls
- `ui.translations.cancel` / `.yes` — i18n for confirm dialog

---

### DynamicGroup (ROW / COL / GROUP / FRAGMENT)

**File:** `DynamicGroup.jsx`

A single component that renders four different layout types. Switches the Bootstrap tag based on `type`:

```jsx
switch (type) {
    case 'COL':      Tag = Col;    break;
    case 'FRAGMENT': Tag = React.Fragment; break;
    case 'GROUP':    Tag = FormGroup; break;
    case 'ROW':      Tag = Row;    break;
    default:         Tag = React.Fragment;
}
```

For `COL`, it converts the `length` and `offset` props to Bootstrap column sizes:

```jsx
// JSON "length": { "xs": 12, "md": 6 }
// Becomes: <Col xs={12} md={6}>
```

```jsx
function DynamicGroup({ content, length, offset, type, collapseTitle }) {
    const { renderLayout } = React.useContext(DynamicLayoutContext);

    let Tag;
    let groupProperties = {};

    switch (type) {
        case 'COL':
            Tag = Col;
            if (length) groupProperties = buildLengthForColumn(length, offset);
            break;
        case 'FRAGMENT': Tag = React.Fragment; break;
        case 'GROUP':    Tag = FormGroup; groupProperties.row = true; break;
        case 'ROW':      Tag = Row; break;
        default:         Tag = React.Fragment;
    }

    if (collapseTitle) {
        const id = String.idify(collapseTitle);
        return (
            <Tag>
                <Button id={id} color="link">
                    {collapseTitle} <FontAwesomeIcon icon={faChevronDown} />
                </Button>
                <UncontrolledCollapse toggler={`#${id}`}>
                    {renderLayout(content)}
                </UncontrolledCollapse>
            </Tag>
        );
    }

    return <Tag {...groupProperties}>{renderLayout(content)}</Tag>;
}
```

**Wrapped in `React.memo` with structural comparison:**

```jsx
export default React.memo(DynamicGroup, groupPropsEqual);
```

The `groupPropsEqual` comparer checks only structural props (`type`, `collapseTitle`, and each child's `key` + `type`) to skip re-rendering when only data changes.

---

### DynamicFieldset

**File:** `DynamicFieldset.jsx`

Renders an HTML `<fieldset>` inside a Bootstrap `<Col>`. Supports collapsible mode (with chevron animation) and static mode.

```jsx
function DynamicFieldset({ content, title, length, offset, collapsed }) {
    const { renderLayout } = React.useContext(DynamicLayoutContext);
    const [isOpen, setIsOpen] = React.useState(collapsed !== true);

    // Static fieldset (not collapsible)
    if (collapsed === null || collapsed === undefined) {
        return (
            <Col {...buildLengthForColumn(length, offset)}>
                <fieldset>
                    {title ? <legend>{title}</legend> : undefined}
                    {renderLayout(content)}
                </fieldset>
            </Col>
        );
    }

    // Collapsible fieldset
    return (
        <Col {...buildLengthForColumn(length, offset)}>
            <fieldset>
                {title ? (
                    <legend>
                        <button onClick={handleToggle}>
                            <FontAwesomeIcon icon={faChevronRight}
                                className={isOpen ? style.expanded : ''} />
                            {title}
                        </button>
                    </legend>
                ) : undefined}
                <Collapse isOpen={isOpen}>
                    {renderLayout(content)}
                </Collapse>
            </fieldset>
        </Col>
    );
}
```

**JSON input — static:**
```json
{ "type": "FIELDSET", "title": "User Info", "content": [...] }
```

**JSON input — collapsible:**
```json
{ "type": "FIELDSET", "title": "Advanced", "collapsed": true, "content": [...] }
```

---

### DynamicInputResolver

**File:** `input/DynamicInputResolver.jsx`

The INPUT type's dispatcher. Routes to the correct input component based on `dataType`:

```jsx
function DynamicInputResolver(props) {
    const { dataType, ...rest } = props;

    switch (dataType) {
        case 'STRING':
            return rest.autoCompletionUrl
                ? <DynamicAutoCompletion {...rest} />
                : <DynamicInput {...rest} />;
        case 'DATE': return <DynamicDateInput {...rest} />;
        case 'TIMESTAMP': return <DynamicTimestampInput {...rest} />;
        case 'TIME': return <DynamicTimeInput {...rest} />;
        case 'TASK': return <DynamicTaskSelect {...rest} />;
        case 'USER': case 'GROUP': case 'EMPLOYEE':
        case 'COST1': case 'COST2': case 'KONTO':
            return <DynamicObjectSelect {...rest} />;
        case 'INT': case 'LONG': case 'DECIMAL': case 'NUMBER':
            return <DynamicInput type="number" {...rest} />;
        case 'PASSWORD':
            return <DynamicInput type="password" {...rest} />;
        default:
            return <span>Unknown dataType: {dataType}</span>;
    }
}
```

**JSON input — text field:**
```json
{ "type": "INPUT", "id": "email", "label": "Email", "dataType": "STRING", "required": true }
```

**JSON input — date picker:**
```json
{ "type": "INPUT", "id": "startDate", "dataType": "DATE", "label": "Start Date" }
```

**JSON input — user selector:**
```json
{ "type": "INPUT", "id": "manager", "dataType": "USER", "label": "Manager" }
```

---

### DynamicInput (text)

**File:** `input/DynamicInput.jsx`

A controlled `<Input>` that reads/writes from DynamicLayoutContext:

```jsx
function DynamicInput({ id, label, required, maxLength, focus, ...props }) {
    const { data, setData, validationErrors } = React.useContext(DynamicLayoutContext);
    const value = data && data[id];

    const error = validationErrors && validationErrors.find(e => e.fieldId === id);

    return (
        <>
            {label && <Label>{label}{required && <span>*</span>}</Label>}
            <Input
                id={id}
                value={value || ''}
                maxLength={maxLength}
                autoFocus={focus}
                onChange={(e) => setData({ [id]: e.target.value })}
                invalid={!!error}
            />
            {error && <FormFeedback>{error.message}</FormFeedback>}
        </>
    );
}
```

**Key behavior:**
- Controlled input: `value={value || ''}`
- Reads initial value from `data[id]`
- Writes changes via `setData({ [id]: newValue })`
- Shows validation errors from context via `validationErrors.find(e => e.fieldId === id)`

---

### DynamicSelect

**File:** `select/DynamicReactSelect.jsx`

Wraps `react-select` for dropdown functionality. Supports local values, async autocomplete, multi-select, favorites panel.

```jsx
function DynamicReactSelect({
    id, label, values, valueProperty, labelProperty,
    multi, required, autoCompletion, favorites, ...rest
}) {
    const { data, setData } = React.useContext(DynamicLayoutContext);
    const currentValue = data && data[id];

    // Local values or async autocomplete
    const options = values?.map(v => ({
        [valueProperty]: v[valueProperty],
        [labelProperty]: v[labelProperty],
    })) || [];

    return (
        <>
            {label && <Label>{label}</Label>}
            <ReactSelect
                isMulti={multi}
                options={options}
                value={options.find(o => o[valueProperty] === currentValue)}
                onChange={(selected) => setData({
                    [id]: selected ? selected[valueProperty] : null
                })}
            />
        </>
    );
}
```

**JSON input:**
```json
{
    "type": "SELECT",
    "id": "country",
    "label": "Country",
    "values": [
        { "id": "us", "displayName": "USA" },
        { "id": "de", "displayName": "Germany" }
    ],
    "valueProperty": "id",
    "labelProperty": "displayName"
}
```

---

### DynamicCheckbox

**File:** `input/DynamicCheckbox.jsx`

```jsx
function DynamicCheckbox({ id, label, color, inline, tooltip }) {
    const { data, setData } = React.useContext(DynamicLayoutContext);
    const checked = data && data[id];

    return (
        <div style={{ display: inline ? 'inline-flex' : 'flex', alignItems: 'center' }}>
            <Input type="checkbox" id={id} checked={!!checked}
                   onChange={(e) => setData({ [id]: e.target.checked })} />
            {label && <Label for={id}>{label}</Label>}
        </div>
    );
}
```

**JSON input:**
```json
{ "type": "CHECKBOX", "id": "agree", "label": "I agree to terms" }
```

---

### DynamicAlert

**File:** `DynamicAlert.jsx`

```jsx
function DynamicAlert({ message, title, color, id, markdown, icon }) {
    const { data } = React.useContext(DynamicLayoutContext);
    const resolvedMessage = (id && data && data[id]) || message;

    if (!resolvedMessage) return null;

    return (
        <Alert color={color}>
            {title && <h4>{title}</h4>}
            {markdown ? <ReactMarkdown>{resolvedMessage}</ReactMarkdown> : resolvedMessage}
        </Alert>
    );
}
```

**JSON input:**
```json
{
    "type": "ALERT", "message": "Saved successfully", "color": "success",
    "icon": ["fas", "check"]
}
```

**Supports:**
- Markdown content via `react-markdown`
- Dynamic message from `data[id]`
- FontAwesome icons

---

### DynamicValidationManager

**File:** `input/DynamicValidationManager.jsx`

Wraps input components and provides server-side validation feedback:

```jsx
function DynamicValidationManager({ id, children }) {
    const { validationErrors } = React.useContext(DynamicLayoutContext);
    const error = validationErrors?.find(e => e.fieldId === id);

    return (
        <div>
            {children(error)}
            {error && <span className="text-danger">{error.message}</span>}
        </div>
    );
}
```

---

## How Components Work Together

```
JSON Layout (from server)
  │
  ▼
DynamicLayout Context (data, setData, renderLayout, callAction)
  │
  ├── DynamicGroup (ROW)
  │     └── DynamicGroup (COL, md=6)
  │           ├── DynamicFieldset (title="Info")
  │           │     └── DynamicLabel (text="Name")
  │           └── DynamicInput (id="name", dataType="STRING")
  │                 └── reads/writes: context.data.name
  │
  ├── DynamicGroup (ROW)
  │     └── DynamicGroup (COL, md=6)
  │           └── DynamicButton (id="save", title="Save")
  │                 └── calls: context.callAction({id: "save"})
  │
  └── DynamicValidationManager (id="name")
        └── reads: context.validationErrors[fieldId === "name"]
```

Each component reads from `DynamicLayoutContext`:
- **Read data:** `context.data[id]` → initial value
- **Write data:** `context.setData({ [id]: value })` → updates context → re-renders
- **Validate:** `context.validationErrors.find(e => e.fieldId === id)` → shows errors
- **Actions:** `context.callAction(props)` → POST to server → ResponseAction
- **Render children:** `context.renderLayout(content)` → recursive rendering
