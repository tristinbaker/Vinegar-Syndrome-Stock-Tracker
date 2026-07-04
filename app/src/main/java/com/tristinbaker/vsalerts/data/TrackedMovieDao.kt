package com.tristinbaker.vsalerts.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedMovieDao {
    @Query("SELECT * FROM tracked_movies ORDER BY title ASC")
    fun observeAll(): Flow<List<TrackedMovie>>

    @Query("SELECT * FROM tracked_movies ORDER BY title ASC")
    suspend fun getAll(): List<TrackedMovie>

    @Query("SELECT * FROM tracked_movies WHERE handle = :handle LIMIT 1")
    suspend fun findByHandle(handle: String): TrackedMovie?

    @Insert
    suspend fun insert(movie: TrackedMovie): Long

    @Update
    suspend fun update(movie: TrackedMovie)

    @Delete
    suspend fun delete(movie: TrackedMovie)
}
