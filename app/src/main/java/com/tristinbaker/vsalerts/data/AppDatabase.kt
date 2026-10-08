package com.tristinbaker.vsalerts.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TrackedMovie::class, CollectionItem::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackedMovieDao(): TrackedMovieDao
    abstract fun collectionItemDao(): CollectionItemDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tracked_movies ADD COLUMN vendorLabel TEXT")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `collection_items` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`handle` TEXT NOT NULL, " +
                        "`vendorLabel` TEXT NOT NULL, " +
                        "`thumbnailUrl` TEXT, " +
                        "`addedAt` INTEGER NOT NULL)",
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tracked_movies ADD COLUMN store TEXT NOT NULL DEFAULT 'VINEGAR_SYNDROME'")
                db.execSQL("ALTER TABLE collection_items ADD COLUMN store TEXT NOT NULL DEFAULT 'VINEGAR_SYNDROME'")
            }
        }

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vs_alerts.db",
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { instance = it }
            }
    }
}
