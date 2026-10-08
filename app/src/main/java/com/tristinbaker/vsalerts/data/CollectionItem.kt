package com.tristinbaker.vsalerts.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tristinbaker.vsalerts.network.Store

/** A release the user owns, independent of any stock/price tracking. */
@Entity(tableName = "collection_items")
data class CollectionItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val handle: String,
    // Rows from before multi-store support are all Vinegar Syndrome; MIGRATION_2_3 backfills that.
    @ColumnInfo(defaultValue = "VINEGAR_SYNDROME")
    val store: Store = Store.VINEGAR_SYNDROME,
    val vendorLabel: String,
    val thumbnailUrl: String?,
    val addedAt: Long,
)
