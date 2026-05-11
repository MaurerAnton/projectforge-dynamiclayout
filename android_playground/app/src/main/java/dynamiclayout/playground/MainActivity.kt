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
                Text("View your phone contacts rendered via DynamicLayout engine.",
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) }) {
                    Text("Grant Contacts Permission")
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { hasPermission = true; reloadKey++ }) {
                    Text("Skip — Show Demo")
                }
            }
        }
    } else {
        ContactsView(context, loadKey = reloadKey, onReload = { reloadKey++ })
    }
}

@Composable
fun ContactsView(context: android.content.Context, loadKey: Int, onReload: () -> Unit) {
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var filter by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showDemo by remember { mutableStateOf(false) }

    LaunchedEffect(loadKey) {
        if (showDemo) return@LaunchedEffect
        isLoading = true; errorMsg = null
        try {
            val result = withContext(Dispatchers.IO) {
                Contacts.load(context)
            }
            contacts = result
        } catch (t: Throwable) {
            errorMsg = "${t.javaClass.simpleName}: ${t.message}"
            showDemo = true
        }
        isLoading = false
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(); Spacer(Modifier.height(8.dp)); Text("Loading contacts...")
            }
        }
        return
    }

    if (errorMsg != null && contacts.isEmpty()) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Error", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp)); Text(errorMsg ?: "")
                Spacer(Modifier.height(16.dp))
                Button(onClick = { showDemo = true; isLoading = false }) { Text("Show Demo Instead") }
            }
        }
        return
    }

    // Filtered contacts
    val filtered = remember(filter, contacts) {
        if (filter.isBlank()) contacts
        else contacts.filter { it.name.contains(filter, ignoreCase = true) }
    }

    // Build layout from contacts
    val layout = remember(filtered) { buildContactLayout(filtered, filter) }
    val stateData = remember { mutableStateMapOf<String, Any?>() }

    // Render
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text(layout.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        layout.elements.forEach { el ->
            RenderEl(el, stateData, onUpdate = { updated ->
                val s = updated["search"] as? String ?: ""
                if (s != filter) filter = s
            })
        }

        if (layout.actions.isNotEmpty()) {
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                layout.actions.forEach { el -> RenderEl(el, stateData) }
            }
        }
    }
}

// ── Layout builder ──

data class LayoutSpec(val title: String, val elements: List<Map<String, Any?>>, val actions: List<Map<String, Any?>>)

fun buildContactLayout(contacts: List<Contact>, filter: String): LayoutSpec {
    val elements = mutableListOf<Map<String, Any?>>()

    // Search
    elements.add(mapOf(
        "type" to "INPUT", "key" to "search", "id" to "search",
        "label" to "Search contacts", "dataType" to "STRING"
    ))

    // Stats
    elements.add(mapOf(
        "type" to "INLINE_GROUP", "key" to "stats",
        "content" to listOf(
            mapOf("type" to "BADGE", "key" to "cnt", "title" to "${contacts.size} contacts", "color" to "primary"),
            mapOf("type" to "SPACER", "key" to "sp1", "width" to 8),
            mapOf("type" to "LABEL", "key" to "hint", "label" to "Showing ${contacts.size.coerceAtMost(20)} contacts")
        )
    ))

    elements.add(mapOf("type" to "SPACER", "key" to "sp2", "width" to 8))

    if (contacts.isEmpty()) {
        elements.add(mapOf("type" to "ALERT", "key" to "empty", "message" to "No contacts found", "color" to "warning"))
    }

    contacts.take(20).forEachIndexed { i, c ->
        val fields = mutableListOf<Map<String, Any?>>()

        fields.add(mapOf("type" to "INPUT", "key" to "nm$i", "id" to "name_$i", "label" to "Name"))
        if (c.phone.isNotBlank())
            fields.add(mapOf("type" to "INPUT", "key" to "ph$i", "id" to "phone_$i", "label" to "Phone"))
        if (c.email.isNotBlank())
            fields.add(mapOf("type" to "INPUT", "key" to "em$i", "id" to "email_$i", "label" to "Email"))
        if (c.organization.isNotBlank())
            fields.add(mapOf("type" to "INPUT", "key" to "org$i", "id" to "org_$i", "label" to "Organization"))
        if (c.address.isNotBlank())
            fields.add(mapOf("type" to "INPUT", "key" to "adr$i", "id" to "addr_$i", "label" to "Address"))
        if (c.note.isNotBlank())
            fields.add(mapOf("type" to "INPUT", "key" to "note$i", "id" to "note_$i", "label" to "Note"))

        elements.add(mapOf(
            "type" to "FIELDSET", "key" to "fs_$i",
            "title" to c.name,
            "content" to fields
        ))
    }

    val actions = listOf(
        mapOf("type" to "BUTTON", "key" to "refresh", "id" to "refresh", "title" to "Refresh", "color" to "primary"),
        mapOf("type" to "BUTTON", "key" to "count", "id" to "count", "title" to "${contacts.size} total", "color" to "secondary")
    )

    return LayoutSpec("Contacts (${contacts.size})", elements, actions)
}

