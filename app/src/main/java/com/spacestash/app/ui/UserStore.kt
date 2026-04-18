package com.spacestash.app.ui

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Tworzymy instancję DataStore (zapisze się w ukrytym pliku "settings")
val Context.dataStore by preferencesDataStore(name = "settings")

class UserStore(private val context: Context) {

    // Klucz, pod którym będziemy trzymać imię
    companion object {
        val USER_NAME_KEY = stringPreferencesKey("user_name")
    }

    // Odczytywanie imienia (Flow to taki strumień, który sam odświeży UI jak wartość się zmieni)
    val userNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY] ?: "" // Jeśli nie ma imienia, zwraca puste
    }

    // Zapisywanie nowego imienia
    suspend fun saveName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
        }
    }
}