# JSON Schema for UILayout format

> **Status:** Draft. The format is not standardized — this is a reconstruction from source code.

## Full schema (Draft 2020-12)

The schema is defined in `dynamiclayout-schema.json` in the repository root and described in [api-reference.md](api-reference.md).

## Minimal valid JSON

```json
{
  "ui": {
    "title": "Hello",
    "uid": "layout1",
    "layout": [
      { "type": "LABEL", "key": "el-1", "label": "Hello World" }
    ],
    "translations": {},
    "userAccess": { "cancel": true }
  }
}
```

## Usage

### Server-side validation (Java/Spring)

```java
Schema schema = JsonSchemaFactory.getInstance().getSchema(
    getClass().getResourceAsStream("/ui-layout.schema.json")
);
ProcessingReport report = schema.validate(layoutJson);
if (!report.isSuccess()) { /* handle errors */ }
```

### Client-side validation (JavaScript)

```js
import Ajv from 'ajv';
import schema from './ui-layout.schema.json' assert { type: 'json' };
const ajv = new Ajv();
const validate = ajv.compile(schema);
if (!validate(layoutJson)) {
    console.error('Invalid layout:', validate.errors);
}
```

## Note

The JSON Schema is a formal specification of the format. Currently:
- **No official schema file** exists in the ProjectForge repository
- JSON serialization is a side effect of Gson, not a format design
- This schema is a reconstruction from source code and may not cover all edge cases

If the format is standardized, the schema should be extracted into a separate npm/Gradle package.