// ── Contact data class ──

data class Contact(
    val id: String,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val organization: String = "",
    val address: String = "",
    val note: String = ""
)

// ── Contacts loader ──

object Contacts {
    fun load(context: android.content.Context): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val uri = android.provider.ContactsContract.Data.CONTENT_URI
        val proj = arrayOf(
            android.provider.ContactsContract.Data.CONTACT_ID,
            android.provider.ContactsContract.Data.DISPLAY_NAME,
            android.provider.ContactsContract.Data.MIMETYPE,
            android.provider.ContactsContract.Data.DATA1,
            android.provider.ContactsContract.Data.DATA2,
            android.provider.ContactsContract.Data.DATA3,
            android.provider.ContactsContract.Data.DATA4
        )
        val sel = "${android.provider.ContactsContract.Data.MIMETYPE} IN (?,?,?,?)"
        val selArgs = arrayOf(
            android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
            android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
            android.provider.ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE,
            android.provider.ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE
        )
        val sort = "${android.provider.ContactsContract.Data.DISPLAY_NAME} ASC"

        val cursor = context.contentResolver.query(uri, proj, sel, selArgs, sort)
        if (cursor == null) return contacts

        cursor.use {
            val idIdx = it.getColumnIndex(proj[0])
            val nmIdx = it.getColumnIndex(proj[1])
            val mtIdx = it.getColumnIndex(proj[2])
            val d1Idx = it.getColumnIndex(proj[3])
            val d2Idx = it.getColumnIndex(proj[4])
            val d3Idx = it.getColumnIndex(proj[5])
            val d4Idx = it.getColumnIndex(proj[6])

            if (idIdx < 0 || nmIdx < 0) return contacts

            val map = linkedMapOf<String, Contact>()
            while (it.moveToNext() && map.size < 20) {
                val cid = it.getString(idIdx) ?: continue
                val name = it.getString(nmIdx) ?: "Unknown"
                val mime = it.getString(mtIdx) ?: continue
                val contact = map.getOrPut(cid) { Contact(cid, name) }

                when (mime) {
                    android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> {
                        val phone = it.getString(d1Idx) ?: ""
                        if (phone.isNotBlank() && contact.phone.isBlank())
                            map[cid] = contact.copy(phone = phone)
                    }
                    android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> {
                        val email = it.getString(d1Idx) ?: ""
                        if (email.isNotBlank() && contact.email.isBlank())
                            map[cid] = contact.copy(email = email)
                    }
                    android.provider.ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE -> {
                        val org = it.getString(d1Idx) ?: ""
                        val title = it.getString(d4Idx) ?: ""
                        val orgStr = if (title.isNotBlank()) "$title @ $org" else org
                        if (orgStr.isNotBlank() && contact.organization.isBlank())
                            map[cid] = contact.copy(organization = orgStr)
                    }
                    android.provider.ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE -> {
                        val note = it.getString(d1Idx) ?: ""
                        if (note.isNotBlank() && contact.note.isBlank())
                            map[cid] = contact.copy(note = note)
                    }
                }
            }

            // Also get addresses
            val addrContacts = loadAddresses(context, map.keys)
            addrContacts.forEach { (cid, addr) ->
                map[cid]?.let { map[cid] = it.copy(address = addr) }
            }

            contacts.addAll(map.values.sortedBy { it.name.lowercase() })
        }
        return contacts
    }

    private fun loadAddresses(context: android.content.Context, ids: Set<String>): Map<String, String> {
        val result = mutableMapOf<String, String>()
        if (ids.isEmpty()) return result
        try {
            val uri = android.provider.ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI
            val cursor = context.contentResolver.query(uri, arrayOf(
                android.provider.ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID,
                android.provider.ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS
            ), null, null, null)
            cursor?.use {
                val idIdx = it.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID)
                val addrIdx = it.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS)
                while (it.moveToNext()) {
                    val cid = it.getString(idIdx) ?: continue
                    if (cid in ids && result.size < ids.size) {
                        result[cid] = it.getString(addrIdx) ?: ""
                    }
                }
            }
        } catch (_: Exception) {}
        return result
    }
}

