# History of DynamicLayout Engine

## About the name

Inside the project, the engine is called **DynamicLayout** (React side) / **UILayout** (Kotlin side). Git commits and code reference:
- `DynamicLayout` — root React provider component
- `DynamicRenderer` — recursive renderer
- `UILayout` — root Kotlin layout class
- `UIElement` — base class for all UI components
- `LayoutUtils` — server-side post-processing

---

## Timeline

### ▸ March 2019 — Beginnings

First commits by **Fin Reinhard**:

```
2019-03-13  |  add Base Edit Page
2019-03-13  |  WIP: add Search Filter
2019-03-13  |  WIP: move Edit Page to redux
2019-03-13  |  WIP: implement redux state in editpage
```

The original `EditPage` was created — ancestor of the current `FormPage`. It already used Redux and dynamic data fetching via REST. At that point, the React UI was hybrid — some components were hardcoded, some dynamic.

### ▸ May 2019 — DynamicLayout as concept

```
2019-05-04  |  Fin | WIP: dynamic layout restructuration
2019-05-15  |  Fin | option system for dynamic layout with usage of context
2019-05-16  |  Fin | WIP: layout group move to dynamic layout
2019-05-17  |  Fin | add dynamic input resolver
2019-05-17  |  Fin | store and manipulate data in the dynamic layout context
2019-05-17  |  Fin | add ValidationManager
2019-05-20  |  Fin | implement DynamicFieldset
2019-05-20  |  Fin | add DynamicLabel
```

Key month. Created:
- **DynamicLayoutContext** — React Context for data and UI between components
- **DynamicInputResolver** — dispatcher by `dataType`
- **DynamicFieldset** — first container component
- **DynamicLabel** — text label
- **ValidationManager** — server validation error → field binding

Original idea: replace the monolithic `LayoutGroup` with a system of small reusable components managed through context.

### ▸ June 2019 — First business components and migration

```
2019-06-04  |  Fin | finish TimesheedEditTaskAndKost2 rebuild to dynamic layout
2019-06-14  |  Fin | implement DynamicLayout to LayoutGroup bridge
2019-06-14  |  Fin | add ActionGroup to DynamicLayout
2019-06-20  |  Fin | add dynamic checkbox
2019-06-25  |  Fin | move all LayoutInput types to Dynamic Layout
2019-06-25  |  Fin | implement Text Area
2019-06-27  |  Fin | replaced LayoutGroup with DynamicLayout in EditPage completely
2019-06-27  |  Fin | move list view to dynamic layout
2019-06-27  |  Fin | remove LayoutGroup completely
```

**Kai Reinhard** simultaneously created Kotlin classes in `projectforge-rest/.../ui/`:

```
2019-06-07  |  Kai | CopyRight of all Kotlin file-header updated or created
                (first versions of UILayout, UIElement, UIInput, UISelect, ...)
```

Month of full migration. Old `LayoutGroup` deleted. Everything moved to DynamicLayout. Created:
- `DynamicCheckbox`, `DynamicTextArea`
- `DynamicReactSelect` (react-select)
- `DynamicActionGroup` (action buttons)
- `UISelect` with custom serializer (`UISelectTypeSerializer`)
- First `customized` components: `BookLendOut`, `CustomizedAddressImage`
- `DynamicInput` performance improvements

### ▸ July 2019 — Bootstrap grid, ResponseAction, Favorites

```
2019-07-02  |  Fin | add component registry for DynamicLayout
2019-07-04  |  Fin | simplified DynamicTable
2019-07-07  |  Fin | implement new action handling in listpage/editpage
2019-07-09  |  Kai | Frontend: radio button implemented
2019-07-09  |  Kai | Bootstrap grid system: length, sm, md, lg, xl
2019-07-11  |  Kai | UIButton supports now ResponseAction
2019-07-12  |  Kai | ResponseAction: targetType TOAST added
2019-07-14  |  Kai | UIStyle -> UIColor
```

Three key changes:
1. **Component Registry** (`DynamicRenderer.jsx`) — single registration point, type → component
2. **ResponseAction** — server returns not only layout but actions (redirect, toast, update, modal)
3. **Bootstrap Grid** — `UICol.length` with `xs/sm/md/lg/xl` support, responsive layout
4. **Favorites** — `UISelect` supports favorites

### ▸ August 2019 — Customized component growth

```
2019-08-01  |  Fin/Kai | Timesheet edit: customized consumption bar, favorites, templates
2019-08-04  |  Kai     | TimesheetTemplatesAndRecents, CustomizedConsumptionBar
2019-08-03  |  Kai     | WIP: Time sheet editing (favorites)
```

