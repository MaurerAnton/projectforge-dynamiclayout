package dynamiclayout.playground

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable fun DemoPage(onBack: () -> Unit) {
    val el = mapOf("type" to "ALERT", "key" to "a", "message" to "Renderer works!", "color" to "success")
    val layout = listOf(el,
        mapOf("type" to "INPUT", "key" to "i1", "id" to "name", "label" to "Name", "required" to true),
        mapOf("type" to "INPUT", "key" to "i2", "id" to "email", "label" to "Email"),
        mapOf("type" to "CHECKBOX", "key" to "cb", "id" to "agree", "label" to "I agree"),
        mapOf("type" to "SELECT", "key" to "s", "id" to "country", "label" to "Country", "values" to listOf(mapOf("id" to "us", "displayName" to "USA"), mapOf("id" to "de", "displayName" to "Germany"))),
        mapOf("type" to "RATING", "key" to "rt", "id" to "stars", "label" to "Rate"),
        mapOf("type" to "BADGE", "key" to "bd", "title" to "Badge", "color" to "primary"),
        mapOf("type" to "PROGRESS", "key" to "pr", "progress" to 65))
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
