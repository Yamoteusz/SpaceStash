package com.spacestash.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import android.content.Context
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

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
        // Przycisk Pobierz/Odśwież wymagany przez specyfikację
        Button(onClick = { viewModel.fetchApod(apiKey) }) {
            Text("Pobierz / Odśwież Kosmiczne Zdjęcie")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Reagowanie na stan z ViewModelu
        when (val state = uiState) {
            is HomeUiState.Initial -> Text("Kliknij przycisk, aby nawiązać łączność z NASA!")
            is HomeUiState.Loading -> CircularProgressIndicator() // Kręcące się kółko
            is HomeUiState.Error -> Text(text = state.message, color = MaterialTheme.colorScheme.error)
            is HomeUiState.Success -> {
                Text(
                    text = state.data.title,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Wyświetlanie zdjęcia prosto z internetu dzięki bibliotece Coil
                AsyncImage(
                    model = state.data.url,
                    contentDescription = state.data.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    // Intencja otwierająca przeglądarkę z oficjalną stroną APOD
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
                // Stany do kontrolowania okienka i tekstu wpisywanego przez użytkownika
                var showDialog by remember { mutableStateOf(false) }
                var noteText by remember { mutableStateOf("") }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { showDialog = true }) {
                    Text("Dodaj do Schowka")
                }

                // Logika wyskakującego okienka
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
                                // Zapisujemy z notatką!
                                stashViewModel.addItem(
                                    title = state.data.title,
                                    url = state.data.url,
                                    date = state.data.date,
                                    note = noteText
                                )
                                showDialog = false // Zamykamy okienko
                                noteText = "" // Czyścimy pole na przyszłość
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

    // Stany do okienka
    var showCustomDialog by remember { mutableStateOf(false) }
    var customImageUri by remember { mutableStateOf("") }
    var customTitle by remember { mutableStateOf("Moje odkrycie") }
    var customNote by remember { mutableStateOf("") }

    // Stan do trzymania tymczasowego linku aparatu
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Wyrzutnia Galerii
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            customImageUri = uri.toString()
            showCustomDialog = true
        }
    }

    // Wyrzutnia Aparatu
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

        // Dwa przyciski obok siebie
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                val uri = context.createImageFileUri()
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            }) {
                Text("📷 Aparat")
            }

            Button(onClick = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }) {
                Text("🖼️ Galeria")
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

    // Okienko zapisu
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

        // Przycisk uruchamiający intencję telefonu
        Button(onClick = {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:+48123456789") // Przykładowy numer
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // Obsługa błędu, gdy system nie znajdzie aplikacji telefonu
                Toast.makeText(context, "Brak aplikacji do dzwonienia!", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Zadzwoń do nas")
        }
    }
}

fun Context.createImageFileUri(): Uri {
    // Tworzy unikalny plik w folderze cache z aktualną datą
    val file = File(cacheDir, "camera_image_${System.currentTimeMillis()}.jpg")
    // Zwraca bezpieczny Uri przez nasz FileProvider
    return FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
}