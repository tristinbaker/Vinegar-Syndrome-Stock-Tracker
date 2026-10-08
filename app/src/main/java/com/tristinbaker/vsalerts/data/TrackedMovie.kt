package com.tristinbaker.vsalerts.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tristinbaker.vsalerts.network.Store

@Entity(tableName = "tracked_movies")
data class TrackedMovie(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val handle: String,
    // Rows from before multi-store support are all Vinegar Syndrome; MIGRATION_2_3 backfills that.
    @ColumnInfo(defaultValue = "VINEGAR_SYNDROME")
    val store: Store = Store.VINEGAR_SYNDROME,
    val variantId: Long,
    val variantTitle: String,
    val thumbnailUrl: String?,
    val lastPriceCents: Int,
    val lastCompareAtPriceCents: Int?,
    val lastInventoryQty: Int,
    val lastCheckedAt: Long,
    val vendorLabel: String? = null,
)
