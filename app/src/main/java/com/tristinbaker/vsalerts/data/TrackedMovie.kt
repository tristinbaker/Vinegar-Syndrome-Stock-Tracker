package com.tristinbaker.vsalerts.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracked_movies")
data class TrackedMovie(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val handle: String,
    val variantId: Long,
    val variantTitle: String,
    val thumbnailUrl: String?,
    val lastPriceCents: Int,
    val lastCompareAtPriceCents: Int?,
    val lastInventoryQty: Int,
    val lastCheckedAt: Long,
    val vendorLabel: String? = null,
)
