package com.spacestash.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stash_table")
data class StashEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val date: String,
    val note: String = ""
)