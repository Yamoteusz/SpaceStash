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

                val stashViewModel: StashViewModel = viewModel() // Dodaj ten import i inicjalizację

                Button(onClick = {
                    stashViewModel.addItem(state.data.title, state.data.url, state.data.date)
                }) {
                    Text("Dodaj do Stasha")
                }
            }
        }
    }
}
@Composable
fun StashScreen(viewModel: StashViewModel = viewModel()) {
    // Odczyt danych z bazy Room
    val items by viewModel.allItems.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mój Kosmiczny Schowek", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        // Obsługa pustej listy zgodnie ze specyfikacją
        if (items.isEmpty()) {
            Text("Twój schowek jest pusty. Dodaj zdjęcia z ekranu Home!")
        }

        LazyColumn {
            // Funkcjonalność przeglądania zapisanych elementów
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

                        // Wyświetlanie notatek użytkownika [cite: 5, 26]
                        Text("Notatka: ${item.note}")

                        // Przycisk usuwania z bazy (CRUD)
                        Button(onClick = { viewModel.deleteItem(item) }) {
                            Text("Usuń")
                        }
                    }
                }
            }
        }
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