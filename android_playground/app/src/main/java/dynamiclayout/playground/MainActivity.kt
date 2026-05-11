package dynamiclayout.playground

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { App() } }
    }
}

@Composable fun App() {
    val ctx = LocalContext.current
    var perm by remember { mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) }
    var key by remember { mutableStateOf(0) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { g -> perm = g; if (g) key++ }

    if (!perm) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("DynamicLayout Contacts", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                Text("View your phone contacts via DynamicLayout engine.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { launcher.launch(Manifest.permission.READ_CONTACTS) }) { Text("Grant Contacts Permission") }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { perm = true; key++ }) { Text("Skip — Show Demo") }
            }
        }
    } else {
        MainScreen(ctx, key) { key++ }
    }
}

@Composable fun MainScreen(ctx: android.content.Context, key: Int, reload: () -> Unit) {
    var page by remember { mutableStateOf("menu") }

    if (page == "menu") {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("DynamicLayout Demo", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                Text("Renderer is working. Choose:", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { page = "demo" }) { Text("Show Demo Render") }
                Spacer(Modifier.height(12.dp))
                Button(onClick = { page = "contacts" }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF198754))) {
                    Text("Render My Contacts (step by step)")
                }
            }
        }
        return
    }

    if (page == "demo") {
        DemoPage(onBack = { page = "menu" })
        return
    }

    if (page == "contacts") {
        ContactsPage(ctx, onBack = { page = "menu" }, reload)
        return
    }
}

// ── Demo page (verified working) ──

@Composable fun DemoPage(onBack: () -> Unit) {
    val layout = listOf(
        mapOf("type" to "ALERT", "key" to "a1", "message" to "Renderer is working!", "color" to "success"),
        mapOf("type" to "INPUT", "key" to "i1", "id" to "name", "label" to "Your Name", "required" to true),
        mapOf("type" to "INPUT", "key" to "i2", "id" to "email", "label" to "Email"),
        mapOf("type" to "CHECKBOX", "key" to "cb1", "id" to "agree", "label" to "I agree"),
        mapOf("type" to "SELECT", "key" to "s1", "id" to "country", "label" to "Country", "values" to listOf(mapOf("id" to "us", "displayName" to "USA"), mapOf("id" to "de", "displayName" to "Germany"))),
        mapOf("type" to "RATING", "key" to "rt1", "id" to "stars", "label" to "Rate"),
        mapOf("type" to "BADGE", "key" to "bd1", "title" to "Badge", "color" to "primary"),
        mapOf("type" to "PROGRESS", "key" to "pr1", "progress" to 65)
    )
    val actions = listOf(mapOf("type" to "BUTTON", "key" to "b1", "id" to "save", "title" to "Save", "color" to "primary"), mapOf("type" to "BUTTON", "key" to "b2", "id" to "back", "title" to "Back", "color" to "secondary"))
    val data = remember { mutableStateMapOf<String, Any?>() }

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("DynamicLayout Demo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        layout.forEach { RenderEl(it, data) }
        Divider(modifier = Modifier.padding(vertical = 12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { actions.forEach { RenderEl(it, data, onAction = { id, _ -> if (id == "back") onBack() }) } }
    }
}

// ── Contacts page (step by step) ──

data class Contact(val id: String, val name: String, val phone: String = "")

@Composable fun ContactsPage(ctx: android.content.Context, onBack: () -> Unit, reload: () -> Unit) {
    var step by remember { mutableStateOf(1) }
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var err by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(step) {
        if (step != 2) return@LaunchedEffect
        try {
            val r = withContext(Dispatchers.IO) { loadContacts(ctx) }
            contacts = r; step = 3
        } catch (t: Throwable) { err = "${t.javaClass.simpleName}: ${t.message}" }
    }

    when (step) {
        1 -> {
            var dbg by remember { mutableStateOf("") }
            Box(Modifier.fillMaxSize().padding(16.dp)) { Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Step 1: Render test", style = MaterialTheme.typography.headlineSmall); Spacer(Modifier.height(8.dp))
                Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) { Column(Modifier.padding(12.dp)) { Text("Test Contact", fontWeight = FontWeight.SemiBold); Text("Phone: +1 234 567", style = MaterialTheme.typography.bodySmall, color = Color.Gray) } }
                if (dbg.isNotBlank()) { Text(dbg, color = if (dbg.startsWith("OK")) Color(0xFF198754) else MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
                Spacer(Modifier.height(16.dp))
                Button(onClick = { try { val l = emptyList<Contact>(); dbg = "OK emptyList: ${l.size}" } catch (t: Throwable) { dbg = "${t.javaClass.simpleName}: ${t.message}" } }) { Text("Test emptyList") }
                Spacer(Modifier.height(4.dp))
                Button(onClick = { try { val c = ctx.contentResolver.query(android.provider.ContactsContract.Contacts.CONTENT_URI, null, null, null, null); c?.close(); dbg = "OK query+close: cursor=${c != null}" } catch (t: Throwable) { dbg = "${t.javaClass.simpleName}: ${t.message}" } }) { Text("Test query()+close()") }
                Spacer(Modifier.height(4.dp))
                Button(onClick = { try { val c = ctx.contentResolver.query(android.provider.ContactsContract.Contacts.CONTENT_URI, null, null, null, null); val list = mutableListOf<Contact>(); c?.use { val ii = it.getColumnIndex(android.provider.ContactsContract.Contacts._ID); val ni = it.getColumnIndex(android.provider.ContactsContract.Contacts.DISPLAY_NAME); if (ii >= 0 && ni >= 0 && it.moveToFirst()) list.add(Contact(it.getString(ii)?:"", it.getString(ni)?:"?")) }; dbg = "OK read 1: ${list.size}" } catch (t: Throwable) { dbg = "${t.javaClass.simpleName}: ${t.message}" } }) { Text("Test read 1st contact") }
                Spacer(Modifier.height(4.dp))
                Button(onClick = { try { val list = loadContacts(ctx); contacts = list; dbg = "OK read all: ${list.size}"; if (list.isNotEmpty()) step = 3 } catch (t: Throwable) { dbg = "${t.javaClass.simpleName}: ${t.message}" } }) { Text("Test read all + show") }
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = onBack) { Text("Back") }
            }}
        }
        2 -> { // Step 2: loading
            Box(Modifier.fillMaxSize()) {
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text("Loading contacts...")
                    if (err != null) {
                        Spacer(Modifier.height(16.dp))
                        Text(err ?: "", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { step = 1; err = null }) { Text("Back to test") }
                    }
                }
            }
        }
        3 -> { // Step 3: show contacts
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Contacts (${contacts.size})", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    OutlinedButton(onClick = onBack) { Text("Back") }
                }
                Spacer(Modifier.height(12.dp))
                contacts.take(20).forEach { c ->
                    Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(c.name, fontWeight = FontWeight.SemiBold)
                            if (c.phone.isNotBlank()) Text("Phone: ${c.phone}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = reload) { Text("Refresh") }
            }
        }
    }
}

