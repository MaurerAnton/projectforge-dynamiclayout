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
        Box(Modifier.fillMaxSize()) { Column(Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("DynamicLayout Contacts", style = MaterialTheme.typography.headlineSmall); Spacer(Modifier.height(16.dp))
            Text("View your phone contacts.", style = MaterialTheme.typography.bodyMedium); Spacer(Modifier.height(24.dp))
            Button(onClick = { launcher.launch(Manifest.permission.READ_CONTACTS) }) { Text("Grant Permission") }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { perm = true; key++ }) { Text("Skip — Show Demo") }
        }}
    } else { MainScreen(ctx, key) { key++ } }
}

@Composable fun MainScreen(ctx: android.content.Context, key: Int, reload: () -> Unit) {
    var page by remember { mutableStateOf("menu") }
    if (page == "menu") {
        Box(Modifier.fillMaxSize()) { Column(Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("DynamicLayout Demo", style = MaterialTheme.typography.headlineSmall); Spacer(Modifier.height(16.dp))
            Text("Choose:", style = MaterialTheme.typography.bodyMedium); Spacer(Modifier.height(24.dp))
            Button(onClick = { page = "demo" }) { Text("Show Demo Render") }; Spacer(Modifier.height(12.dp))
            Button(onClick = { page = "contacts" }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF198754))) { Text("Render My Contacts (debug)") }
        }}
    } else if (page == "demo") { DemoPage { page = "menu" } }
    else if (page == "contacts") { ContactsPage(ctx, { page = "menu" }) }
}

// ── Demo ──
@Composable fun DemoPage(onBack: () -> Unit) {
    val layout = listOf(
        mapOf("type" to "ALERT", "key" to "a", "message" to "Renderer works!", "color" to "success"),
        mapOf("type" to "INPUT", "key" to "i1", "id" to "name", "label" to "Name", "required" to true),
        mapOf("type" to "INPUT", "key" to "i2", "id" to "email", "label" to "Email"),
        mapOf("type" to "CHECKBOX", "key" to "cb", "id" to "agree", "label" to "I agree"),
        mapOf("type" to "SELECT", "key" to "s", "id" to "country", "label" to "Country", "values" to listOf(mapOf("id" to "us", "displayName" to "USA"), mapOf("id" to "de", "displayName" to "Germany"))),
        mapOf("type" to "RATING", "key" to "rt", "id" to "stars", "label" to "Rate"),
        mapOf("type" to "BADGE", "key" to "bd", "title" to "Badge", "color" to "primary"),
        mapOf("type" to "PROGRESS", "key" to "pr", "progress" to 65)
    )
    val data = remember { mutableStateMapOf<String, Any?>() }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Demo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Spacer(Modifier.height(16.dp))
        layout.forEach { RenderEl(it, data) }
        Divider(Modifier.padding(vertical = 12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RenderEl(mapOf("type" to "BUTTON", "key" to "b1", "id" to "save", "title" to "Save", "color" to "primary"), data)
            RenderEl(mapOf("type" to "BUTTON", "key" to "b2", "id" to "back", "title" to "Back", "color" to "secondary"), data, onAction = { _, _ -> onBack() })
        }
    }
}

// ── Contacts debug ──
data class Contact(val id: String, val name: String, val phone: String = "")

