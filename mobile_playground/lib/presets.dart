// DynamicLayout presets — same JSON as web playground and examples.
// Used by mobile playground and any other client that needs demo data.

const presets = <String, String>{
  'about': '''
{
  "ui": {
    "title": "About ProjectForge",
    "uid": "playground-about",
    "layout": [
      { "type": "FIELDSET", "key": "fs1", "title": "Info", "content": [
        { "type": "LABEL", "key": "l1", "label": "DynamicLayout Engine v1.0" },
        { "type": "LABEL", "key": "l2", "label": "Server-Driven UI for 42 languages + 12 clients" },
        { "type": "LABEL", "key": "l3", "label": "React, Vue, Svelte, Flutter, SwiftUI, Compose..." }
      ]},
      { "type": "FIELDSET", "key": "fs2", "title": "License", "content": [
        { "type": "LABEL", "key": "l4", "label": "Dual-licensed under GPLv3" },
        { "type": "LABEL", "key": "l5", "label": "https://www.projectforge.org/license" }
      ]},
      { "type": "ALERT", "key": "a1", "message": "This is the mobile playground — edit JSON on tab 2!", "color": "info" }
    ],
    "actions": [
      { "type": "BUTTON", "key": "b1", "id": "ok", "title": "OK", "color": "primary" },
      { "type": "BUTTON", "key": "b2", "id": "cancel", "title": "Cancel", "color": "secondary" }
    ],
    "translations": {},
    "userAccess": { "cancel": true }
  }
}
''',

  'feedback': '''
{
  "ui": {
    "title": "Feedback",
    "uid": "playground-feedback",
    "layout": [
      { "type": "FIELDSET", "key": "fs1", "title": "Your Details", "content": [
        { "type": "ROW", "key": "r1", "content": [
          { "type": "COL", "key": "c1", "length": { "xs": 12, "md": 6 }, "content": [
            { "type": "INPUT", "key": "i1", "id": "name", "label": "Name", "required": true }
          ]},
          { "type": "COL", "key": "c2", "length": { "xs": 12, "md": 6 }, "content": [
            { "type": "INPUT", "key": "i2", "id": "email", "label": "Email", "required": true }
          ]}
        ]},
        { "type": "INPUT", "key": "i3", "id": "subject", "label": "Subject" },
        { "type": "TEXTAREA", "key": "t1", "id": "message", "label": "Your Message", "rows": 5 }
      ]},
      { "type": "FIELDSET", "key": "fs2", "title": "Rating", "content": [
        { "type": "RATING", "key": "rt1", "id": "stars", "label": "Rate your experience" },
        { "type": "SELECT", "key": "s1", "id": "source", "label": "How did you find us?", "values": [
          { "id": "search", "displayName": "Search engine" },
          { "id": "friend", "displayName": "Friend recommendation" },
          { "id": "social", "displayName": "Social media" },
          { "id": "other", "displayName": "Other" }
        ]}
      ]}
    ],
    "actions": [
      { "type": "BUTTON", "key": "b1", "id": "send", "title": "Send Feedback", "color": "primary" },
      { "type": "BUTTON", "key": "b2", "id": "cancel", "title": "Cancel", "color": "secondary" }
    ],
    "translations": {},
    "userAccess": { "cancel": true }
  }
}
''',

  'registration': '''
{
  "ui": {
    "title": "Registration",
    "uid": "playground-registration",
    "layout": [
      { "type": "ROW", "key": "r1", "content": [
        { "type": "COL", "key": "c1", "length": { "xs": 12, "md": 6 }, "content": [
          { "type": "FIELDSET", "key": "fs1", "title": "Personal Info", "content": [
            { "type": "INPUT", "key": "i1", "id": "firstName", "label": "First Name", "required": true },
            { "type": "INPUT", "key": "i2", "id": "lastName", "label": "Last Name", "required": true },
            { "type": "INPUT", "key": "i3", "id": "email", "label": "Email", "required": true }
          ]}
        ]},
        { "type": "COL", "key": "c2", "length": { "xs": 12, "md": 6 }, "content": [
          { "type": "FIELDSET", "key": "fs2", "title": "Account Settings", "content": [
            { "type": "INPUT", "key": "i4", "id": "username", "label": "Username", "required": true },
            { "type": "INPUT", "key": "i5", "id": "password", "label": "Password", "dataType": "PASSWORD", "required": true },
            { "type": "SELECT", "key": "s1", "id": "country", "label": "Country", "values": [
              { "id": "us", "displayName": "United States" },
              { "id": "de", "displayName": "Germany" },
              { "id": "jp", "displayName": "Japan" },
              { "id": "br", "displayName": "Brazil" }
            ]}
          ]}
        ]}
      ]},
      { "type": "FIELDSET", "key": "fs3", "title": "Preferences", "content": [
        { "type": "CHECKBOX", "key": "cb1", "id": "newsletter", "label": "Subscribe to newsletter" },
        { "type": "CHECKBOX", "key": "cb2", "id": "agree", "label": "I agree to the terms and conditions" }
      ]},
      { "type": "ALERT", "key": "a1", "message": "All fields marked * are required", "color": "warning" }
    ],
    "actions": [
      { "type": "BUTTON", "key": "b1", "id": "register", "title": "Register", "color": "primary" },
      { "type": "BUTTON", "key": "b2", "id": "cancel", "title": "Cancel", "color": "secondary" }
    ],
    "translations": {},
    "userAccess": { "cancel": true }
  }
}
''',

  'all': '''
{
  "ui": {
    "title": "All DynamicLayout Components",
    "uid": "playground-all",
    "layout": [
      { "type": "ROW", "key": "r1", "content": [
        { "type": "COL", "key": "c1", "length": { "xs": 12, "md": 6 }, "content": [
          { "type": "FIELDSET", "key": "fs1", "title": "Inputs", "content": [
            { "type": "INPUT", "key": "i1", "id": "textInput", "label": "Text Input", "required": true },
            { "type": "INPUT", "key": "i2", "id": "numberInput", "label": "Number", "dataType": "INTEGER" },
            { "type": "INPUT", "key": "i3", "id": "dateInput", "label": "Date", "dataType": "DATE" },
            { "type": "INPUT", "key": "i4", "id": "passwordInput", "label": "Password", "dataType": "PASSWORD" },
            { "type": "TEXTAREA", "key": "t1", "id": "notes", "label": "Textarea", "rows": 4 }
          ]},
          { "type": "FIELDSET", "key": "fs2", "title": "Selection", "content": [
            { "type": "SELECT", "key": "s1", "id": "country", "label": "Select", "values": [
              { "id": "us", "displayName": "USA" },
              { "id": "de", "displayName": "Germany" },
              { "id": "jp", "displayName": "Japan" }
            ]},
            { "type": "CHECKBOX", "key": "cb1", "id": "agree", "label": "Checkbox" },
            { "type": "RATING", "key": "rt1", "id": "stars", "label": "Rating" }
          ]}
        ]},
        { "type": "COL", "key": "c2", "length": { "xs": 12, "md": 6 }, "content": [
          { "type": "FIELDSET", "key": "fs3", "title": "Display", "content": [
            { "type": "LABEL", "key": "l1", "label": "This is a label" },
            { "type": "BADGE", "key": "bd1", "title": "Primary", "color": "primary" },
            { "type": "BADGE", "key": "bd2", "title": "Success", "color": "success" },
            { "type": "BADGE", "key": "bd3", "title": "Danger", "color": "danger" },
            { "type": "SPACER", "key": "sp1", "width": 12 },
            { "type": "PROGRESS", "key": "pr1", "label": "Progress", "progress": 65, "color": "primary" },
            { "type": "SPACER", "key": "sp2", "width": 12 },
            { "type": "ALERT", "key": "a1", "message": "Info alert message", "color": "info" },
            { "type": "ALERT", "key": "a2", "message": "Warning: check your input!", "color": "warning" },
            { "type": "ALERT", "key": "a3", "message": "Success: saved!", "color": "success" }
          ]},
          { "type": "FIELDSET", "key": "fs4", "title": "Read-only", "content": [
            { "type": "READONLY_FIELD", "key": "ro1", "id": "textInput", "label": "Echo of text input" }
          ]}
        ]}
      ]}
    ],
    "actions": [
      { "type": "BUTTON", "key": "b1", "id": "save", "title": "Save", "color": "primary" },
      { "type": "BUTTON", "key": "b2", "id": "cancel", "title": "Cancel", "color": "secondary" }
    ],
    "translations": {},
    "userAccess": { "cancel": true }
  }
}
'''
};