private fun loadContacts(ctx: android.content.Context): List<Contact> {
    val list = mutableListOf<Contact>()
    val c = ctx.contentResolver.query(android.provider.ContactsContract.Contacts.CONTENT_URI, null, null, null, null) ?: return list
    c.use {
        val idIdx = it.getColumnIndex(android.provider.ContactsContract.Contacts._ID)
        val nmIdx = it.getColumnIndex(android.provider.ContactsContract.Contacts.DISPLAY_NAME)
        if (idIdx < 0 || nmIdx < 0) return list
        while (it.moveToNext() && list.size < 20) {
            list.add(Contact(it.getString(idIdx) ?: "", it.getString(nmIdx) ?: "?"))
        }
    }
    return list
}

// ── DynamicLayout Renderer ──

@Composable fun RenderEl(el: Map<String, Any?>, data: MutableMap<String, Any?>, onUpdate: ((Map<String, Any?>) -> Unit)? = null, onAction: ((String, Map<String, Any?>?) -> Unit)? = null) {
    val t = el["type"]?.toString() ?: ""
    @Composable fun Children() { (el["content"] as? List<*>)?.forEach { RenderEl((it as? Map<String, Any?>) ?: emptyMap(), data, onUpdate, onAction) } }
    when (t) {
        "ROW" -> Row(Modifier.fillMaxWidth()) { Children() }
        "COL" -> Column { Children() }
        "GROUP" -> Column { Children() }
        "INLINE_GROUP" -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Children() }
        "FIELDSET" -> Card(Modifier.fillMaxWidth().padding(bottom = 12.dp)) { Column(Modifier.padding(12.dp)) { el["title"]?.let { Text(it.toString(), fontWeight = FontWeight.SemiBold) }; Children() } }
        "LABEL" -> Text(el["label"]?.toString() ?: "", Modifier.padding(bottom = 4.dp), fontWeight = FontWeight.Medium)
        "ALERT" -> { val bg = mapOf("info" to Color(0xFFE0F0FF), "success" to Color(0xFFD4EDDA), "warning" to Color(0xFFFFF3CD), "danger" to Color(0xFFFFE0E0)); Surface(Modifier.fillMaxWidth().padding(bottom = 12.dp), color = bg[el["color"]?.toString()] ?: Color(0xFFE0F0FF), shape = MaterialTheme.shapes.small) { Text(el["message"]?.toString() ?: "", Modifier.padding(12.dp)) } }
        "BADGE" -> { val c = mapOf("primary" to Color(0xFF0D6EFD), "secondary" to Color(0xFF6C757D), "success" to Color(0xFF198754)); Surface(color = c[el["color"]?.toString()] ?: Color.Gray, shape = MaterialTheme.shapes.small) { Text(el["title"]?.toString() ?: "", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall) } }
        "SPACER" -> Spacer(Modifier.height(((el["width"] as? Number)?.toFloat() ?: 20f).dp))
        "PROGRESS" -> Column(Modifier.padding(bottom = 8.dp)) { el["label"]?.let { Text(it.toString(), modifier = Modifier.padding(bottom = 4.dp)) }; LinearProgressIndicator(progress = ((el["progress"] as? Number)?.toFloat() ?: 0f) / 100f, modifier = Modifier.fillMaxWidth()) }
        "INPUT" -> { val id = el["id"]?.toString() ?: ""; var text by remember { mutableStateOf(data[id]?.toString() ?: "") }; OutlinedTextField(value = text, onValueChange = { text = it; data[id] = it; onUpdate?.invoke(mapOf(id to it)) }, label = { Text(el["label"]?.toString() ?: "") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) }
        "CHECKBOX" -> { val id = el["id"]?.toString() ?: ""; var ck by remember { mutableStateOf(data[id] == true) }; Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) { Checkbox(ck, { ck = it; data[id] = it; onUpdate?.invoke(mapOf(id to it)) }); Text(el["label"]?.toString() ?: "") } }
        "SELECT" -> { val id = el["id"]?.toString() ?: ""; var expanded by remember { mutableStateOf(false) }; val vals = (el["values"] as? List<*>)?.mapNotNull { val m = it as? Map<*, *> ?: return@mapNotNull null; val vId = m["id"]?.toString() ?: return@mapNotNull null; vId to (m["displayName"]?.toString() ?: vId) } ?: emptyList(); val sel = vals.firstOrNull { it.first == data[id]?.toString() }?.second ?: ""; Box(Modifier.padding(bottom = 8.dp)) { OutlinedTextField(value = sel, onValueChange = {}, readOnly = true, label = { Text(el["label"]?.toString() ?: "") }, modifier = Modifier.fillMaxWidth()); DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) { vals.forEach { (vid, nm) -> DropdownMenuItem(text = { Text(nm) }, onClick = { data[id] = vid; expanded = false; onUpdate?.invoke(mapOf(id to vid)) }) } } } }
        "RATING" -> { val id = el["id"]?.toString() ?: ""; var rating by remember { mutableStateOf((data[id] as? Number)?.toInt() ?: 0) }; Column(Modifier.padding(bottom = 8.dp)) { el["label"]?.let { Text(it.toString()) }; Row { (1..5).forEach { n -> Text(if (n <= rating) "★" else "☆", color = Color(0xFFFFC107), fontSize = MaterialTheme.typography.headlineSmall.fontSize, modifier = Modifier.padding(4.dp).clickable { rating = n; data[id] = n; onUpdate?.invoke(mapOf(id to n)) }) } } } }
        "READONLY_FIELD" -> { val id = el["id"]?.toString() ?: ""; Column(Modifier.padding(bottom = 8.dp)) { el["label"]?.let { Text(it.toString(), fontWeight = FontWeight.Medium) }; Surface(color = Color(0xFFF8F9FA), shape = MaterialTheme.shapes.small) { Text(data[id]?.toString() ?: "—", Modifier.fillMaxWidth().padding(8.dp)) } } }
        "BUTTON" -> { val c = mapOf("primary" to MaterialTheme.colorScheme.primary, "secondary" to Color.Gray); Button(onClick = { onAction?.invoke(el["id"]?.toString() ?: "", el["responseAction"] as? Map<String, Any?>) }, colors = ButtonDefaults.buttonColors(containerColor = c[el["color"]?.toString()] ?: MaterialTheme.colorScheme.primary)) { Text(el["title"]?.toString() ?: el["id"]?.toString() ?: "") } }
        else -> Text("Unknown: $t", color = Color.Red, modifier = Modifier.padding(bottom = 4.dp))
    }
}
