package dynamiclayout.playground

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ── Main Activity ──

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> /* ViewModel handles result */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                PlaygroundScreen()
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }
}

// ── ViewModel ──

class PlaygroundViewModel(app: android.app.Application) : AndroidViewModel(app) {
    var contacts by mutableStateOf<List<Contact>>(emptyList())
        private set
    var filter by mutableStateOf("")
        private set
    var spec by mutableStateOf<Map<String, Any>>(emptyMap())
        private set
    var data by mutableStateOf<Map<String, Any>>(emptyMap())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    init {
        // Don't auto-load — wait for activity to trigger
    }

    fun loadContacts() {
        isLoading = true
        error = null
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    ContactsLoader.load(getApplication<android.app.Application>(), filter)
                }
                contacts = result
                regenerate()
                isLoading = false
            } catch (e: Exception) {
                error = e.message ?: "Failed to load contacts"
                isLoading = false
            }
        }
    }

    fun onFilterChange(newFilter: String) {
        filter = newFilter
        val filtered = if (newFilter.isBlank()) contacts
        else contacts.filter { it.name.contains(newFilter, ignoreCase = true) }
        val generated = ContactToJson.generate(filtered, newFilter)
        spec = generated["ui"] as Map<String, Any>
        data = generated["data"] as Map<String, Any>
    }

    private fun regenerate() {
        val generated = ContactToJson.generate(contacts, filter)
        spec = generated["ui"] as Map<String, Any>
        data = generated["data"] as Map<String, Any>
    }
}

// ── Playground Screen ──

@Composable
fun PlaygroundScreen(vm: PlaygroundViewModel = viewModel()) {
    LaunchedEffect(Unit) {
        if (vm.spec.isEmpty()) vm.loadContacts()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        when {
            vm.isLoading -> Box(Modifier.fillMaxSize()) {
                Column(Modifier.align(androidx.compose.ui.Alignment.Center),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text("Loading contacts...")
                }
            }
            vm.error != null -> Box(Modifier.fillMaxSize()) {
                Column(Modifier.align(androidx.compose.ui.Alignment.Center).padding(32.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text("Error loading contacts", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Text(vm.error ?: "", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { vm.loadContacts() }) { Text("Retry") }
                }
            }
            vm.spec.isEmpty() -> Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(androidx.compose.ui.Alignment.Center))
            }
            else -> DynamicLayout(
                spec = vm.spec,
                data = vm.data,
                onUpdate = { updated ->
                    val searchVal = updated["search"] as? String ?: ""
                    if (searchVal != vm.filter) {
                        vm.onFilterChange(searchVal)
                    }
                },
                onAction = { id, _ ->
                    when (id) {
                        "refresh" -> vm.loadContacts()
                    }
                }
            )
        }
    }
}

// ── DynamicLayout Compose Renderer ──

@Composable
fun DynamicLayout(
    spec: Map<String, Any?>,
    data: Map<String, Any?>,
    modifier: Modifier = Modifier,
    onUpdate: ((Map<String, Any?>) -> Unit)? = null,
    onAction: ((String, Map<String, Any?>?) -> Unit)? = null
) {
    val state = remember { mutableStateMapOf<String, Any?>() }
    LaunchedEffect(data) { state.clear(); data.forEach { (k, v) -> state[k] = v } }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        spec["title"]?.let {
            Text(it.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
        }

        (spec["layout"] as? List<*>)?.forEach { el ->
            RenderEl((el as? Map<String, Any?>) ?: emptyMap(), state, onUpdate, onAction)
        }

        val actions = spec["actions"] as? List<*>
        if (!actions.isNullOrEmpty()) {
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                actions.forEach { el ->
                    RenderEl((el as? Map<String, Any?>) ?: emptyMap(), state, onUpdate, onAction)
                }
            }
        }
    }
}

@Composable
fun RenderEl(
    el: Map<String, Any?>,
    data: MutableMap<String, Any?>,
    onUpdate: ((Map<String, Any?>) -> Unit)?,
    onAction: ((String, Map<String, Any?>?) -> Unit)?
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
        "ALERT" -> {
            val bg = mapOf("info" to Color(0xFFE0F0FF), "warning" to Color(0xFFFFF3CD), "danger" to Color(0xFFFFE0E0))
            Text(el["message"]?.toString() ?: "", Modifier.fillMaxWidth().padding(12.dp), color = Color(0xFF333333))
        }
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
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
        }
        "CHECKBOX" -> {
            val id = el["id"]?.toString() ?: ""
            var ck by remember { mutableStateOf(data[id] == true) }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
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
            ) {
                Text(el["title"]?.toString() ?: el["id"]?.toString() ?: "")
            }
        }
        else -> Text("Unknown: $t", color = Color.Red)
    }
}
