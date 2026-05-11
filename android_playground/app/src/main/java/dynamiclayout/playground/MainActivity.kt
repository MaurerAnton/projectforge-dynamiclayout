package dynamiclayout.playground

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
    // STEP 1: minimal — just text to isolate the crash
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text("App is working. Contacts rendering is disabled for debugging.\n\nTap Reload to retry.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp))
    }
}

