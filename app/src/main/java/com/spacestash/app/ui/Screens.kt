package com.spacestash.app.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // --- NOWY KOD DLA DATASTORE ---
    val userStore = remember { UserStore(context) }
    val scope = rememberCoroutineScope()

    // initial = null oznacza, że aplikacja "jeszcze ładuje" plik
    val savedName by userStore.userNameFlow.collectAsState(initial = null)

    var showNameDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }

    LaunchedEffect(savedName) {
        // Jeśli plik się załadował i faktycznie jest puste ("", a nie null)
        if (savedName == "") {
            showNameDialog = true
        }
    }

    // Okienko do podania imienia
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { /* Nie pozwalamy zamknąć bez podania */ },
            title = { Text("Witaj w SpaceStash!") },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Jak masz na imię, Dowódco?") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (tempName.isNotBlank()) {
                        scope.launch {
                            userStore.saveName(tempName) // Zapisujemy na stałe!
                        }
                        showNameDialog = false
                    }
                }) {
                    Text("Zapisz")
                }
            }
        )
    }

    // Pobieranie naszego ukrytego klucza NASA z AndroidManifest.xml
    val apiKey = remember {
        val appInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        appInfo.metaData.getString("NASA_API_KEY") ?: "DEMO_KEY"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- OSOBISTE POWITANIE ---
        if (!savedName.isNullOrEmpty()) { // Zmieniony warunek!
            Text(
                text = "Witaj, $savedName! \uD83D\uDE80",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Przycisk Pobierz/Odśwież
        Button(onClick = { viewModel.fetchApod(apiKey) }) {
            Text("Pobierz / Odśwież Kosmiczne Zdjęcie")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Reagowanie na stan z ViewModelu
        when (val state = uiState) {
            is HomeUiState.Initial -> Text("Kliknij przycisk, aby nawiązać łączność z NASA!")
            is HomeUiState.Loading -> CircularProgressIndicator()
            is HomeUiState.Error -> Text(text = state.message, color = MaterialTheme.colorScheme.error)
            is HomeUiState.Success -> {
                Text(
                    text = state.data.title,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                AsyncImage(
                    model = state.data.url,
                    contentDescription = state.data.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://apod.nasa.gov/apod/"))
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Brak przeglądarki na urządzeniu!", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Czytaj więcej na stronie NASA")
                }

                val stashViewModel: StashViewModel = viewModel()
                var showDialog by remember { mutableStateOf(false) }
                var noteText by remember { mutableStateOf("") }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { showDialog = true }) {
                    Text("Dodaj do Schowka")
                }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Zapisz w Schowku") },
                        text = {
                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                label = { Text("Dodaj własną notatkę (opcjonalnie)") }
                            )
                        },
                        confirmButton = {
                            Button(onClick = {
                                stashViewModel.addItem(
                                    title = state.data.title,
                                    url = state.data.url,
                                    date = state.data.date,
                                    note = noteText
                                )
                                showDialog = false
                                noteText = ""
                            }) {
                                Text("Zapisz")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Anuluj")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StashScreen(viewModel: StashViewModel = viewModel()) {
    val items by viewModel.allItems.collectAsState(initial = emptyList())
    val context = LocalContext.current

    var showCustomDialog by remember { mutableStateOf(false) }
    var customImageUri by remember { mutableStateOf("") }
    var customTitle by remember { mutableStateOf("Moje odkrycie") }
    var customNote by remember { mutableStateOf("") }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            customImageUri = uri.toString()
            showCustomDialog = true
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            customImageUri = tempCameraUri.toString()
            showCustomDialog = true
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mój Kosmiczny Schowek", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                val uri = context.createImageFileUri()
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            }) {
                Text("\uD83D\uDCF7 Aparat")
            }

            Button(onClick = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }) {
                Text("\uD83D\uDDBC\uFE0F Galeria")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (items.isEmpty()) {
            Text("Twój schowek jest pusty. Dodaj zdjęcia z ekranu Home, Galerii lub zrób zdjęcie Aparatem!")
        }

        LazyColumn {
            items(items) { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(item.title, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(8.dp))

                        AsyncImage(
                            model = item.url,
                            contentDescription = null,
                            modifier = Modifier.height(150.dp).fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Data: ${item.date}")
                        Text("Notatka: ${item.note}")

                        Button(onClick = { viewModel.deleteItem(item) }) {
                            Text("Usuń")
                        }
                    }
                }
            }
        }
    }

    if (showCustomDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("Zapisz własne zdjęcie") },
            text = {
                Column {
                    OutlinedTextField(
                        value = customTitle,
                        onValueChange = { customTitle = it },
                        label = { Text("Tytuł zdjęcia") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customNote,
                        onValueChange = { customNote = it },
                        label = { Text("Twoja notatka") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addItem(
                        title = customTitle,
                        url = customImageUri,
                        date = "Własne",
                        note = customNote
                    )
                    showCustomDialog = false
                    customTitle = "Moje odkrycie"
                    customNote = ""
                }) {
                    Text("Zapisz")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

@Composable
fun ContactScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Kontakt z Bazą", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Masz pytania dotyczące naszej misji?")
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:+48123456789")
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Brak aplikacji do dzwonienia!", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Zadzwoń do nas")
        }
    }
}

fun Context.createImageFileUri(): Uri {
    val file = File(cacheDir, "camera_image_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
}