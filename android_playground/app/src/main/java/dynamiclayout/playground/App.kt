package dynamiclayout.playground

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

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
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("DynamicLayout Demo", style = MaterialTheme.typography.headlineSmall); Spacer(Modifier.height(16.dp))
                Text("Renderer is working. Choose:", style = MaterialTheme.typography.bodyMedium); Spacer(Modifier.height(24.dp))
                Button(onClick = { page = "demo" }) { Text("Show Demo Render") }; Spacer(Modifier.height(12.dp))
                Button(onClick = { page = "contacts" }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF198754))) { Text("Render My Contacts (debug)") }
            }
            Text("build 37", style = MaterialTheme.typography.labelSmall, color = Color.Gray,
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))
        }
        return
    }
    if (page == "demo") { DemoPage { page = "menu" }; return }
    if (page == "contacts") { ContactsPage(ctx, { page = "menu" }, reload); return }
}
