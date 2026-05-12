# Android Contacts Playground

DynamicLayout demo app for Android — renders phone contacts using the DynamicLayout engine. Built with Jetpack Compose.

## Architecture

```
App()                        Entry point, permission gate
├── MainScreen()             Menu: Demo | Contacts
│   ├── DemoPage()            Verified-working DynamicLayout renderer
│   └── ContactsPage()        Debug contacts loader
│       ├── Step 1             Hardcoded test + debug buttons
│       ├── Step 2             Loading (unused, kept for future)
│       └── Step 3             Contact list with all data
├── Contacts                  Contact data loader
│   ├── loadContactsBasic()   Names only (Contacts.CONTENT_URI)
│   ├── loadContactsWithPhones()  + phone numbers with type icons
│   ├── loadContactsWithEmails()  + emails
│   └── loadContactsFull()    + org, address, notes, photos
└── DynamicLayout renderer    Full component renderer (inlined)
```

## Current features

### Menu screen
- "Show Demo Render" — renders a hardcoded form (INPUT, CHECKBOX, SELECT, RATING, BADGE, PROGRESS, BUTTON)
- "Render My Contacts" — loads real phone contacts

### Contacts (build 36)
- **100 contacts** loaded from device
- **All phone numbers** per contact with type icons:
  - 📱 Mobile, 🏠 Home, 💼 Work, 📠 Fax, 📞 Other
  - 💬 WhatsApp, ✈ Telegram, 🔒 Signal, 📲 Viber (by label match)
- **Emails** — all addresses per contact
- **Organization** — company name
- **Address** — formatted postal address
- **Notes** — full text notes
- **Photos** — contact avatar (40dp circle)
- **Sorting** — A-Z / Z-A toggle
- **Build number** displayed at bottom of menu

### Debug buttons (Step 1)
- Load names only — basic test
- Load all fields — full data test
- Each button shows OK count or error message
- test emptyList, test query()+close(), test read 1st

## Data model

```kotlin
data class Contact(
    val id: String,
    val name: String,
    val phones: List<String> = emptyList(),       // all phone numbers
    val emails: List<String> = emptyList(),       // all email addresses
    val org: String = "",                          // organization name
    val addr: String = "",                         // formatted address
    val note: String = "",                         // contact note
    val photo: ByteArray? = null,                  // avatar thumbnail
    val phoneTypes: Map<String, String> = emptyMap() // phone → icon
)
```

## Files

```
android_playground/
├── build.gradle.kts                Project build config
├── settings.gradle.kts             Gradle settings
├── gradle.properties               Gradle properties
├── gradle/wrapper/                  Gradle wrapper config
├── app/
│   ├── build.gradle.kts             App build config (Compose BOM 2024.01)
│   └── src/main/
│       ├── AndroidManifest.xml      READ_CONTACTS permission
│       ├── res/values/themes.xml    Theme placeholder
│       └── java/dynamiclayout/playground/
│           └── MainActivity.kt      All source code (single file)
└── app-debug.apk                   Pre-built APK
```

## Planned features (roadmap)

### Next activities — user-facing screens
- [ ] **Contact detail** — tap contact → full page with all fields, edit capability
- [ ] **Favorites** — star/favorite contacts, separate tab
- [ ] **Groups** — show contact groups/labels
- [ ] **Call log** — recent calls (needs READ_CALL_LOG permission)
- [ ] **SMS/MMS preview** — last message per contact (needs READ_SMS)
- [ ] **Export to PFDL JSON** — generate DynamicLayout JSON from contacts
- [ ] **Search/filter** — text filter by name, phone, email
- [ ] **Events** — birthdays, anniversaries from contact events
- [ ] **Share contact** — share as vCard or text
- [ ] **Dark theme** toggle

### Technical improvements
- [ ] DynamicLayout-based rendering for contacts (currently plain Compose)
- [ ] Photo loading with caching (memory + disk)
- [ ] Pull-to-refresh contacts list
- [ ] Infinite scroll (load 100, then 100 more)
- [ ] Background sync (WorkManager for periodic reload)
- [ ] Multiple Activities (not just single-activity Compose)
- [ ] Proper package structure (not single-file)

## How to add a new activity

### Option 1: New page in MainScreen (simplest)

```kotlin
// In MainScreen, add:
else if (page == "newpage") { NewPage { page = "menu" } }

// And a button:
Button(onClick = { page = "newpage" }) { Text("Open New Page") }
```

### Option 2: New Activity (traditional Android)

```kotlin
// Register in AndroidManifest.xml
<activity android:name=".NewActivity" />

// Create NewActivity.kt
class NewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { NewScreen() } }
    }
}

// Launch from MainScreen
val ctx = LocalContext.current
Button(onClick = { ctx.startActivity(Intent(ctx, NewActivity::class.java)) }) { Text("Open") }
```

### Option 3: Bottom navigation

```kotlin
// Add NavigationBar with NavHost in App()
var tab by remember { mutableStateOf(0) }
Scaffold(bottomBar = {
    NavigationBar {
        NavigationBarItem(...); NavigationBarItem(...)
    }
}) { innerPadding ->
    when (tab) { 0 -> MainScreen(...); 1 -> FavoritesScreen(...) }
}
```

## Build instructions

### Android Studio
1. Open `android_playground/` in Android Studio
2. Wait for Gradle sync
3. Run on device or emulator (API 26+)

### Command line
```bash
cd android_playground
./gradlew assembleDebug
# APK at app/build/outputs/apk/debug/app-debug.apk
```

### GitHub Actions
APK is built automatically on every push to master.
Download from: https://github.com/MaurerAnton/projectforge-dynamiclayout-xx/actions

### Pre-built APK
Direct download: `https://raw.githubusercontent.com/MaurerAnton/projectforge-dynamiclayout-xx/master/android_playground/app-debug.apk`
