package dynamiclayout.playground

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable fun RenderEl(el: Map<String, Any?>, data: MutableMap<String, Any?>,
                          onUpdate: ((Map<String, Any?>) -> Unit)? = null,
                          onAction: ((String, Map<String, Any?>?) -> Unit)? = null) {
    val t = el["type"]?.toString() ?: ""
    @Composable fun C() { (el["content"] as? List<*>)?.forEach { RenderEl((it as? Map<String, Any?>) ?: emptyMap(), data, onUpdate, onAction) } }
    when (t) {
        "ROW" -> Row(Modifier.fillMaxWidth()) { C() }; "COL" -> Column { C() }; "GROUP" -> Column { C() }
        "INLINE_GROUP" -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { C() }
        "FIELDSET" -> Card(Modifier.fillMaxWidth().padding(bottom = 12.dp)) { Column(Modifier.padding(12.dp)) { el["title"]?.let { Text(it.toString(), fontWeight = FontWeight.SemiBold) }; C() } }
        "LABEL" -> Text(el["label"]?.toString() ?: "", Modifier.padding(bottom = 4.dp), fontWeight = FontWeight.Medium)
        "ALERT" -> { val bg = mapOf("info" to Color(0xFFE0F0FF), "success" to Color(0xFFD4EDDA), "warning" to Color(0xFFFFF3CD), "danger" to Color(0xFFFFE0E0)); Surface(Modifier.fillMaxWidth().padding(bottom = 12.dp), color = bg[el["color"]?.toString()] ?: Color(0xFFE0F0FF), shape = MaterialTheme.shapes.small) { Text(el["message"]?.toString() ?: "", Modifier.padding(12.dp)) } }
        "BADGE" -> { val c = mapOf("primary" to Color(0xFF0D6EFD), "secondary" to Color(0xFF6C757D), "success" to Color(0xFF198754)); Surface(color = c[el["color"]?.toString()] ?: Color.Gray, shape = MaterialTheme.shapes.small) { Text(el["title"]?.toString() ?: "", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall) } }
        "SPACER" -> Spacer(Modifier.height(((el["width"] as? Number)?.toFloat() ?: 20f).dp))
        "PROGRESS" -> Column(Modifier.padding(bottom = 8.dp)) { el["label"]?.let { Text(it.toString(), modifier = Modifier.padding(bottom = 4.dp)) }; LinearProgressIndicator(progress = ((el["progress"] as? Number)?.toFloat() ?: 0f) / 100f, modifier = Modifier.fillMaxWidth()) }
        "INPUT" -> { val id = el["id"]?.toString() ?: ""; var txt by remember { mutableStateOf(data[id]?.toString() ?: "") }; OutlinedTextField(value = txt, onValueChange = { txt = it; data[id] = it; onUpdate?.invoke(mapOf(id to it)) }, label = { Text(el["label"]?.toString() ?: "") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) }
        "CHECKBOX" -> { val id = el["id"]?.toString() ?: ""; var ck by remember { mutableStateOf(data[id] == true) }; Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) { Checkbox(ck, { ck = it; data[id] = it; onUpdate?.invoke(mapOf(id to it)) }); Text(el["label"]?.toString() ?: "") } }
        "SELECT" -> { val id = el["id"]?.toString() ?: ""; var exp by remember { mutableStateOf(false) }; val vals = (el["values"] as? List<*>)?.mapNotNull { val m = it as? Map<*, *> ?: return@mapNotNull null; val vi = m["id"]?.toString() ?: return@mapNotNull null; vi to (m["displayName"]?.toString() ?: vi) } ?: emptyList(); val sel = vals.firstOrNull { it.first == data[id]?.toString() }?.second ?: ""; Box(Modifier.padding(bottom = 8.dp)) { OutlinedTextField(value = sel, onValueChange = {}, readOnly = true, label = { Text(el["label"]?.toString() ?: "") }, modifier = Modifier.fillMaxWidth()); DropdownMenu(expanded = exp, onDismissRequest = { exp = false }) { vals.forEach { (vi, nm) -> DropdownMenuItem(text = { Text(nm) }, onClick = { data[id] = vi; exp = false; onUpdate?.invoke(mapOf(id to vi)) }) } } } }
        "RATING" -> { val id = el["id"]?.toString() ?: ""; var rt by remember { mutableStateOf((data[id] as? Number)?.toInt() ?: 0) }; Column(Modifier.padding(bottom = 8.dp)) { el["label"]?.let { Text(it.toString()) }; Row { (1..5).forEach { n -> Text(if (n <= rt) "★" else "☆", color = Color(0xFFFFC107), fontSize = MaterialTheme.typography.headlineSmall.fontSize, modifier = Modifier.padding(4.dp).clickable { rt = n; data[id] = n; onUpdate?.invoke(mapOf(id to n)) }) } } } }
        "READONLY_FIELD" -> { val id = el["id"]?.toString() ?: ""; Column(Modifier.padding(bottom = 8.dp)) { el["label"]?.let { Text(it.toString(), fontWeight = FontWeight.Medium) }; Surface(color = Color(0xFFF8F9FA), shape = MaterialTheme.shapes.small) { Text(data[id]?.toString() ?: "—", Modifier.fillMaxWidth().padding(8.dp)) } } }
        "BUTTON" -> { val c = mapOf("primary" to MaterialTheme.colorScheme.primary, "secondary" to Color.Gray); Button(onClick = { onAction?.invoke(el["id"]?.toString() ?: "", null) }, colors = ButtonDefaults.buttonColors(containerColor = c[el["color"]?.toString()] ?: MaterialTheme.colorScheme.primary)) { Text(el["title"]?.toString() ?: el["id"]?.toString() ?: "") } }
    }
}
