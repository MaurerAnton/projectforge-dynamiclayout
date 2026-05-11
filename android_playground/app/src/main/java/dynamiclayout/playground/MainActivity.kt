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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ── Main Activity ──

class MainActivity : ComponentActivity() {
    private val viewModel = PlaygroundViewModel()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.loadContacts(this)
        else Toast.makeText(this, "Contacts permission required", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                PlaygroundScreen(viewModel)
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.loadContacts(this)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }
}

// ── ViewModel (state holder) ──

class PlaygroundViewModel {
    var contacts by mutableStateOf<List<Contact>>(emptyList())
    var filter by mutableStateOf("")
    var spec by mutableStateOf<Map<String, Any>>(emptyMap())
    var data by mutableStateOf<Map<String, Any>>(emptyMap())
    private var hasPermission = false

    fun loadContacts(context: android.content.Context) {
        hasPermission = true
        lifecycleScope(context).launch {
            val result = withContext(Dispatchers.IO) {
                ContactsLoader.load(context, filter)
            }
            contacts = result
            regenerate()
        }
    }

    fun onFilterChange(newFilter: String) {
        filter = newFilter
        if (hasPermission) {
            // Reload with filter from the already-loaded list
            val filtered = if (newFilter.isBlank()) contacts
            else contacts.filter { it.name.contains(newFilter, ignoreCase = true) }
            val generated = ContactToJson.generate(filtered, newFilter)
            spec = generated["ui"] as Map<String, Any>
            data = generated["data"] as Map<String, Any>
        }
    }

    private fun regenerate() {
        val generated = ContactToJson.generate(contacts, filter)
        spec = generated["ui"] as Map<String, Any>
        data = generated["data"] as Map<String, Any>
    }

    private fun lifecycleScope(context: android.content.Context) =
        (context as ComponentActivity).lifecycleScope
}

// ── Playground Screen ──

@Composable
fun PlaygroundScreen(viewModel: PlaygroundViewModel) {
    Surface(modifier = Modifier.fillMaxSize()) {
        if (viewModel.spec.isEmpty()) {
            Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(androidx.compose.ui.Alignment.Center))
            }
        } else {
            DynamicLayout(
                spec = viewModel.spec,
                data = viewModel.data,
                onUpdate = { updated ->
                    val searchVal = updated["search"] as? String ?: ""
                    if (searchVal != viewModel.filter) {
                        viewModel.onFilterChange(searchVal)
                    }
                },
                onAction = { id, _ ->
                    when (id) {
                        "refresh" -> viewModel.onFilterChange(viewModel.filter)
                    }
                }
            )
        }
    }
}

// ── DynamicLayout Compose Renderer (inlined for playground) ──

@Composable
fun DynamicLayout(
    spec: Map<String, Any?>,
    data: Map<String, Any?>,
    modifier: Modifier = Modifier,
    onUpdate: ((Map<String, Any?>) -> Unit)? = null,
    onAction: ((String, Map<String, Any?>?) -> Unit)? = null
) {
    val state = remember { mutableStateMapOf<String, Any?>() }
    LaunchedEffect(data) { state.clear(); state.putAll(data) }

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
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
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

    fun children() {
        (el["content"] as? List<*>)?.forEach {
            RenderEl((it as? Map<String, Any?>) ?: emptyMap(), data, onUpdate, onAction)
        }
    }

    when (t) {
        "ROW" -> Row(Modifier.fillMaxWidth()) { children() }
        "COL" -> Column { children() }
        "GROUP" -> Column { children() }
        "INLINE_GROUP" -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { children() }
        "FIELDSET" -> Card(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            Column(Modifier.padding(12.dp)) {
                el["title"]?.let { Text(it.toString(), fontWeight = FontWeight.SemiBold) }
                children()
            }
        }
        "LABEL" -> Text(el["label"]?.toString() ?: "", modifier = Modifier.padding(bottom = 4.dp), fontWeight = FontWeight.Medium)
        "ALERT" -> {
            val bg = mapOf("info" to Color(0xFFE0F0FF), "warning" to Color(0xFFFFF3CD), "danger" to Color(0xFFFFE0E0))
            Text(el["message"]?.toString() ?: "", Modifier.fillMaxWidth().padding(12.dp), color = Color(0xFF333333),
                style = MaterialTheme.typography.bodyMedium)
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
                colors = ButtonDefaults.buttonColors(containerColor = c[el["color"]?.toString()] ?: MaterialTheme.colorScheme.primary),
                modifier = Modifier
            ) {
                Text(el["title"]?.toString() ?: el["id"]?.toString() ?: "")
            }
        }
        else -> Text("Unknown: $t", color = Color.Red)
    }
}