// ── DynamicLayout Renderer (same as before, verified working) ──

@Composable
fun RenderEl(
    el: Map<String, Any?>, data: MutableMap<String, Any?>,
    onUpdate: ((Map<String, Any?>) -> Unit)? = null,
    onAction: ((String, Map<String, Any?>?) -> Unit)? = null
) {
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
            Column(Modifier.padding(12.dp)) {
                el["title"]?.let { Text(it.toString(), fontWeight = FontWeight.SemiBold) }
                Children()
            }
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
                Text(el["title"]?.toString() ?: "", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall)
            }
        }
        "SPACER" -> Spacer(Modifier.height(((el["width"] as? Number)?.toFloat() ?: 20f).dp))
        "PROGRESS" -> Column(Modifier.padding(bottom = 8.dp)) {
            el["label"]?.let { Text(it.toString(), modifier = Modifier.padding(bottom = 4.dp)) }
            val pct = ((el["progress"] as? Number)?.toFloat() ?: 0f) / 100f
            LinearProgressIndicator(progress = pct, modifier = Modifier.fillMaxWidth())
        }
        "INPUT" -> {
            val id = el["id"]?.toString() ?: ""
            var text by remember { mutableStateOf(data[id]?.toString() ?: "") }
            OutlinedTextField(value = text, onValueChange = { text = it; data[id] = it; onUpdate?.invoke(mapOf(id to it)) },
                label = { Text(el["label"]?.toString() ?: "") }, singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
        }
        "CHECKBOX" -> {
            val id = el["id"]?.toString() ?: ""
            var ck by remember { mutableStateOf(data[id] == true) }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                Checkbox(ck, { ck = it; data[id] = it; onUpdate?.invoke(mapOf(id to it)) })
                Text(el["label"]?.toString() ?: "")
            }
        }
        "SELECT" -> {
            val id = el["id"]?.toString() ?: ""
            var expanded by remember { mutableStateOf(false) }
            val values = (el["values"] as? List<*>)?.mapNotNull {
                val m = it as? Map<*, *> ?: return@mapNotNull null
                val vId = m["id"]?.toString() ?: return@mapNotNull null
                val vName = m["displayName"]?.toString() ?: vId
                vId to vName
            } ?: emptyList()
            val selected = values.firstOrNull { it.first == data[id]?.toString() }?.second ?: ""
            Box(Modifier.padding(bottom = 8.dp)) {
                OutlinedTextField(value = selected, onValueChange = {}, readOnly = true,
                    label = { Text(el["label"]?.toString() ?: "") }, modifier = Modifier.fillMaxWidth())
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    values.forEach { (vid, name) ->
                        DropdownMenuItem(text = { Text(name) }, onClick = { data[id] = vid; expanded = false; onUpdate?.invoke(mapOf(id to vid)) })
                    }
                }
            }
        }
        "RATING" -> {
            val id = el["id"]?.toString() ?: ""
            var rating by remember { mutableStateOf((data[id] as? Number)?.toInt() ?: 0) }
            Column(Modifier.padding(bottom = 8.dp)) {
                el["label"]?.let { Text(it.toString()) }
                Row { (1..5).forEach { n ->
                    Text(if (n <= rating) "★" else "☆", color = Color(0xFFFFC107), fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                        modifier = Modifier.padding(4.dp).clickable { rating = n; data[id] = n; onUpdate?.invoke(mapOf(id to n)) })
                }}
            }
        }
        "READONLY_FIELD" -> {
            val id = el["id"]?.toString() ?: ""
            Column(Modifier.padding(bottom = 8.dp)) {
                el["label"]?.let { Text(it.toString(), fontWeight = FontWeight.Medium) }
                Surface(color = Color(0xFFF8F9FA), shape = MaterialTheme.shapes.small) {
                    Text(data[id]?.toString() ?: "—", Modifier.fillMaxWidth().padding(8.dp))
                }
            }
        }
        "BUTTON" -> {
            val c = mapOf("primary" to MaterialTheme.colorScheme.primary, "secondary" to Color.Gray, "danger" to Color.Red)
            Button(onClick = { onAction?.invoke(el["id"]?.toString() ?: "", el["responseAction"] as? Map<String, Any?>) },
                colors = ButtonDefaults.buttonColors(containerColor = c[el["color"]?.toString()] ?: MaterialTheme.colorScheme.primary)) {
                Text(el["title"]?.toString() ?: el["id"]?.toString() ?: "")
            }
        }
        else -> Text("Unknown: $t", color = Color.Red, modifier = Modifier.padding(bottom = 4.dp))
    }
}
