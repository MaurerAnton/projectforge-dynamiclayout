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

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) loadKey++  // trigger reload
    }

    if (!hasPermission) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.align(Alignment.Center).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text("DynamicLayout Contacts Demo", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                Text("This app renders your phone contacts using the DynamicLayout engine.\n\nGrant permission to continue.",
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) }) {
                    Text("Grant Contacts Permission")
                }
            }
        }
    } else {
        var loadKey by remember { mutableIntStateOf(0) }
        ContactsView(context, loadKey, onReload = { loadKey++ })
    }
}

// ── Contacts loader + renderer ──

@Composable
fun ContactsView(context: android.content.Context, loadKey: Int = 0, onReload: () -> Unit = {}) {
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var filter by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(loadKey) {
        try {
            val result = withContext(Dispatchers.IO) {
                ContactsLoader.load(context, "")
            }
            contacts = result
            isLoading = false
        } catch (e: Exception) {
            errorMsg = e.message ?: "Failed"
            isLoading = false
        }
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

    // Generate JSON from contacts
    val filtered = remember(filter, contacts) {
        if (filter.isBlank()) contacts
        else contacts.filter { it.name.contains(filter, ignoreCase = true) }
    }

    val spec = remember(filtered) { ContactToJson.generate(filtered, filter) }
    val uiSpec = spec["ui"] as? Map<String, Any?> ?: error("Bad spec")
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
            RenderEl((el as? Map<String, Any?>) ?: emptyMap(), stateData) { updated ->
                val s = updated["search"] as? String ?: ""
                if (s != filter) filter = s
            }
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
