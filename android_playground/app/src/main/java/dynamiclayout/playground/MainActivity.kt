package dynamiclayout.playground

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ── MainActivity ──

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                ContactsApp()
            }
        }
    }
}

// ── App composable ──

@Composable
fun ContactsApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
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
                Text("DynamicLayout Contacts Demo", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                Text("Renders your phone contacts using the DynamicLayout engine.",
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) }) {
                    Text("Grant Contacts Permission")
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { hasPermission = true; reloadKey++ }) {
                    Text("Skip — Show Demo Layout")
                }
            }
        }
    } else {
        ContactsView(context, loadKey = reloadKey, onReload = { reloadKey++ })
    }
}

// ── Contacts loader + renderer ──

@Composable
fun ContactsView(context: android.content.Context, loadKey: Int = 0, onReload: () -> Unit = {}) {
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var filter by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var useDemo by remember { mutableStateOf(false) }

    LaunchedEffect(loadKey) {
        isLoading = true; errorMsg = null
        try {
            val result = withContext(Dispatchers.IO) { ContactsLoader.load(context, "") }
            contacts = result
            if (result.isEmpty()) useDemo = true
        } catch (t: Throwable) {
            errorMsg = "${t.javaClass.simpleName}: ${t.message}"
            useDemo = true
        }
        isLoading = false
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(8.dp))
                Text("Loading contacts...")
            }
        }
        return
    }

    if (errorMsg != null) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Error", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text(errorMsg ?: "")
                Spacer(Modifier.height(16.dp))
                Button(onClick = { isLoading = true; errorMsg = null; onReload() }) { Text("Retry") }
            }
        }
        return
    }

    // Generate JSON from contacts (or fallback to demo)
    val filtered = remember(filter, contacts) {
        if (filter.isBlank()) contacts
        else contacts.filter { it.name.contains(filter, ignoreCase = true) }
    }

    val spec = remember(filtered, useDemo) {
        if (useDemo) DEMO_JSON else ContactToJson.generate(filtered, filter)
    }
    val uiSpec = spec["ui"] as? Map<String, Any?> ?: emptyMap()
    val uiData = spec["data"] as? Map<String, Any?> ?: emptyMap()

    val stateData = remember { mutableStateMapOf<String, Any?>() }
    LaunchedEffect(uiData) { stateData.clear(); uiData.forEach { (k, v) -> stateData[k] = v } }

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        uiSpec["title"]?.let {
            Text(it.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
        }

        (uiSpec["layout"] as? List<*>)?.forEach { el ->
            RenderEl(
                (el as? Map<String, Any?>) ?: emptyMap(),
                stateData,
                onUpdate = { updated ->
                    val s = updated["search"] as? String ?: ""
                    if (s != filter) filter = s
                }
            )
        }

        val actions = uiSpec["actions"] as? List<*>
        if (!actions.isNullOrEmpty()) {
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                actions.forEach { el ->
                    RenderEl((el as? Map<String, Any?>) ?: emptyMap(), stateData, onAction = { id, _ ->
                        // Handle action
                    })
                }
            }
        }
    }
}

