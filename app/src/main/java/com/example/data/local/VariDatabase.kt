package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [StyleLyricEntity::class, SavedSongEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VariDatabase : RoomDatabase() {
    abstract fun variDao(): VariDao

    companion object {
        @Volatile
        private var instance: VariDatabase? = null

        fun getInstance(context: Context): VariDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    VariDatabase::class.java,
                    "vari_lyrics_studio.db"
                ).build().also { instance = it }
            }
        }
    }
}
