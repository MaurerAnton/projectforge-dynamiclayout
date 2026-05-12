package dynamiclayout.playground

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable fun ContactsPage(ctx: android.content.Context, onBack: () -> Unit, reload: () -> Unit) {
    var step by remember { mutableStateOf(1) }
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var err by remember { mutableStateOf<String?>(null) }
    var dbg by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf("name") }

    // Step 1: debug menu
    if (step == 1) {
        Box(Modifier.fillMaxSize().padding(16.dp)) { Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Step 1: Render test", style = MaterialTheme.typography.headlineSmall); Spacer(Modifier.height(8.dp))
            Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) { Column(Modifier.padding(12.dp)) { Text("Test Contact", fontWeight = FontWeight.SemiBold); Text("Phone: +1 234 567", style = MaterialTheme.typography.bodySmall, color = Color.Gray) } }
            if (dbg.isNotBlank()) Text(dbg, color = if (dbg.startsWith("OK")) Color(0xFF198754) else MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(12.dp))
            Button(onClick = { try { contacts = Contacts.loadBasic(ctx); dbg = "OK names: ${contacts.size}"; step = 3 } catch (t: Throwable) { dbg = "${t.javaClass.simpleName}: ${t.message}" } }) { Text("Load names only") }
            Spacer(Modifier.height(4.dp))
            Button(onClick = { try { contacts = Contacts.loadFull(ctx); dbg = "OK full: ${contacts.size} contacts, phones=${contacts.sumOf{it.phones.size}} emails=${contacts.sumOf{it.emails.size}}"; step = 3 } catch (t: Throwable) { dbg = "${t.javaClass.simpleName}: ${t.message}" } }) { Text("Load all fields") }
            Spacer(Modifier.height(4.dp))
            Button(onClick = { try { contacts = Contacts.loadFull(ctx); step = 4 } catch (t: Throwable) { dbg = "${t.javaClass.simpleName}: ${t.message}" } }) { Text("Load + render DynamicLayout") }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onBack) { Text("Back") }
        }}
        return
    }

    // Step 3: plain Card rendering
    if (step == 3) {
        val sorted = remember(contacts, sort) { when (sort) { "-name" -> contacts.sortedByDescending { it.name.lowercase() }; else -> contacts.sortedBy { it.name.lowercase() } } }
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Contacts (${contacts.size})", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); OutlinedButton(onClick = onBack) { Text("Back") } }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { listOf("name" to "A-Z", "-name" to "Z-A").forEach { (k, l) -> TextButton(onClick = { sort = k }) { Text(l, color = if (sort == k) MaterialTheme.colorScheme.primary else Color.Gray, style = MaterialTheme.typography.labelSmall) } } }
            Spacer(Modifier.height(8.dp))
            sorted.take(100).forEach { c -> Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) { Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (c.photo != null) { val bmp = remember(c.photo) { try { BitmapFactory.decodeByteArray(c.photo, 0, c.photo!!.size) } catch (_: Exception) { null } }; if (bmp != null) { Image(bitmap = bmp.asImageBitmap(), contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape)); Spacer(Modifier.width(8.dp)) } }
                    Text(c.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                }
                c.phones.forEach { p -> val icon = c.phoneTypes[p] ?: "📞"; Text("$icon $p", style = MaterialTheme.typography.bodySmall) }
                c.emails.forEach { e -> Text("✉ $e", style = MaterialTheme.typography.bodySmall) }
                if (c.org.isNotBlank()) Text("🏢 ${c.org}", style = MaterialTheme.typography.bodySmall)
                if (c.addr.isNotBlank()) Text("📍 ${c.addr}", style = MaterialTheme.typography.bodySmall)
                if (c.note.isNotBlank()) Text("📝 ${c.note}", style = MaterialTheme.typography.bodySmall)
            } } }
        }
        return
    }

    // Step 4: DynamicLayout rendering
    var filter by remember { mutableStateOf("") }
    if (step == 4) {
        val filtered = remember(filter, contacts) { if (filter.isBlank()) contacts else contacts.filter { it.name.contains(filter, ignoreCase = true) } }
        val layout = remember(filtered) { buildLayout(filtered, filter) }
        val data = remember { mutableStateMapOf<String, Any?>() }
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(layout["title"]?.toString() ?: "", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); OutlinedButton(onClick = onBack) { Text("Back") } }
            Spacer(Modifier.height(8.dp))
            (layout["layout"] as? List<*>)?.forEach { el -> RenderEl((el as? Map<String, Any?>) ?: emptyMap(), data, onUpdate = { u -> val s = u["search"] as? String ?: ""; if (s != filter) filter = s }) }
            val actions = layout["actions"] as? List<*>; if (!actions.isNullOrEmpty()) { Divider(Modifier.padding(vertical = 12.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { actions.forEach { el -> RenderEl((el as? Map<String, Any?>) ?: emptyMap(), data) } } }
        }
    }
}

// ── DynamicLayout JSON generator for contacts ──

fun buildLayout(contacts: List<Contact>, filter: String): Map<String, Any> {
    val layout = mutableListOf<Map<String, Any>>()
    layout.add(mapOf("type" to "INPUT", "key" to "search", "id" to "search", "label" to "Search", "dataType" to "STRING"))
    layout.add(mapOf("type" to "INLINE_GROUP", "key" to "stats", "content" to listOf(
        mapOf("type" to "BADGE", "key" to "cnt", "title" to "${contacts.size} contacts", "color" to "primary"),
        mapOf("type" to "SPACER", "key" to "sp1", "width" to 8),
        mapOf("type" to "LABEL", "key" to "hint", "label" to "Rendered via DynamicLayout engine")
    )))
    layout.add(mapOf("type" to "SPACER", "key" to "sp2", "width" to 8))
    if (contacts.isEmpty()) layout.add(mapOf("type" to "ALERT", "key" to "empty", "message" to "No contacts", "color" to "warning"))
    contacts.take(30).forEachIndexed { i, c ->
        val fields = mutableListOf<Map<String, Any>>()
        fields.add(mapOf("type" to "ALERT", "key" to "nm$i", "message" to c.name, "color" to "info"))
        c.phones.forEach { p -> val icon = c.phoneTypes[p] ?: "📞"; fields.add(mapOf("type" to "READONLY_FIELD", "key" to "ph${i}_$p", "id" to "phone_${i}_${fields.size}", "label" to "$icon $p")) }
        c.emails.forEach { e -> fields.add(mapOf("type" to "READONLY_FIELD", "key" to "em${i}_$e", "id" to "email_${i}_${fields.size}", "label" to "✉ $e")) }
        if (c.org.isNotBlank()) fields.add(mapOf("type" to "READONLY_FIELD", "key" to "org$i", "id" to "org_$i", "label" to "🏢 ${c.org}"))
        if (c.addr.isNotBlank()) fields.add(mapOf("type" to "READONLY_FIELD", "key" to "adr$i", "id" to "addr_$i", "label" to "📍 ${c.addr}"))
        if (c.note.isNotBlank()) fields.add(mapOf("type" to "READONLY_FIELD", "key" to "note$i", "id" to "note_$i", "label" to "📝 ${c.note.take(80)}"))
        layout.add(mapOf("type" to "FIELDSET", "key" to "fs_$i", "title" to c.name, "content" to fields))
    }
    return mapOf("title" to "Contacts (${contacts.size}) via DynamicLayout", "layout" to layout, "actions" to listOf(mapOf("type" to "BUTTON", "key" to "back", "id" to "back", "title" to "Back", "color" to "secondary")), "translations" to emptyMap<String, String>(), "userAccess" to mapOf("cancel" to true))
}
