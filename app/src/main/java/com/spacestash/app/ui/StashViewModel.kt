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

    val allItems: Flow<List<StashEntity>> = dao.getAllItems()

    fun addItem(title: String, url: String, date: String, note: String = "") {
        viewModelScope.launch {
            dao.insertItem(StashEntity(title = title, url = url, date = date, note = note))
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