@Composable fun ContactsPage(ctx: android.content.Context, onBack: () -> Unit) {
    var step by remember { mutableStateOf(1) }
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var err by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(step) { /* unused */ }

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)) {
        if (err != null) { Text("Error: ${err}", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp)) }
    when (step) {
        1 -> { Text("Step 1: Render test", style = MaterialTheme.typography.headlineSmall); Spacer(Modifier.height(8.dp))
                Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) { Column(Modifier.padding(12.dp)) { Text("Test Contact", fontWeight = FontWeight.SemiBold); Text("Phone: +1 234 567", style = MaterialTheme.typography.bodySmall, color = Color.Gray) } }
                Spacer(Modifier.height(24.dp))
                Button(onClick = {
                    try { contacts = emptyList(); step = 22 } catch (t: Throwable) { err = t.message }
                }) { Text("Step 2a: Create empty list") }
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    try { ctx.contentResolver.query(android.provider.ContactsContract.Contacts.CONTENT_URI, null, null, null, null)?.close(); step = 23 } catch (t: Throwable) { err = t.message }
                }) { Text("Step 2b: Open cursor + close") }
                OutlinedButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) { Text("Back") } }
            22 -> { Text("Step 2a: Empty list — OK (${contacts.size})", style = MaterialTheme.typography.headlineSmall); Spacer(Modifier.height(8.dp))
                Button(onClick = { try { ctx.contentResolver.query(android.provider.ContactsContract.Contacts.CONTENT_URI, null, null, null, null)?.close(); step = 23 } catch (t: Throwable) { err = t.message } }) { Text("Step 2b: Open + close cursor") } }
            23 -> { Text("Step 2b: Cursor OK", style = MaterialTheme.typography.headlineSmall); Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    try { val c = ctx.contentResolver.query(android.provider.ContactsContract.Contacts.CONTENT_URI, null, null, null, null); val list = mutableListOf<Contact>(); c?.use { val ii = it.getColumnIndex(android.provider.ContactsContract.Contacts._ID); val ni = it.getColumnIndex(android.provider.ContactsContract.Contacts.DISPLAY_NAME); if (ii >= 0 && ni >= 0 && it.moveToFirst()) list.add(Contact(it.getString(ii)?:"", it.getString(ni)?:"?")) }; contacts = list; step = 24 } catch (t: Throwable) { err = t.message }
                }) { Text("Step 2c: Read 1st contact") } }
            24 -> { Text("Step 2c: 1 contact read (${contacts.size})", style = MaterialTheme.typography.headlineSmall); Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    try { contacts = loadContacts(ctx); step = 25 } catch (t: Throwable) { err = t.message }
                }) { Text("Step 2d: Read all contacts") } }
            25 -> if (err != null) {
                Box(Modifier.fillMaxSize()) { Column(Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error", color = MaterialTheme.colorScheme.error); Spacer(Modifier.height(8.dp))
                    Text(err ?: ""); Spacer(Modifier.height(16.dp))
                    Button(onClick = { err = null; contacts = emptyList(); step = 1 }) { Text("Back to start") }
                }}
            } else {
                Text("Contacts: ${contacts.size}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                contacts.take(20).forEach { c -> Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) { Column(Modifier.padding(12.dp)) { Text(c.name, fontWeight = FontWeight.SemiBold); if (c.phone.isNotBlank()) Text("Phone: ${c.phone}", style = MaterialTheme.typography.bodySmall, color = Color.Gray) } } }
                Spacer(Modifier.height(16.dp)); OutlinedButton(onClick = onBack) { Text("Back") }
            }
        }
    }
}

@Composable fun DebugStep(title: String, desc: String) {
    Box(Modifier.fillMaxSize().padding(32.dp)) { Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.headlineSmall); Spacer(Modifier.height(8.dp))
        Text(desc, style = MaterialTheme.typography.bodySmall, color = Color.Gray); Spacer(Modifier.height(24.dp))
    }
}

private fun loadContacts(ctx: android.content.Context): List<Contact> {
    val list = mutableListOf<Contact>()
    val c = ctx.contentResolver.query(android.provider.ContactsContract.Contacts.CONTENT_URI, null, null, null, null) ?: return list
    c.use { val ii = it.getColumnIndex(android.provider.ContactsContract.Contacts._ID); val ni = it.getColumnIndex(android.provider.ContactsContract.Contacts.DISPLAY_NAME); if (ii < 0 || ni < 0) return list; while (it.moveToNext() && list.size < 20) list.add(Contact(it.getString(ii) ?: "", it.getString(ni) ?: "?")) }
    return list
}

// ── Renderer ──
@Composable fun RenderEl(el: Map<String, Any?>, data: MutableMap<String, Any?>, onUpdate: ((Map<String, Any?>) -> Unit)? = null, onAction: ((String, Map<String, Any?>?) -> Unit)? = null) {
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
