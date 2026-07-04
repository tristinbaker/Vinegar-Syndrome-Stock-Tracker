package com.tristinbaker.vsalerts.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A release the user owns, independent of any stock/price tracking. */
@Entity(tableName = "collection_items")
data class CollectionItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val handle: String,
    val vendorLabel: String,
    val thumbnailUrl: String?,
    val addedAt: Long,
)
