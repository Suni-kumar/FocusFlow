package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.DictationDao
import com.example.data.local.dao.FlashcardDao
import com.example.data.local.dao.FolderDao
import com.example.data.local.entity.DictationDeckEntity
import com.example.data.local.entity.DictationWordEntity
import com.example.data.local.entity.FlashcardDeckEntity
import com.example.data.local.entity.FlashcardEntity
import com.example.data.local.entity.FolderItemEntity

@Database(
    entities = [
        FolderItemEntity::class,
        FlashcardDeckEntity::class,
        FlashcardEntity::class,
        DictationDeckEntity::class,
        DictationWordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FocusFlowDatabase : RoomDatabase() {
    abstract fun folderDao(): FolderDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun dictationDao(): DictationDao

    companion object {
        @Volatile
        private var INSTANCE: FocusFlowDatabase? = null

        fun getInstance(context: Context): FocusFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FocusFlowDatabase::class.java,
                    "focusflow.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
