package com.tristinbaker.vsalerts.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionItemDao {
    @Query("SELECT * FROM collection_items ORDER BY title ASC")
    fun observeAll(): Flow<List<CollectionItem>>

    @Query("SELECT * FROM collection_items WHERE handle = :handle LIMIT 1")
    suspend fun findByHandle(handle: String): CollectionItem?

    @Insert
    suspend fun insert(item: CollectionItem): Long

    @Delete
    suspend fun delete(item: CollectionItem)
}