@Composable
fun RenderEl(
    el: Map<String, Any?>,
    data: MutableMap<String, Any?>,
    onUpdate: ((Map<String, Any?>) -> Unit)? = null,
    onAction: ((String, Map<String, Any?>?) -> Unit)? = null
) {
    val t = el["type"]?.toString() ?: ""

    @Composable
    fun Children() {
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
        "LABEL" -> Text(el["label"]?.toString() ?: "", modifier = Modifier.padding(bottom = 4.dp), fontWeight = FontWeight.Medium)
        "ALERT" -> Text(el["message"]?.toString() ?: "", Modifier.fillMaxWidth().padding(12.dp), color = Color(0xFF333333))
        "BADGE" -> {
            val c = mapOf("primary" to Color(0xFF0D6EFD), "secondary" to Color(0xFF6C757D), "success" to Color(0xFF198754))
            Surface(color = c[el["color"]?.toString()] ?: Color.Gray, shape = MaterialTheme.shapes.small) {
                Text(el["title"]?.toString() ?: "", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall)
            }
        }
        "SPACER" -> Spacer(Modifier.height(((el["width"] as? Number)?.toFloat() ?: 20f).dp))
        "INPUT" -> {
            val id = el["id"]?.toString() ?: ""
            var text by remember { mutableStateOf(data[id]?.toString() ?: "") }
            OutlinedTextField(
                value = text, onValueChange = { text = it; data[id] = it; onUpdate?.invoke(mapOf(id to it)) },
                label = { Text(el["label"]?.toString() ?: "") },
                singleLine = true, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
        }
        "CHECKBOX" -> {
            val id = el["id"]?.toString() ?: ""
            var ck by remember { mutableStateOf(data[id] == true) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(ck, { ck = it; data[id] = it; onUpdate?.invoke(mapOf(id to it)) })
                Text(el["label"]?.toString() ?: "")
            }
        }
        "READONLY_FIELD" -> {
            val id = el["id"]?.toString() ?: ""
            Column {
                el["label"]?.let { Text(it.toString(), fontWeight = FontWeight.Medium) }
                Surface(color = Color(0xFFF8F9FA), shape = MaterialTheme.shapes.small) {
                    Text(data[id]?.toString() ?: "—", Modifier.fillMaxWidth().padding(8.dp))
                }
            }
        }
        "BUTTON" -> {
            val c = mapOf("primary" to MaterialTheme.colorScheme.primary, "secondary" to Color.Gray)
            Button(
                onClick = { onAction?.invoke(el["id"]?.toString() ?: "", el["responseAction"] as? Map<String, Any?>) },
                colors = ButtonDefaults.buttonColors(containerColor = c[el["color"]?.toString()] ?: MaterialTheme.colorScheme.primary)
            ) { Text(el["title"]?.toString() ?: el["id"]?.toString() ?: "") }
        }
        else -> Text("Unknown: $t", color = Color.Red)
    }
}

// ── Demo layout (used when contacts unavailable or loading fails) ──

val DEMO_JSON = mapOf(
    "ui" to mapOf(
        "title" to "DynamicLayout Demo",
        "uid" to "demo",
        "layout" to listOf(
            mapOf("type" to "ALERT", "key" to "a1", "message" to "This is a demo layout. Click 'Grant Permission' to see your real contacts.", "color" to "info"),
            mapOf("type" to "FIELDSET", "key" to "fs1", "title" to "Inputs", "content" to listOf(
                mapOf("type" to "INPUT", "key" to "i1", "id" to "name", "label" to "Your Name", "required" to true),
                mapOf("type" to "INPUT", "key" to "i2", "id" to "email", "label" to "Email", "dataType" to "STRING"),
                mapOf("type" to "INPUT", "key" to "i3", "id" to "password", "label" to "Password", "dataType" to "PASSWORD"),
                mapOf("type" to "TEXTAREA", "key" to "t1", "id" to "notes", "label" to "Notes", "rows" to 3)
            )),
            mapOf("type" to "FIELDSET", "key" to "fs2", "title" to "Selection", "content" to listOf(
                mapOf("type" to "CHECKBOX", "key" to "cb1", "id" to "agree", "label" to "I agree to terms"),
                mapOf("type" to "SELECT", "key" to "s1", "id" to "country", "label" to "Country", "values" to listOf(
                    mapOf("id" to "us", "displayName" to "USA"),
                    mapOf("id" to "de", "displayName" to "Germany"),
                    mapOf("id" to "jp", "displayName" to "Japan")
                )),
                mapOf("type" to "RATING", "key" to "rt1", "id" to "stars", "label" to "Rate this app")
            )),
            mapOf("type" to "FIELDSET", "key" to "fs3", "title" to "Display", "content" to listOf(
                mapOf("type" to "LABEL", "key" to "l1", "label" to "This is a label"),
                mapOf("type" to "BADGE", "key" to "bd1", "title" to "Primary Badge", "color" to "primary"),
                mapOf("type" to "BADGE", "key" to "bd2", "title" to "Success", "color" to "success"),
                mapOf("type" to "ALERT", "key" to "a2", "message" to "Everything is working", "color" to "success"),
                mapOf("type" to "PROGRESS", "key" to "pr1", "progress" to 65, "color" to "primary")
            ))
        ),
        "actions" to listOf(
            mapOf("type" to "BUTTON", "key" to "b1", "id" to "save", "title" to "Save Demo", "color" to "primary"),
            mapOf("type" to "BUTTON", "key" to "b2", "id" to "cancel", "title" to "Cancel", "color" to "secondary")
        ),
        "translations" to emptyMap<String, String>(),
        "userAccess" to mapOf("cancel" to true)
    ),
    "data" to emptyMap<String, Any>()
)
