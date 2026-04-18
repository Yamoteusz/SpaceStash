package com.spacestash.app.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.spacestash.app.domain.Post
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val userStore = remember { UserStore(context) }
    val scope = rememberCoroutineScope()

    val savedName by userStore.userNameFlow.collectAsState(initial = null)

    var showNameDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }

    LaunchedEffect(savedName) {
        if (savedName == "") {
            showNameDialog = true
        }
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { },
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
                            userStore.saveName(tempName)
                        }
                        showNameDialog = false
                    }
                }) {
                    Text("Zapisz")
                }
            }
        )
    }

    val apiKey = remember {
        val appInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        appInfo.metaData.getString("NASA_API_KEY") ?: "DEMO_KEY"
    }

    val notificationHelper = remember { NotificationHelper(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            notificationHelper.showNotification("SpaceStash", "Łączność nawiązana! Powiadomienia działają.")
        } else {
            Toast.makeText(context, "Brak zgody na powiadomienia", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!savedName.isNullOrEmpty()) {
            Text(
                text = "Witaj, $savedName! \uD83D\uDE80",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(onClick = { viewModel.fetchApod(apiKey) }) {
            Text("Pobierz / Odśwież Kosmiczne Zdjęcie")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                if (isGranted) {
                    notificationHelper.showNotification("SpaceStash", "Misja gotowa do startu! 🚀")
                } else {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                notificationHelper.showNotification("SpaceStash", "Misja gotowa do startu! 🚀")
            }
        }) {
            Text("Wyślij sygnał testowy \uD83D\uDD14")
        }
        Spacer(modifier = Modifier.height(16.dp))

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

@Composable
fun CommunityScreen(viewModel: CommunityViewModel = viewModel()) {
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    val userStore = remember { UserStore(context) }
    val authorName by userStore.userNameFlow.collectAsState(initial = "Anonimowy Astronauta")

    var showPostDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var postDescription by remember { mutableStateOf("") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            showPostDialog = true
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }) {
                Text("➕", fontSize = 24.sp)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (posts.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Brak postów. Bądź pierwszy!")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(posts) { post ->
                        PostCard(
                            post = post,
                            currentUserId = viewModel.getCurrentUserId() ?: "",
                            onLikeClick = { viewModel.toggleLike(post.id, post.likedBy) }
                        )
                    }
                }
            }
        }
    }

    if (showPostDialog && selectedImageUri != null) {
        AlertDialog(
            onDismissRequest = {
                showPostDialog = false
                selectedImageUri = null
            },
            title = { Text("Udostępnij swoje odkrycie") },
            text = {
                Column {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = postDescription,
                        onValueChange = { postDescription = it },
                        label = { Text("Opisz co widzisz...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.createPost(selectedImageUri!!, postDescription, authorName ?: "Anonim")
                    showPostDialog = false
                    selectedImageUri = null
                    postDescription = ""
                }) {
                    Text("Wyślij")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPostDialog = false
                    selectedImageUri = null
                }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

@Composable
fun PostCard(post: Post, currentUserId: String, onLikeClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = post.authorName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            AsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = post.description)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLikeClick) {
                    val isLiked = post.likedBy.contains(currentUserId)
                    Text(if (isLiked) "❤️" else "🤍", fontSize = 20.sp)
                }
                Text(text = "${post.likesCount} polubień")
            }
        }
    }
}

fun Context.createImageFileUri(): Uri {
    val file = File(cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        this,
        "${packageName}.fileprovider",
        file
    )
}