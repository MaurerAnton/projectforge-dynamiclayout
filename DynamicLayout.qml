// DynamicLayout Qt/QML renderer — renders PFDL JSON in Qt Quick apps.
// Single-file QML component. Works on Qt 6.5+ (Desktop, Embedded, Mobile).
//
// Example:
//   DynamicLayout { spec: uiJsonObject; data: formData; onAction: id => console.log(id) }

import QtQuick
import QtQuick.Controls
import QtQuick.Layouts

Item {
    id: root
    property var spec: ({})
    property var data: ({})
    signal actionTriggered(string id, var actionData)

    implicitWidth: column.implicitWidth
    implicitHeight: column.implicitHeight

    function setData(key, value) {
        var d = Object.assign({}, data)
        d[key] = value
        data = d
    }

    function renderContent(content) {
        if (!content) return []
        var result = []
        for (var i = 0; i < content.length; i++) {
            result.push(renderEl(content[i]))
        }
        return result
    }

    function renderEl(el) {
        switch (el.type) {
        case "ROW": return rowComponent.createObject(column, { el: el })
        case "COL": return colComponent.createObject(column, { el: el })
        case "FIELDSET": return fieldsetComponent.createObject(column, { el: el })
        case "GROUP": return groupComponent.createObject(column, { el: el })
        case "LABEL": return Qt.createQmlObject('import QtQuick; Text { text: "' + (el.label||"") + '"; font.weight: Font.Medium }', column)
        case "ALERT": return Qt.createQmlObject('import QtQuick; Rectangle { color: "' + (el.color==="danger"?"#f8d7da":el.color==="warning"?"#fff3cd":"#cff4fc") + '"; radius: 6; implicitWidth: 200; implicitHeight: 40; Text { anchors.centerIn: parent; text: "' + (el.message||"") + '" } }', column)
        case "BADGE": return Qt.createQmlObject('import QtQuick; Rectangle { color: "' + (el.color==="primary"?"#0d6efd":"#6c757d") + '"; radius: 4; implicitWidth: badgeText.implicitWidth + 16; implicitHeight: 20; Text { id: badgeText; text: "' + (el.title||"") + '"; color: "white"; font.pixelSize: 12; font.weight: Font.Bold; anchors.centerIn: parent } }', column)
        case "SPACER": return Qt.createQmlObject('import QtQuick; Item { implicitHeight: ' + (el.width||20) + ' }', column)
        case "INPUT": return inputComponent.createObject(column, { el: el, dataRef: root })
        case "CHECKBOX": return checkboxComponent.createObject(column, { el: el, dataRef: root })
        case "TEXTAREA": return textareaComponent.createObject(column, { el: el, dataRef: root })
        case "SELECT": return selectComponent.createObject(column, { el: el, dataRef: root })
        case "READONLY_FIELD": return readonlyComponent.createObject(column, { el: el, dataRef: root })
        case "BUTTON": return buttonComponent.createObject(column, { el: el, rootRef: root })
        default: return Qt.createQmlObject('import QtQuick; Text { text: "Unknown: ' + (el.type||"?") + '"; color: "red" }', column)
        }
    }

    ColumnLayout {
        id: column
        anchors.fill: parent
        spacing: 8

        Text {
            text: spec.title || ""
            font.pixelSize: 20; font.weight: Font.Bold
            visible: spec.title ? true : false
        }

        Component.onCompleted: {
            if (spec.layout) {
                var children = renderContent(spec.layout)
                for (var i = 0; i < children.length; i++) {
                    children[i].parent = column
                }
            }
            if (spec.actions) {
                var actions = renderContent(spec.actions)
                for (var j = 0; j < actions.length; j++) {
                    actions[j].parent = column
                }
            }
        }
    }

    // ── Dynamic components ──

    Component { id: rowComponent; RowLayout { property var el; Component.onCompleted: { var kids = root.renderContent(el.content); for (var i=0; i<kids.length; i++) kids[i].parent = this } } }
    Component { id: colComponent; ColumnLayout { property var el; Component.onCompleted: { var kids = root.renderContent(el.content); for (var i=0; i<kids.length; i++) kids[i].parent = this } } }
    Component { id: groupComponent; ColumnLayout { property var el; spacing: 4; Component.onCompleted: { var kids = root.renderContent(el.content); for (var i=0; i<kids.length; i++) kids[i].parent = this } } }
    Component { id: fieldsetComponent; GroupBox { property var el; title: el.title || ""; Component.onCompleted: { var kids = root.renderContent(el.content); for (var i=0; i<kids.length; i++) kids[i].parent = this } } }

    Component {
        id: inputComponent
        ColumnLayout { property var el; property var dataRef
            Label { text: (el.label||"") + (el.required ? " *" : ""); visible: el.label ? true : false }
            TextField { id: tf; text: dataRef.data[el.id] || ""; onTextChanged: dataRef.setData(el.id, text) }
        }
    }
    Component {
        id: checkboxComponent
        RowLayout { property var el; property var dataRef
            CheckBox { id: cb; checked: dataRef.data[el.id] === true; onCheckedChanged: dataRef.setData(el.id, checked) }
            Label { text: el.label || ""; visible: el.label ? true : false }
        }
    }
    Component {
        id: textareaComponent
        ColumnLayout { property var el; property var dataRef
            Label { text: el.label || ""; visible: el.label ? true : false }
            TextArea { id: ta; text: dataRef.data[el.id] || ""; implicitHeight: (el.rows||3) * 20; onTextChanged: dataRef.setData(el.id, text) }
        }
    }
    Component {
        id: selectComponent
        ColumnLayout { property var el; property var dataRef
            Label { text: el.label || ""; visible: el.label ? true : false }
            ComboBox { id: cb; model: (el.values||[]).map(function(v) { return v.displayName }); currentIndex: { var vals = el.values||[]; var cur = dataRef.data[el.id]; for (var i=0; i<vals.length; i++) if (vals[i].id == cur) return i+1; return 0 } onActivated: { if (index > 0) dataRef.setData(el.id, el.values[index-1].id) } }
        }
    }
    Component {
        id: readonlyComponent
        ColumnLayout { property var el; property var dataRef
            Label { text: el.label || ""; font.weight: Font.Medium; visible: el.label ? true : false }
            Rectangle { color: "#f8f9fa"; radius: 4; implicitWidth: 200; implicitHeight: 30; Label { anchors.centerIn: parent; text: dataRef.data[el.id] || "—" } }
        }
    }
    Component {
        id: buttonComponent
        property var el; property var rootRef
        Button {
            text: el.title || el.id; onClicked: rootRef.actionTriggered(el.id, el.responseAction)
            background: Rectangle { color: el.color==="primary"?"#0d6efd":el.color==="danger"?"#dc3545":el.color==="secondary"?"#6c757d":"#0d6efd"; radius: 6 }
            contentItem: Text { text: parent.text; color: "white"; font.weight: Font.Bold; horizontalAlignment: Text.AlignHCenter; verticalAlignment: Text.AlignVCenter }
        }
    }
}
