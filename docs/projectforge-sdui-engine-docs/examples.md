# Usage Examples

## Example 1: About Page (static info page)

The simplest SDUI example. The server returns a layout without data or actions.

### Kotlin controller

```kotlin
@RestController
@RequestMapping("${Rest.URL}/about")
class AboutPageRest : AbstractDynamicPageRest() {
    @GetMapping("dynamic")
    fun getForm(request: HttpServletRequest): FormLayoutData {
        val layout = UILayout("'About ProjectForge")

        layout.add(
            UIFieldset(title = "'About")
                .add(UILabel("'ProjectForge Community Edition"))
                .add(UILabel("'Version 8.2-SNAPSHOT"))
                .add(UILabel("'Copyright 2001-2026 Micromata GmbH"))
        )

        layout.add(
            UIFieldset(title = "'License")
                .add(UILabel("'Dual-licensed under GPLv3"))
                .add(UILabel("'https://www.projectforge.org/license"))
        )

        layout.add(
            UIFieldset(title = "'Links")
                .add(UILabel("'Website: www.projectforge.org"))
                .add(UILabel("'GitHub: github.com/projectforge"))
        )

        LayoutUtils.process(layout)
        return FormLayoutData(null, layout, createServerData(request))
    }
}
```

## Example 2: Feedback form

Form with name, email, and message fields.

### Kotlin

```kotlin
fun getLayout(): UILayout {
    val layout = UILayout("feedback.send.title")

    layout.add(
        UIFieldset(title = "'Your details")
            .add(
                UIRow().add(
                    UICol(UILength(xs = 12, md = 6))
                        .add(UIInput("name", dataType = UIDataType.STRING, label = "'Name", required = true))
                ).add(
                    UICol(UILength(xs = 12, md = 6))
                        .add(UIInput("email", dataType = UIDataType.STRING, label = "'E-mail", required = true))
                )
            )
            .add(UIInput("subject", dataType = UIDataType.STRING, label = "'Subject", required = true))
            .add(UITextArea("message", label = "'Message", required = true, rows = 6))
    )

    layout.add(UIButton.createDefaultButton(id = "send", title = "'Send Feedback"))
    layout.add(UIButton.createCancelButton())
    LayoutUtils.process(layout)
    return layout
}
```

## Example 3: Edit page with save

Typical edit page using `AbstractPagesRest` which automatically adds Save/Cancel buttons.

### Kotlin

```kotlin
@RestController
@RequestMapping("${Rest.URL}/contact")
class ContactPageRest : AbstractPagesRest<ContactDO, ContactDao>() {
    override fun createEditLayout(dto: ContactDO, request: HttpServletRequest): UILayout {
        val layout = UILayout("contact.title.edit")

        layout.add(
            UIFieldset(title = "'General")
                .add(UIRow().add(
                    UICol(UILength(xs = 12, md = 6))
                        .add(UIInput("name", label = "'Name"))
                        .add(UIInput("email", label = "'E-mail"))
                ).add(
                    UICol(UILength(xs = 12, md = 6))
                        .add(UIInput("phone", label = "'Phone"))
                        .add(UIInput("company", label = "'Company"))
                ))
        )
        layout.add(
            UIFieldset(title = "'Address")
                .add(UIInput("street", label = "'Street"))
                .add(UIRow().add(
                    UICol(UILength(xs = 12, md = 4))
                        .add(UIInput("zipCode", label = "'ZIP"))
                ).add(
                    UICol(UILength(xs = 12, md = 8))
                        .add(UIInput("city", label = "'City"))
                ))
        )
        return layout
    }
}
```

## Example 4: List page with AgGrid

List page with AgGrid, filters, and search.

### Kotlin

```kotlin
override fun createListLayout(request: HttpServletRequest): UILayout {
    val layout = UILayout("contact.title.list")
    layout.hideSearchFilter = false
    layout.excelExportSupported = true
    layout.multiSelectionSupported = true

    val agGrid = UIAgGrid(
        columnDefs = mutableListOf(
            UIAgGridColumnDef(field = "name", headerName = "'Name", sortable = true, filter = "agTextColumnFilter"),
            UIAgGridColumnDef(field = "email", headerName = "'E-mail", sortable = true, filter = "agTextColumnFilter"),
            UIAgGridColumnDef(field = "company", headerName = "'Company", sortable = true, filter = "agTextColumnFilter"),
            UIAgGridColumnDef(field = "created", headerName = "'Created", dataType = UIDataType.DATE, sortable = true, filter = "agDateColumnFilter"),
        ),
        rowSelection = UIAgGrid.RowSelection(mode = "multiRow", enableClickSelection = true),
        pagination = true,
        paginationPageSize = 50,
    )
    layout.add(agGrid)
    LayoutUtils.processListPage(layout)
    return layout
}
```

## Example 5: Complex nested layout

Two-column layout with sections.

### Kotlin DSL

```kotlin
layout.add(
    UIRow().add(
        UICol(UILength(xs = 12, md = 6))
            .add(
                UIFieldset(title = "'Primary Info")
                    .add(UIInput("firstName", label = "'First Name"))
                    .add(UIInput("lastName", label = "'Last Name"))
                    .add(UIInput("email", label = "'E-mail"))
            )
            .add(
                UIFieldset(title = "'Address", collapsed = true)
                    .add(UIInput("street", label = "'Street"))
                    .add(UIInput("city", label = "'City"))
                    .add(UIInput("zip", label = "'ZIP"))
            )
    ).add(
        UICol(UILength(xs = 12, md = 6))
            .add(
                UIFieldset(title = "'Employment")
                    .add(UISelect("department", label = "'Department", values = departments))
                    .add(UIInput("startDate", label = "'Start Date", dataType = UIDataType.DATE))
            )
    )
)
```

## Example 6: InlineGroup usage

Compact layout: Label + Input + Label in one row.

### Kotlin

```kotlin
layout.add(
    UIInlineGroup()
        .add(UILabel("'From:"))
        .add(UIInput("startTime", dataType = UIDataType.TIME))
        .add(UILabel("'To:"))
        .add(UIInput("endTime", dataType = UIDataType.TIME))
)
```
