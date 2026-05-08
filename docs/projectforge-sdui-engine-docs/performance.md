# Performance

## JSON size analysis

### Typical scenarios

| Page type | JSON size | Elements | Comment |
|-----------|:---------:|:--------:|--------|
| About (static) | ~1.5 KB | 10-15 | Only LABEL + FIELDSET |
| Feedback form | ~3 KB | 15-20 | INPUT + TEXTAREA + BUTTON |
| Address edit | ~8-15 KB | 40-80 | Row/Col/Fieldset/Input/Select |
| List page (Table) | ~5 KB | 20-30 | Table + columns |
| List page (AgGrid) | ~8-12 KB | 30-50 | AgGrid + columnDefs |
| Address edit (complex) | ~25 KB | 150+ | CUSTOMIZED: phones, import, images |

### Bottlenecks

1. **Translations** — flat i18n key map. A page with tasks may have 50+ keys
2. **AgGrid columnDefs** — each column is fully serialized even with mostly default values
3. **UISelect.values** — value list can be large (countries, currencies, departments)
4. **CUSTOMIZED components** — custom components may include their own data in `values`

### Comparison with DivKit

| Aspect | ProjectForge | DivKit |
|--------|-------------|--------|
| Form example | ~3-15 KB | ~2-10 KB (templates reduce size) |
| Templates | None | Yes — 30-50% size reduction |
| Repeated structures | Copied in JSON | Extracted to `templates` |
| Minification | Gson compact | Similar |

DivKit wins on templates. ProjectForge could have a similar mechanism but currently serializes every page fully.

## Impact on server response time

### Server-side

1. **Building UILayout** (createEditLayout) — creating Kotlin objects: 1-5ms
2. **LayoutUtils.process()** — recursive traversal, i18n, annotations: 5-20ms
3. **Gson serialization** — reflection, object graph traversal: 5-30ms (depends on size)
4. **Total:** ~10-55ms for JSON generation on server

### Client-side

1. HTTP GET to server: ~5-50ms (LAN/internet)
2. JSON.parse() — ~1-5ms for 15 KB
3. React reconciliation — ~5-30ms

**Total time to full render:** ~20-140ms for a typical page.

## Gson serialization overhead

Gson uses reflection to serialize objects. For UI classes:

```kotlin
data class UIInput(
    val id: String,
    val dataType: UIDataType,
    val label: String?,
    // ...
    @Transient val layoutContext: LayoutContext?
)
```

Each `@Transient` layoutContext holds JPA metadata references that would cause circular references and StackOverflow if serialized. Without `@Transient`, JSON would be an order of magnitude larger and broken.

## Optimization recommendations

### Server-side

1. **Use `'literal`** instead of i18n keys for static text — LayoutUtils skips lookup
2. **UISelect.valueProperty / labelProperty** — configure fields instead of full serialization of each value
3. **Avoid deep nesting** over 4 levels of Row/Col — larger JSON, slower traversal
4. **Cache layout** for read-only pages (e.g., About)
5. **UIAgGridColumnDef.filter** — use string type over object where possible

### Client-side

1. **React.memo** on DynamicLayout components (already applied to DynamicGroup, DynamicFieldset, DynamicInlineGroup)
2. **AgGrid** virtualizes rows but columnDefs are processed fully
3. **Redux** — each `setData` causes full FormPage re-render (no element-level selectors)

## Potential improvements

| Improvement | Expected effect | Difficulty |
|------------|----------------|:----------:|
| React.memo for DynamicGroup/DynamicFieldset | Less re-render on data changes | ✅ Done |
| Jackson with Afterburner | 2-3x faster serialization | Medium (library swap) |
| Templates (like DivKit) | 2x smaller JSON for repeated structures | High |
| Gzip at HTTP level | 5-10x smaller transfer | Already on (Spring Boot) |
| Virtual scroll for large SELECT | Less data in JSON | High |

## Current performance

At typical enterprise scale (50-200 concurrent users), performance is **adequate**. For hundreds of thousands of users, optimization would be needed:

1. Gson → Jackson (better Kotlin support) — easy
2. Read-only layout caching — easy
3. React.memo for components — ✅ done
4. Templates — hard but most impactful
