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
        setContent { MaterialTheme { ContactsApp() } }
    }
}

@Composable
fun ContactsApp() {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED
        )
    }
    var reloadKey by remember { mutableStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) reloadKey++
    }

    if (!hasPermission) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.align(Alignment.Center).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text("DynamicLayout Contacts", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                Text("View your phone contacts via DynamicLayout engine.",
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) }) {
                    Text("Grant Contacts Permission")
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { hasPermission = true; reloadKey++ }) {
                    Text("Skip — Show Demo Render")
                }
            }
        }
    } else {
        ContactsView(context, loadKey = reloadKey, onReload = { reloadKey++ })
    }
}

@Composable
fun ContactsView(context: android.content.Context, loadKey: Int, onReload: () -> Unit) {
    var mode by remember { mutableStateOf("menu") } // menu | demo | contacts

    // ── Menu screen ──
    if (mode == "menu") {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.align(Alignment.Center).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text("DynamicLayout Demo", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                Text("Renderer is working. Choose what to render:",
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { mode = "demo" }) {
                    Text("Show Demo Render")
                }
                Spacer(Modifier.height(12.dp))
                Button(onClick = { mode = "contacts" }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF198754))) {
                    Text("Render My Contacts")
                }
            }
        }
        return
    }

    // ── Demo render ──
    if (mode == "demo") {
        RenderDemoLayout(onBack = { mode = "menu" })
        return
    }

    // ── Contacts render ──
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var filter by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(loadKey) {
        isLoading = true; errorMsg = null
        try {
            val result = withContext(Dispatchers.IO) { Contacts.load(context) }
            contacts = result
        } catch (t: Throwable) {
            errorMsg = "${t.javaClass.simpleName}: ${t.message}"
        }
        isLoading = false
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(); Spacer(Modifier.height(8.dp))
                Text("Loading contacts..."); Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = { mode = "menu" }) { Text("Cancel") }
            }
        }
        return
    }

    if (errorMsg != null) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Error loading contacts", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp)); Text(errorMsg ?: ""); Spacer(Modifier.height(16.dp))
                Button(onClick = { mode = "demo" }) { Text("Show Demo Instead") }
            }
        }
        return
    }

    val filtered = remember(filter, contacts) {
        if (filter.isBlank()) contacts else contacts.filter { it.name.contains(filter, ignoreCase = true) }
    }
    val stateData = remember { mutableStateMapOf<String, Any?>() }

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Contacts (${contacts.size})", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = { mode = "menu" }) { Text("Back") }
        }
        Spacer(Modifier.height(12.dp))

        // Search
        var searchText by remember { mutableStateOf(filter) }
        OutlinedTextField(value = searchText, onValueChange = { searchText = it; filter = it },
            label = { Text("Search contacts") }, singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))

        // Count badge
        Surface(color = Color(0xFF0D6EFD), shape = MaterialTheme.shapes.small, modifier = Modifier.padding(bottom = 12.dp)) {
            Text("${filtered.size} contacts shown (${contacts.size} total)", color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall)
        }

        filtered.take(20).forEachIndexed { i, c ->
            Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(c.name, fontWeight = FontWeight.SemiBold)
                    if (c.phone.isNotBlank()) ContactRow("Phone", c.phone)
                    if (c.email.isNotBlank()) ContactRow("Email", c.email)
                    if (c.organization.isNotBlank()) ContactRow("Org", c.organization)
                    if (c.address.isNotBlank()) ContactRow("Address", c.address)
                    if (c.note.isNotBlank()) ContactRow("Note", c.note)
                }
            }
        }
    }
}

