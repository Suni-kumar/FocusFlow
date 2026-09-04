package com.example

import android.app.Application
import android.util.Log
import com.example.data.local.FocusFlowDatabase
import com.example.data.preferences.UserPreferencesManager
import com.example.data.repository.DictationRepository
import com.example.data.repository.FlashcardRepository
import com.example.data.repository.FolderRepository
import com.example.model.DictationMockDataSource
import com.example.model.MockDataSource
import com.sepfol.app.ui.folder.FolderInitialData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FocusFlowApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: FocusFlowDatabase by lazy {
        FocusFlowDatabase.getInstance(this)
    }

    val folderRepository: FolderRepository by lazy {
        FolderRepository(database.folderDao())
    }

    val flashcardRepository: FlashcardRepository by lazy {
        FlashcardRepository(database.flashcardDao())
    }

    val dictationRepository: DictationRepository by lazy {
        DictationRepository(database.dictationDao())
    }

    val prefsManager: UserPreferencesManager by lazy {
        UserPreferencesManager(this)
    }

    companion object {
        lateinit var instance: FocusFlowApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        initializeDatabaseIfNeeded()
    }

    private fun initializeDatabaseIfNeeded() {
        if (!prefsManager.isDatabaseInitialized) {
            applicationScope.launch {
                try {
                    val folderCount = folderRepository.getItemCount()
                    val deckCount = flashcardRepository.getDeckCount()
                    val dictationCount = dictationRepository.getDeckCount()

                    if (folderCount == 0 && deckCount == 0 && dictationCount == 0) {
                        folderRepository.seedInitialData(FolderInitialData.getStarterItems())
                        flashcardRepository.seedInitialData(MockDataSource.decks)
                        dictationRepository.seedInitialData(DictationMockDataSource.getInitialDictationDecks())
                        Log.i("FocusFlowApp", "Initial starter data seeded on clean install.")
                    }
                    prefsManager.isDatabaseInitialized = true
                } catch (e: Exception) {
                    Log.e("FocusFlowApp", "Failed to initialize database", e)
                }
            }
        }
    }
}