Rapid growth of domain-specific customized components: timesheets, tasks, vacations.

### ▸ September 2019 — March 2020 — FormPage

```
2019-11-06  |  Fin | wip: implement dynamic list
2019-11-07  |  Fin | move Dynamic List
2020-01-20  |  Kai | First version of (ugly) read-only fields in react
2020-03-04  |  Fin | combine edit and dynamic page to form page → FormPage
2020-03-06  |  Fin | DynamicActionButton -> DynamicButton
2020-03-19  |  Fin | wip: implement dynamic alert
```

`FormPage` merged EditPage and DynamicPage. Created `DynamicAlert`, `DynamicButton`, `UIReadOnlyField`.

### ▸ 2020 — Tables, attachments

```
2020-04-12  |  Kai | wip: attachments in ui → UIAttachmentList
2020-10-14  |  Kai | Rating component (skills matrix)
2021-05-26  |  Kai | DynamicTable for list pages; refactored to DynamicListPageTable
```

- `AttachmentList` — full file manager
- `DynamicTable` — basic table
- `DynamicListPageTable` — table with row navigation
- `Rating` — star rating

### ▸ 2021 — DropArea, Badge, CreatableSelect, Editor

```
2021-05-26  |  Kai | LogViewer: DynamicTable outside of list pages
2021-06-21  |  Kai | UIBadge/DynamicBadgeList
2021-06-23  |  Kai | UIDropArea added
2021-06-28  |  Kai | UICreatableSelect added
2021-09-06  |  Kai | UILayout supports back-button for edit forms
2022-03-01  |  Kai | ACE editor added (scripting)
```

### ▸ 2022 — AgGrid, WebAuthn, TypeScript

```
2022-03-25  |  Kai | WIP: AG-Grid
2022-04-01  |  Kai | WIP: mass update. Checkbox fixed
2022-04-30  |  Kai | WebAuthn: AGGrid now supported as form element
2022-05-20  |  Fin | fix typescript eslint bugs, fix eslint
```

Biggest change: **AgGrid**. Full enterprise table with columns, sorting, filters, pagination, row selection, cell renderers, MultiLineCell, DiffCell, ImportStatusCell, localization.

TypeScript started being adopted (first `.tsx` files: `context.tsx`, `AddressFieldSelector`, `VCardImportDialog`, `MultipleFileUploadArea`).

### ▸ 2025-2026 — React 18, modern code

```
2025-01-06  |  Fin | lift to react 18 wip
2025-10-04  |  Kai | WIP: currency conversion with Claude Code
```

React 18 upgrade, refactoring.

## Developers

**Fin Reinhard** — main React-side developer:
- Created DynamicLayout, DynamicRenderer, all base components
- Migrated LayoutGroup → DynamicLayout
- Implemented FormPage, action handling, validation

**Kai Reinhard** — main Kotlin-side developer:
- Created UILayout, UIRow, UICol, UIInput, UISelect, UIButton and all UI classes
- Wrote LayoutUtils (post-processing, i18n, keys)
- Implemented auto-detection (JPA → UI), ElementsRegistry
- ResponseAction, UserAccess, processEditPage()

**Ulrich Schmidt** — occasional CSS contributions

The engine grew iteratively without a master plan — each change solved a specific problem. By 2022, a complete SDUI system had emerged.

## Key milestones

| Period | Change |
|--------|--------|
| March 2019 | Base Edit Page (FormPage ancestor) |
| May 2019 | DynamicLayout as concept, 5 base components |
| June 2019 | Full migration, LayoutGroup deleted, first customized |
| July 2019 | Component Registry, ResponseAction, Bootstrap grid, Favorites |
| 2019-2020 | FormPage, DynamicAlert, DynamicButton, read-only |
| 2020-2021 | Tables, attachments, rating |
| 2021 | DropArea, Badge, CreatableSelect, Editor |
| 2022 | AgGrid, WebAuthn, TypeScript |
| 2025-2026 | React 18 |

| Metric | End of 2019 | 2026 |
|--------|:-----------:|:----:|
| Kotlin UI files | 39 | **59** |
| React files (jsx/tsx) | 37 | **90** |
| Registered in DynamicRenderer | 17 | **31** |
| UIElementType values | 18 | **33** |
| Customized sub-types | 10 | **31** |
| Lines of code (Kotlin) | ~2843 | **~5917** |
| Lines of code (React) | ~2917 | **~9532** |