@Composable
fun ContactRow(label: String, value: String) {
    Row(Modifier.padding(top = 2.dp)) {
        Text("$label: ", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall,
            color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

// ── Demo render (unchanged, verified working) ──

@Composable
fun RenderDemoLayout(onBack: () -> Unit) {
    val demoUi = mapOf(
        "title" to "DynamicLayout Demo",
        "layout" to listOf(
            mapOf("type" to "ALERT", "key" to "a1", "message" to "Renderer is working!", "color" to "success"),
            mapOf("type" to "INPUT", "key" to "i1", "id" to "name", "label" to "Your Name", "required" to true),
            mapOf("type" to "INPUT", "key" to "i2", "id" to "email", "label" to "Email"),
            mapOf("type" to "CHECKBOX", "key" to "cb1", "id" to "agree", "label" to "I agree"),
            mapOf("type" to "SELECT", "key" to "s1", "id" to "country", "label" to "Country",
                "values" to listOf(
                    mapOf("id" to "us", "displayName" to "USA"),
                    mapOf("id" to "de", "displayName" to "Germany")
                )),
            mapOf("type" to "RATING", "key" to "rt1", "id" to "stars", "label" to "Rate"),
            mapOf("type" to "BADGE", "key" to "bd1", "title" to "Badge", "color" to "primary"),
            mapOf("type" to "PROGRESS", "key" to "pr1", "progress" to 65)
        ),
        "actions" to listOf(
            mapOf("type" to "BUTTON", "key" to "b_save", "id" to "save", "title" to "Save", "color" to "primary"),
            mapOf("type" to "BUTTON", "key" to "b_back", "id" to "back", "title" to "Back", "color" to "secondary")
        ),
        "translations" to emptyMap<String, String>(), "userAccess" to mapOf("cancel" to true)
    )

    val stateData = remember { mutableStateMapOf<String, Any?>() }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text(demoUi["title"]?.toString() ?: "", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        (demoUi["layout"] as? List<*>)?.forEach { el ->
            RenderEl((el as? Map<String, Any?>) ?: emptyMap(), stateData)
        }
        val actions = demoUi["actions"] as? List<*>
        if (!actions.isNullOrEmpty()) {
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                actions.forEach { el ->
                    RenderEl((el as? Map<String, Any?>) ?: emptyMap(), stateData, onAction = { id, _ ->
                        if (id == "back") onBack()
                    })
                }
            }
        }
    }
}

// ── Contacts loader ──

data class Contact(val id: String, val name: String, val phone: String = "", val email: String = "",
                   val organization: String = "", val address: String = "", val note: String = "")

object Contacts {
    fun load(context: android.content.Context): List<Contact> {
        val contacts = mutableListOf<Contact>()
        try {
            val uri = android.provider.ContactsContract.Contacts.CONTENT_URI
            val cursor = context.contentResolver.query(uri, null, null, null, "${android.provider.ContactsContract.Contacts.DISPLAY_NAME} ASC")
            if (cursor == null) return contacts
            cursor.use {
                val idIdx = it.getColumnIndex(android.provider.ContactsContract.Contacts._ID)
                val nmIdx = it.getColumnIndex(android.provider.ContactsContract.Contacts.DISPLAY_NAME)
                val hasPhoneIdx = it.getColumnIndex(android.provider.ContactsContract.Contacts.HAS_PHONE_NUMBER)
                if (idIdx < 0 || nmIdx < 0) return contacts
                while (it.moveToNext() && contacts.size < 20) {
                    contacts.add(Contact(
                        id = it.getString(idIdx) ?: "",
                        name = it.getString(nmIdx) ?: "Unknown",
                        phone = if (hasPhoneIdx >= 0 && it.getInt(hasPhoneIdx) > 0) "Has phone" else ""
                    ))
                }
            }
        } catch (e: Exception) {
            contacts.add(Contact("err", "Error: ${e.javaClass.simpleName}", e.message ?: ""))

        }
        return contacts
    }
}

// ── DynamicLayout Renderer (verified working) ──

@Composable
fun RenderEl(el: Map<String, Any?>, data: MutableMap<String, Any?>,
             onUpdate: ((Map<String, Any?>) -> Unit)? = null,
             onAction: ((String, Map<String, Any?>?) -> Unit)? = null) {
    val t = el["type"]?.toString() ?: ""

    @Composable fun Children() {
        (el["content"] as? List<*>)?.forEach {
            RenderEl((it as? Map<String, Any?>) ?: emptyMap(), data, onUpdate, onAction)
        }
    }

    when (t) {
        "ROW" -> Row(Modifier.fillMaxWidth()) { Children() }
        "COL" -> Column { Children() }
        "GROUP" -> Column { Children() }
        "INLINE_GROUP" -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Children() }
        "FIELDSET" -> Card(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            Column(Modifier.padding(12.dp)) { el["title"]?.let { Text(it.toString(), fontWeight = FontWeight.SemiBold) }; Children() }
        }
        "LABEL" -> Text(el["label"]?.toString() ?: "", Modifier.padding(bottom = 4.dp), fontWeight = FontWeight.Medium)
        "ALERT" -> {
            val bg = mapOf("info" to Color(0xFFE0F0FF), "success" to Color(0xFFD4EDDA), "warning" to Color(0xFFFFF3CD), "danger" to Color(0xFFFFE0E0))
            Surface(Modifier.fillMaxWidth().padding(bottom = 12.dp), color = bg[el["color"]?.toString()] ?: Color(0xFFE0F0FF), shape = MaterialTheme.shapes.small) {
                Text(el["message"]?.toString() ?: "", Modifier.padding(12.dp))
            }
        }
        "BADGE" -> {
            val c = mapOf("primary" to Color(0xFF0D6EFD), "secondary" to Color(0xFF6C757D), "success" to Color(0xFF198754))
            Surface(color = c[el["color"]?.toString()] ?: Color.Gray, shape = MaterialTheme.shapes.small) {
                Text(el["title"]?.toString() ?: "", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
            }
        }
        "SPACER" -> Spacer(Modifier.height(((el["width"] as? Number)?.toFloat() ?: 20f).dp))
        "PROGRESS" -> Column(Modifier.padding(bottom = 8.dp)) {
            el["label"]?.let { Text(it.toString(), modifier = Modifier.padding(bottom = 4.dp)) }
            LinearProgressIndicator(progress = ((el["progress"] as? Number)?.toFloat() ?: 0f) / 100f, modifier = Modifier.fillMaxWidth())
        }
        "INPUT" -> {
            val id = el["id"]?.toString() ?: ""; var text by remember { mutableStateOf(data[id]?.toString() ?: "") }
            OutlinedTextField(value = text, onValueChange = { text = it; data[id] = it; onUpdate?.invoke(mapOf(id to it)) },
                label = { Text(el["label"]?.toString() ?: "") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
        }
        "CHECKBOX" -> {
            val id = el["id"]?.toString() ?: ""; var ck by remember { mutableStateOf(data[id] == true) }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                Checkbox(ck, { ck = it; data[id] = it; onUpdate?.invoke(mapOf(id to it)) }); Text(el["label"]?.toString() ?: "")
            }
        }
        "SELECT" -> {
            val id = el["id"]?.toString() ?: ""; var expanded by remember { mutableStateOf(false) }
            val values = (el["values"] as? List<*>)?.mapNotNull { val m = it as? Map<*, *> ?: return@mapNotNull null; val vId = m["id"]?.toString() ?: return@mapNotNull null; vId to (m["displayName"]?.toString() ?: vId) } ?: emptyList()
            val sel = values.firstOrNull { it.first == data[id]?.toString() }?.second ?: ""
            Box(Modifier.padding(bottom = 8.dp)) {
                OutlinedTextField(value = sel, onValueChange = {}, readOnly = true, label = { Text(el["label"]?.toString() ?: "") }, modifier = Modifier.fillMaxWidth())
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    values.forEach { (vid, name) -> DropdownMenuItem(text = { Text(name) }, onClick = { data[id] = vid; expanded = false; onUpdate?.invoke(mapOf(id to vid)) }) }
                }
            }
        }
        "RATING" -> {
            val id = el["id"]?.toString() ?: ""; var rating by remember { mutableStateOf((data[id] as? Number)?.toInt() ?: 0) }
            Column(Modifier.padding(bottom = 8.dp)) {
                el["label"]?.let { Text(it.toString()) }
                Row { (1..5).forEach { n -> Text(if (n <= rating) "★" else "☆", color = Color(0xFFFFC107), fontSize = MaterialTheme.typography.headlineSmall.fontSize, modifier = Modifier.padding(4.dp).clickable { rating = n; data[id] = n; onUpdate?.invoke(mapOf(id to n)) }) }}
            }
        }
        "READONLY_FIELD" -> { val id = el["id"]?.toString() ?: ""
            Column(Modifier.padding(bottom = 8.dp)) {
                el["label"]?.let { Text(it.toString(), fontWeight = FontWeight.Medium) }
                Surface(color = Color(0xFFF8F9FA), shape = MaterialTheme.shapes.small) { Text(data[id]?.toString() ?: "—", Modifier.fillMaxWidth().padding(8.dp)) }
            }
        }
        "BUTTON" -> { val c = mapOf("primary" to MaterialTheme.colorScheme.primary, "secondary" to Color.Gray)
            Button(onClick = { onAction?.invoke(el["id"]?.toString() ?: "", el["responseAction"] as? Map<String, Any?>) },
                colors = ButtonDefaults.buttonColors(containerColor = c[el["color"]?.toString()] ?: MaterialTheme.colorScheme.primary)) {
                Text(el["title"]?.toString() ?: el["id"]?.toString() ?: "")
            }
        }
        else -> Text("Unknown: $t", color = Color.Red, modifier = Modifier.padding(bottom = 4.dp))
    }
}
