# Refactoring Plan: Extracting DynamicLayout Engine

## Goal

Extract DynamicLayout Engine from ProjectForge into a separate, well-documented project with minimal dependencies.

```
Before: projectforge-rest/.../ui/*.kt  (59 files, tightly coupled with PF)
After:
  dynamiclayout-core/      — 34+ files, pure Kotlin, 0 dependencies
  dynamiclayout-spring/   — Auto-configuration for Spring Boot
  dynamiclayout-jpa/      — JPA metadata adapter
  dynamiclayout-react/    — DynamicRenderer (already mostly independent)
```

## Dependency Analysis

| Layer | Files | Dependencies | Extraction Difficulty |
|------|:-----:|-------------|:--------------------:|
| **Core model** | 34 | None external | ✅ Trivial |
| **Light (i18n)** | 6 | `translate()`, `I18nEnum` | 🟡 Low |
| **Medium (HTTP)** | 3 | `ResponseEntity`, `HttpStatus` | 🟡 Low |
| **Heavy (PF business)** | 11 | `BaseDao`, `PFUserDO`, `MagicFilter`... | 🔴 High |
| **Jackson annotations** | 5 | `@JsonProperty`, `@JsonValue` | 🟡 Low |

## Step-by-Step Plan

### Step 1: Core Model (34 files) — Ready for Extraction

These files have no external dependencies:
`CssClassnames.kt`, `IUIContainer.kt`, `IUIId.kt`, `UIBadge.kt`, `UIBadgeList.kt`, `UICol.kt`, `UICreatableSelect.kt`, `UICustomized.kt`, `UIDataType.kt`, `UIDropArea.kt`, `UIElement.kt`, `UIElementType.kt`, `UIFieldset.kt`, `UIInlineGroup.kt`, `UILabel.kt`, `UILabelledElement.kt`, `UILength.kt`, `UIList.kt`, `UINamedContainer.kt`, `UIProgress.kt`, `UIRadioButton.kt`, `UIRatingStars.kt`, `UIReadOnlyField.kt`, `UIRow.kt`, `UISelectValue.kt`, `UISpacer.kt`, `UITableColumn.kt`, `UITextArea.kt`, `UIAlert.kt`, `UIFilterTimestampElement.kt`, `UIFilterBooleanElement.kt`, `UIFilterObjectElement.kt`

**What to do:**
- Remove Jackson annotations from `UIColor.kt`, `UIIconType.kt`, `UIInput.kt`, `UIList.kt`
- Replace with `@SerialName` (kotlinx.serialisation) or externalize to config
- Move to `dynamiclayout-core/src/main/kotlin/org/dynamiclayout/core/ui/`

Package: `org.dynamiclayout.core`

```kotlin
// New package
package org.dynamiclayout.core.ui

data class UILabel(
    val label: String? = null,
    val labelFor: String? = null,
    // ...
)
```

### Step 2: I18nProvider (6 files)

Problem: `LayoutUtils.kt`, `ResponseAction.kt`, `UIButton.kt`, `UISelect.kt`, `UILayout.kt`, `UIAttachmentList.kt` directly call `org.projectforge.framework.i18n.translate()`.

**Solution: Higher-order function**

```kotlin
// dynamiclayout-core
fun interface I18nProvider {
    fun translate(key: String): String
}
```

`LayoutUtils` accepts `I18nProvider` as a parameter.

**Spring adapter:**
```kotlin
class SpringI18nProvider : I18nProvider {
    override fun translate(key: String) = I18nHelper.getLocalizedMessage(key)
}
```

### Step 3: MetadataProvider (ElementsRegistry)

Problem: `ElementsRegistry` reads JPA annotations (`@Column.length`, `@Column.nullable`) and `@PropertyInfo` annotations. Depends on `EntityMetaDataRegistry`, `BeanUtils`, `PropUtils`.

**Solution: Abstract MetadataProvider**

```kotlin
// dynamiclayout-core
fun interface MetadataProvider {
    fun getFieldMetadata(clazz: Class<*>, fieldName: String): FieldMetadata?
    fun getDataType(clazz: Class<*>): UIDataType
}
```

### Step 4: HTTP Abstraction

Problem: `AbstractDynamicPageRest`, `UIToast`, `ValidationError` use `ResponseEntity`, `HttpStatus`.

**Solution: Replace with Result type**

```kotlin
// dynamiclayout-core
data class ServiceResult<T>(
    val data: T? = null,
    val validationErrors: List<ValidationError>? = null,
    val status: ServiceStatus = ServiceStatus.OK
)
```

### Step 5: Business Adapters

Problem: `UISelect` works with `UserService`, `GroupService`, `EmployeeService`. `UIAgGrid` with `MagicFilter`, `MultiSelectionSupport`.

**Solution: Provider interfaces with default implementations**

```kotlin
interface SelectDataProvider<T> {
    fun getValues(owner: UISelect<T>): List<UISelectValue<T>>?
}
```

The business logic (UserService, GroupService) stays in ProjectForge. Only interfaces go to the separate repository.

### Step 6: React DynamicRenderer

DynamicRenderer is already mostly independent. The only coupling is through `customized/` components (Vacation, Timesheet, Address...).

**Solution:**
- `DynamicRenderer.jsx` + `index.jsx` + `context.tsx` → npm package `@dynamiclayout/react`
- `customized/` → stays in ProjectForge (registered via `registerComponent`)

## Final Repository Structure

```
dynamiclayout/
├── core/                           # Kotlin DSL + JSON model
│   ├── src/main/kotlin/org/dynamiclayout/core/
│   │   ├── ui/                     # UIElementType, UIRow, UICol, UIInput...
│   │   ├── provider/               # I18nProvider, MetadataProvider
│   │   └── util/                   # LayoutUtils (without Spring/JPA)
│   └── build.gradle.kts
│
├── spring/                         # Spring Boot integration
│   ├── src/main/kotlin/org/dynamiclayout/spring/
│   │   ├── autoconfigure/          # @EnableDynamicLayout
│   │   ├── provider/               # SpringI18nProvider, JpaMetadataProvider
│   │   └── web/                    # AbstractDynamicPageRest
│   └── build.gradle.kts
│
├── react/                          # React DynamicRenderer
│   ├── src/
│   │   ├── DynamicLayout.jsx
│   │   ├── DynamicRenderer.jsx
│   │   ├── components/
│   │   └── context.tsx
│   └── package.json
│
├── docs/                           # Documentation
│   └── ...
│
└── README.md
```

## What Stays in ProjectForge

After extraction, ProjectForge retains:
- `*PageRest.kt` (page-specific controllers: AddressPagesRest, TimesheetPagesRest...)
- `customized/` components (VacationTable, TimesheetEdit...)
- `UIAgGrid` + `UIAgGridColumnDef` (tightly coupled with MagicFilter)
- `ElementsRegistry` (accessible through adapter)
- `AbstractPagesRest` (accessible through DynamicPageController interface)
- Business services (UserService, GroupService...)
