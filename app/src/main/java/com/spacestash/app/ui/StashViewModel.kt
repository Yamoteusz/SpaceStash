package com.spacestash.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spacestash.app.data.StashDatabase
import com.spacestash.app.data.StashEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class StashViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = StashDatabase.getDatabase(application).stashDao()

    // Pobieranie wszystkich zapisanych zdjęć jako strumień danych (Flow)
    val allItems: Flow<List<StashEntity>> = dao.getAllItems()

    fun addItem(title: String, url: String, date: String) {
        viewModelScope.launch {
            dao.insertItem(StashEntity(title = title, url = url, date = date))
        }
    }

    fun deleteItem(item: StashEntity) {
        viewModelScope.launch {
            dao.deleteItem(item)
        }
    }

    fun updateNote(item: StashEntity, newNote: String) {
        viewModelScope.launch {
            dao.updateItem(item.copy(note = newNote))
        }
    }
}