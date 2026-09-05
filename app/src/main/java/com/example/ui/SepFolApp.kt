package com.example.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.backup.BackupDataPayload
import com.example.data.backup.BackupManager
import com.example.data.preferences.UserPreferencesManager
import com.example.model.AccentTheme
import com.example.model.BrightnessMode
import com.example.model.DictationDeck
import com.example.model.FlashcardDeck
import com.example.model.MockDataSource
import com.example.model.VisualEngine
import com.example.ui.components.AmbientLiquidOrbsBackground
import com.example.ui.components.MainTab
import com.example.ui.components.SepFolBottomNavBar
import com.example.ui.components.SepFolTopAppBar
import com.example.ui.components.WorkspaceSwitcherModal
import com.example.ui.dialogs.AiGenerateDeckDialog
import com.example.ui.dialogs.AiGenerateDictationDialog
import com.example.ui.dialogs.CreateDeckDialog
import com.example.ui.dialogs.CreateDictationDeckDialog
import com.example.ui.dialogs.EditDictationDeckDialog
import com.example.ui.dialogs.ExportSuccessDialog
import com.example.ui.dialogs.ManualJsonImportDialog
import com.example.ui.dialogs.RenameDeckDialog
import com.example.ui.dialogs.RestoreConfirmationDialog
import com.example.ui.screens.DecksDashboardScreen
import com.example.ui.screens.DictationCheckingScreen
import com.example.ui.screens.DictationPracticeScreen
import com.example.ui.screens.DictationWorkspaceScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.screens.StudyScreen
import com.example.ui.screens.VoiceSettingsScreen
import com.example.ui.theme.SepFolTheme
import com.example.viewmodel.DeckViewModel
import com.example.viewmodel.DictationViewModel
import com.sepfol.app.ui.folder.FolderScreen
import com.sepfol.app.ui.folder.FolderViewModel
import kotlinx.coroutines.launch

enum class ScreenState {
    MAIN_WORKSPACE,
    ALL_DECKS,
    STUDY_STAGE,
    SETTINGS,
    VOICE_SETTINGS,
    DICTATION_PRACTICE,
    DICTATION_CHECKING
}

@Composable
fun SepFolApp() {
    val context = LocalContext.current
    val prefsManager = remember { UserPreferencesManager(context) }

    var selectedTab by remember { mutableStateOf(MainTab.FILES) }
    var screenStack by remember { mutableStateOf(listOf(ScreenState.MAIN_WORKSPACE)) }
    val currentScreen = screenStack.lastOrNull() ?: ScreenState.MAIN_WORKSPACE

    var visualEngine by remember {
        mutableStateOf(
            try {
                VisualEngine.valueOf(prefsManager.visualEngineName)
            } catch (e: Exception) {
                VisualEngine.LIQUID_GLASS_3D
            }
        )
    }
    var gridColumns by remember { mutableIntStateOf(prefsManager.gridColumns) }
    var selectedAccent by remember {
        mutableStateOf(
            try {
                AccentTheme.valueOf(prefsManager.accentThemeName)
            } catch (e: Exception) {
                AccentTheme.BIO_MATRIX
            }
        )
    }
    var brightnessMode by remember {
        mutableStateOf(
            try {
                BrightnessMode.valueOf(prefsManager.brightnessModeName)
            } catch (e: Exception) {
                BrightnessMode.DARK
            }
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val folderViewModel: FolderViewModel = viewModel()
    val deckViewModel: DeckViewModel = viewModel()
    val dictationViewModel: DictationViewModel = viewModel()

    var isSearchActive by remember { mutableStateOf(false) }
    var isWorkspaceSwitcherOpen by remember { mutableStateOf(false) }
    var isHapticEnabled by remember { mutableStateOf(prefsManager.isHapticEnabled) }

    val isSystemDark = isSystemInDarkTheme()
    val isEffectiveDarkTheme = when (brightnessMode) {
        BrightnessMode.DARK -> true
        BrightnessMode.LIGHT -> false
        BrightnessMode.SYSTEM -> isSystemDark
    }

    val folderUiState by folderViewModel.uiState.collectAsState()
    val deckUiState by deckViewModel.uiState.collectAsState()
    var activeDeckId by remember { mutableStateOf<String?>(null) }
    val activeDeck = deckUiState.decks.find { it.id == activeDeckId } ?: deckUiState.decks.firstOrNull()
    val dictationUiState by dictationViewModel.uiState.collectAsState()

    // Backup & Restore Dialog States
    var pendingExportJson by remember { mutableStateOf<String?>(null) }
    var pendingExportFileName by remember { mutableStateOf("") }
    var isExportSuccessDialogOpen by remember { mutableStateOf(false) }

    var pendingRestorePayload by remember { mutableStateOf<BackupDataPayload?>(null) }
    var isRestoreConfirmDialogOpen by remember { mutableStateOf(false) }
    var isManualJsonDialogOpen by remember { mutableStateOf(false) }

    // System File Creator for Exporting Backup JSON
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null && pendingExportJson != null) {
            val success = BackupManager.writeTextToUri(context, uri, pendingExportJson!!)
            if (success) {
                scope.launch {
                    snackbarHostState.showSnackbar("Backup saved to file successfully!")
                }
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("Failed to write backup to chosen location.")
                }
            }
        }
    }

    // System File Picker for Importing Backup JSON
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val text = BackupManager.readTextFromUri(context, uri)
            if (text != null) {
                val result = BackupManager.validateAndParseBackup(text)
                if (result.isValid && result.payload != null) {
                    pendingRestorePayload = result.payload
                    isRestoreConfirmDialogOpen = true
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar("Error: ${result.errorMessage ?: "Invalid backup file"}")
                    }
                }
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("Failed to read backup file.")
                }
            }
        }
    }

    // Helper to generate full backup
    val triggerExportBackup = {
        val allFiles = folderUiState.allItems
        val allDecks = deckUiState.decks
        val fileName = BackupManager.generateBackupFileName()
        val json = BackupManager.createFullBackupJson(
            allItems = allFiles,
            allDecks = allDecks,
            prefsManager = prefsManager
        )
        pendingExportJson = json
        pendingExportFileName = fileName
        isExportSuccessDialogOpen = true
    }

    // Helper to apply restore payload
    val applyRestore: (BackupDataPayload, Boolean) -> Unit = { payload, merge ->
        if (payload.files.isNotEmpty() || !merge) {
            folderViewModel.restoreItems(payload.files, merge)
        }
        if (payload.decks.isNotEmpty() || !merge) {
            deckViewModel.restoreDecks(payload.decks, merge)
        }
        payload.preferences["isDarkTheme"]?.let {
            if (it is Boolean) {
                val mode = if (it) BrightnessMode.DARK else BrightnessMode.LIGHT
                brightnessMode = mode
                prefsManager.isDarkTheme = it
                prefsManager.brightnessModeName = mode.name
            }
        }
        payload.preferences["gridColumns"]?.let {
            if (it is Int) {
                gridColumns = it
                prefsManager.gridColumns = it
            }
        }
        payload.preferences["isHapticEnabled"]?.let {
            if (it is Boolean) {
                isHapticEnabled = it
                prefsManager.isHapticEnabled = it
            }
        }

        val totalFiles = payload.files.count { !it.isDirectory }
        val totalDecks = payload.decks.size
        val totalCards = payload.decks.sumOf { it.cards.size }
        scope.launch {
            snackbarHostState.showSnackbar(
                if (merge) "Merged $totalFiles files & $totalDecks decks ($totalCards cards)"
                else "Restored $totalFiles files & $totalDecks decks ($totalCards cards)"
            )
        }
        isRestoreConfirmDialogOpen = false
        pendingRestorePayload = null
    }

    // Initialize custom API key from persistent storage on startup
    LaunchedEffect(Unit) {
        val savedKey = prefsManager.customApiKey
        if (savedKey.isNotBlank()) {
            deckViewModel.setInitialCustomApiKey(savedKey)
            dictationViewModel.setInitialCustomApiKey(savedKey)
        }
    }

    // Determine current workspace multi-selection state
    val isFilesTab = (currentScreen == ScreenState.MAIN_WORKSPACE && selectedTab == MainTab.FILES)
    val currentSelectionCount = if (isFilesTab) {
        folderUiState.selectedItemIds.size
    } else {
        deckUiState.selectedDeckIds.size
    }

    // Navigation back stack handler
    val navigateBack: () -> Unit = {
        if (isWorkspaceSwitcherOpen) {
            isWorkspaceSwitcherOpen = false
        } else if (folderUiState.isSelectionMode) {
            folderViewModel.clearSelection()
        } else if (deckUiState.isSelectionMode) {
            deckViewModel.clearSelection()
        } else if (isSearchActive) {
            isSearchActive = false
            folderViewModel.setSearchQuery("")
        } else if (screenStack.size > 1) {
            screenStack = screenStack.dropLast(1)
        } else if (selectedTab != MainTab.FILES) {
            selectedTab = MainTab.FILES
        }
    }

    // Intercept hardware/gesture back press for top-level screens & tabs
    BackHandler(enabled = isWorkspaceSwitcherOpen || folderUiState.isSelectionMode || deckUiState.isSelectionMode || isSearchActive || screenStack.size > 1 || (selectedTab != MainTab.FILES && currentScreen == ScreenState.MAIN_WORKSPACE)) {
        navigateBack()
    }

    // Sync grid columns to FolderViewModel
    LaunchedEffect(gridColumns) {
        folderViewModel.setGridColumns(gridColumns)
    }

    // Observe status messages from FolderViewModel
    LaunchedEffect(folderUiState.statusMessage) {
        folderUiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            folderViewModel.clearStatusMessage()
        }
    }

    // Observe status messages from DeckViewModel
    LaunchedEffect(deckUiState.statusMessage) {
        deckUiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            deckViewModel.clearStatusMessage()
        }
    }

    // Observe status messages from DictationViewModel
    LaunchedEffect(dictationUiState.statusMessage) {
        dictationUiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            dictationViewModel.clearStatusMessage()
        }
    }

    SepFolTheme(
        darkTheme = isEffectiveDarkTheme,
        accentTheme = selectedAccent,
        is3DGlassEnabled = (visualEngine == VisualEngine.LIQUID_GLASS_3D)
    ) {
        // Show FocusFlow top app bar ONLY on Home Vault (root) and Flashcard Studio tab
        val isTopAppBarVisible = currentScreen == ScreenState.MAIN_WORKSPACE &&
                folderUiState.selectedViewerItem == null &&
                selectedTab != MainTab.DICTATION &&
                (selectedTab == MainTab.STUDIO || folderUiState.folderStack.size <= 1)

        AmbientLiquidOrbsBackground(
            accentTheme = selectedAccent,
            isDarkTheme = isEffectiveDarkTheme
        ) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    if (isTopAppBarVisible) {
                        SepFolTopAppBar(
                            title = "FocusFlow",
                            searchQuery = folderUiState.searchQuery,
                            onSearchQueryChange = { folderViewModel.setSearchQuery(it) },
                            isSearchActive = isSearchActive,
                            onSearchActiveChange = { active ->
                                isSearchActive = active
                                if (!active) folderViewModel.setSearchQuery("")
                            },
                            isDarkTheme = isEffectiveDarkTheme,
                            onToggleTheme = {
                                val nextMode = if (isEffectiveDarkTheme) BrightnessMode.LIGHT else BrightnessMode.DARK
                                brightnessMode = nextMode
                                prefsManager.brightnessModeName = nextMode.name
                                prefsManager.isDarkTheme = (nextMode == BrightnessMode.DARK)
                            },
                            onSettingsClick = { screenStack = screenStack + ScreenState.SETTINGS },
                            onProfileClick = { screenStack = screenStack + ScreenState.SETTINGS },
                            selectionCount = currentSelectionCount,
                            onClearSelection = {
                                if (isFilesTab) folderViewModel.clearSelection() else deckViewModel.clearSelection()
                            },
                            onDeleteSelected = {
                                if (isFilesTab) folderViewModel.deleteSelectedItems() else deckViewModel.deleteSelectedDecks()
                            },
                            onRenameSelected = {
                                if (isFilesTab) folderViewModel.openRenameForSelected() else deckViewModel.openRenameSelected()
                            }
                        )
                    }
                },
                containerColor = androidx.compose.ui.graphics.Color.Transparent
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    AnimatedContent(
                        targetState = Pair(currentScreen, selectedTab),
                        transitionSpec = {
                            val isForward = targetState.first != ScreenState.MAIN_WORKSPACE && initialState.first == ScreenState.MAIN_WORKSPACE
                            val isBackward = targetState.first == ScreenState.MAIN_WORKSPACE && initialState.first != ScreenState.MAIN_WORKSPACE

                            if (isForward) {
                                (androidx.compose.animation.slideInHorizontally(
                                    animationSpec = androidx.compose.animation.core.tween(260, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                                ) { (it * 0.20f).toInt() } + androidx.compose.animation.fadeIn(
                                    animationSpec = androidx.compose.animation.core.tween(220)
                                )) togetherWith (androidx.compose.animation.slideOutHorizontally(
                                    animationSpec = androidx.compose.animation.core.tween(240, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                                ) { -(it * 0.10f).toInt() } + androidx.compose.animation.fadeOut(
                                    animationSpec = androidx.compose.animation.core.tween(180)
                                ))
                            } else if (isBackward) {
                                (androidx.compose.animation.slideInHorizontally(
                                    animationSpec = androidx.compose.animation.core.tween(260, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                                ) { -(it * 0.10f).toInt() } + androidx.compose.animation.fadeIn(
                                    animationSpec = androidx.compose.animation.core.tween(220)
                                )) togetherWith (androidx.compose.animation.slideOutHorizontally(
                                    animationSpec = androidx.compose.animation.core.tween(240, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                                ) { (it * 0.20f).toInt() } + androidx.compose.animation.fadeOut(
                                    animationSpec = androidx.compose.animation.core.tween(180)
                                ))
                            } else {
                                androidx.compose.animation.fadeIn(
                                    animationSpec = androidx.compose.animation.core.tween(160)
                                ) togetherWith androidx.compose.animation.fadeOut(
                                    animationSpec = androidx.compose.animation.core.tween(120)
                                )
                            }
                        },
                        label = "screenTransition"
                    ) { (screen, tab) ->
                        when {
                            screen == ScreenState.SETTINGS -> {
                                SettingsScreen(
                                    onDoneClick = navigateBack,
                                    isDarkTheme = isEffectiveDarkTheme,
                                    brightnessMode = brightnessMode,
                                    onBrightnessModeChanged = { mode ->
                                        brightnessMode = mode
                                        prefsManager.brightnessModeName = mode.name
                                        if (mode != BrightnessMode.SYSTEM) {
                                            prefsManager.isDarkTheme = (mode == BrightnessMode.DARK)
                                        }
                                    },
                                    onThemeToggled = { dark ->
                                        val mode = if (dark) BrightnessMode.DARK else BrightnessMode.LIGHT
                                        brightnessMode = mode
                                        prefsManager.brightnessModeName = mode.name
                                        prefsManager.isDarkTheme = dark
                                    },
                                    selectedEngine = visualEngine,
                                    onEngineChanged = {
                                        visualEngine = it
                                        prefsManager.visualEngineName = it.name
                                    },
                                    gridCols = gridColumns,
                                    onGridColsChanged = {
                                        gridColumns = it
                                        prefsManager.gridColumns = it
                                    },
                                    selectedAccent = selectedAccent,
                                    onAccentChanged = {
                                        selectedAccent = it
                                        prefsManager.accentThemeName = it.name
                                    },
                                    customApiKey = deckUiState.customApiKey,
                                    onCustomApiKeyChanged = { key ->
                                        deckViewModel.updateCustomApiKey(key)
                                        dictationViewModel.updateCustomApiKey(key)
                                        prefsManager.customApiKey = key
                                    },
                                    preferGeminiVoice = prefsManager.isPreferGeminiVoice,
                                    onPreferGeminiVoiceToggled = { enabled ->
                                        prefsManager.isPreferGeminiVoice = enabled
                                    },
                                    isHapticEnabled = isHapticEnabled,
                                    onHapticToggled = { haptic ->
                                        isHapticEnabled = haptic
                                        prefsManager.isHapticEnabled = haptic
                                    },
                                    filesCount = folderUiState.allItems.count { !it.isDirectory },
                                    foldersCount = folderUiState.allItems.count { it.isDirectory },
                                    decksCount = deckUiState.decks.size,
                                    cardsCount = deckUiState.decks.sumOf { it.cards.size },
                                    onVoicesClick = {
                                        screenStack = screenStack + ScreenState.VOICE_SETTINGS
                                    },
                                    onExportBackupClick = {
                                        triggerExportBackup()
                                    },
                                    onImportBackupClick = {
                                        importFileLauncher.launch("*")
                                    },
                                    onPasteJsonClick = {
                                        isManualJsonDialogOpen = true
                                    }
                                )
                            }
                            screen == ScreenState.VOICE_SETTINGS -> {
                                VoiceSettingsScreen(
                                    onBackClick = navigateBack,
                                    selectedAccent = selectedAccent
                                )
                            }
                            screen == ScreenState.STUDY_STAGE && activeDeck != null -> {
                                StudyScreen(
                                    deck = activeDeck!!,
                                    isDarkTheme = isEffectiveDarkTheme,
                                    onToggleTheme = {
                                        val nextMode = if (isEffectiveDarkTheme) BrightnessMode.LIGHT else BrightnessMode.DARK
                                        brightnessMode = nextMode
                                        prefsManager.brightnessModeName = nextMode.name
                                        prefsManager.isDarkTheme = (nextMode == BrightnessMode.DARK)
                                    },
                                    onBackClick = navigateBack,
                                    onDeckProgressUpdate = { progress ->
                                        deckViewModel.updateDeckProgress(activeDeck!!.id, progress)
                                    },
                                    onToggleCardMastery = { cardId, isMastered ->
                                        deckViewModel.toggleCardMastery(activeDeck!!.id, cardId, isMastered)
                                    }
                                )
                            }
                            screen == ScreenState.DICTATION_PRACTICE && dictationUiState.activeDeck != null -> {
                                DictationPracticeScreen(
                                    deck = dictationUiState.activeDeck!!,
                                    viewModel = dictationViewModel,
                                    onBackClick = navigateBack,
                                    onOpenCheckingTime = {
                                        screenStack = screenStack + ScreenState.DICTATION_CHECKING
                                    }
                                )
                            }
                            screen == ScreenState.DICTATION_CHECKING && dictationUiState.activeDeck != null -> {
                                DictationCheckingScreen(
                                    deck = dictationUiState.activeDeck!!,
                                    viewModel = dictationViewModel,
                                    onBackClick = navigateBack,
                                    onRestartDictation = {
                                        dictationViewModel.startPracticeSession(dictationUiState.activeDeck!!)
                                        screenStack = screenStack.filter { it != ScreenState.DICTATION_CHECKING }
                                    },
                                    onFinishAndSave = {
                                        navigateBack()
                                    }
                                )
                            }
                            screen == ScreenState.ALL_DECKS -> {
                                DecksDashboardScreen(
                                    onBackClick = navigateBack,
                                    decks = deckUiState.decks,
                                    selectedDeckIds = deckUiState.selectedDeckIds,
                                    gridColumns = gridColumns,
                                    onToggleSelection = { deckViewModel.toggleDeckSelection(it) },
                                    onClearSelection = { deckViewModel.clearSelection() },
                                    onToggleStar = { deckViewModel.toggleStarDeck(it) },
                                    onDeckClick = { deck ->
                                        activeDeckId = deck.id
                                        screenStack = screenStack + ScreenState.STUDY_STAGE
                                    },
                                    onCreateDeckClick = {
                                        deckViewModel.openCreateDeckDialog()
                                    },
                                    onAiGenerateClick = {
                                        deckViewModel.openAiGenerateDialog()
                                    },
                                    onRenameDeckClick = { deck ->
                                        deckViewModel.openRenameDeckDialog(deck)
                                    },
                                    onDeleteDeckClick = { deck ->
                                        deckViewModel.deleteDeck(deck)
                                    }
                                )
                            }
                            tab == MainTab.FILES -> {
                                FolderScreen(
                                    viewModel = folderViewModel,
                                    onImportClick = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Import document / flashcards")
                                        }
                                    },
                                    onSwipeUpFab = {
                                        isWorkspaceSwitcherOpen = true
                                    }
                                )
                            }
                            tab == MainTab.DICTATION -> {
                                DictationWorkspaceScreen(
                                    viewModel = dictationViewModel,
                                    gridColumns = gridColumns,
                                    onDeckClick = { deck ->
                                        dictationViewModel.startPracticeSession(deck)
                                        screenStack = screenStack + ScreenState.DICTATION_PRACTICE
                                    },
                                    onVoiceSettingsClick = {
                                        screenStack = screenStack + ScreenState.VOICE_SETTINGS
                                    },
                                    onSwipeUpFab = {
                                        isWorkspaceSwitcherOpen = true
                                    }
                                )
                            }
                            else -> { // MainTab.STUDIO
                                StudioScreen(
                                    decks = deckUiState.decks,
                                    selectedDeckIds = deckUiState.selectedDeckIds,
                                    gridColumns = gridColumns,
                                    onToggleSelection = { deckViewModel.toggleDeckSelection(it) },
                                    onClearSelection = { deckViewModel.clearSelection() },
                                    onViewAllDecksClick = { screenStack = screenStack + ScreenState.ALL_DECKS },
                                    onDeckClick = { deck ->
                                        activeDeckId = deck.id
                                        screenStack = screenStack + ScreenState.STUDY_STAGE
                                    },
                                    onCreateDeckClick = {
                                        deckViewModel.openCreateDeckDialog()
                                    },
                                    onAiGenerateClick = {
                                        deckViewModel.openAiGenerateDialog()
                                    },
                                    onRenameDeckClick = { deck ->
                                        deckViewModel.openRenameDeckDialog(deck)
                                    },
                                    onDeleteDeckClick = { deck ->
                                        deckViewModel.deleteDeck(deck)
                                    },
                                    onSwipeUpFab = {
                                        isWorkspaceSwitcherOpen = true
                                    }
                                )
                            }
                    }
                }

                // AI Generate Flashcards Deck Dialog
                if (deckUiState.isAiGenerateDialogOpen) {
                    AiGenerateDeckDialog(
                        initialPrompt = deckUiState.aiInitialPrompt,
                        isGenerating = deckUiState.isAiGenerating,
                        progressMessage = deckUiState.aiGenerationProgressMessage,
                        hasCustomApiKey = deckUiState.customApiKey.isNotBlank(),
                        onDismiss = { deckViewModel.dismissAiGenerateDialog() },
                        onConfigureApiKeyClick = {
                            deckViewModel.dismissAiGenerateDialog()
                            screenStack = screenStack + ScreenState.SETTINGS
                        },
                        onGenerate = { deckTitle, topic, count, instructions ->
                            deckViewModel.generateAiDeck(
                                topicOrNotes = topic,
                                targetCardCount = count,
                                deckTitle = deckTitle,
                                userInstructions = instructions,
                                onComplete = { generatedDeck ->
                                    activeDeckId = generatedDeck.id
                                    deckViewModel.dismissAiGenerateDialog()
                                    screenStack = screenStack + ScreenState.STUDY_STAGE
                                }
                            )
                        }
                    )
                }

                // Create Deck Dialog
                if (deckUiState.isCreateDeckDialogOpen) {
                    CreateDeckDialog(
                        onDismiss = { deckViewModel.dismissCreateDeckDialog() },
                        onCreateDeck = { title, desc, topic, color, customCards ->
                            deckViewModel.createDeck(title, desc, topic, color, customCards)
                        }
                    )
                }

                // Rename Deck Dialog
                deckUiState.renameTargetDeck?.let { target ->
                    RenameDeckDialog(
                        deck = target,
                        onDismiss = { deckViewModel.dismissRenameDeckDialog() },
                        onConfirm = { newTitle ->
                            deckViewModel.renameDeck(target.id, newTitle)
                        }
                    )
                }

                // --- Dictation Workspace Dialogs ---

                // Create Dictation Deck Dialog
                if (dictationUiState.isCreateDeckDialogOpen) {
                    CreateDictationDeckDialog(
                        onDismiss = { dictationViewModel.dismissCreateDeckDialog() },
                        onCreateDeck = { title, desc, color, words, tags ->
                            dictationViewModel.createDeck(title, desc, color, words, tags)
                        }
                    )
                }

                // Edit Dictation Deck Dialog
                dictationUiState.editTargetDeck?.let { target ->
                    EditDictationDeckDialog(
                        deck = target,
                        onDismiss = { dictationViewModel.dismissEditDeckDialog() },
                        onSave = { updatedDeck ->
                            dictationViewModel.updateDeck(updatedDeck)
                        }
                    )
                }

                // AI Generate Dictation Deck Dialog
                if (dictationUiState.isAiGenerateDialogOpen) {
                    AiGenerateDictationDialog(
                        initialPrompt = dictationUiState.aiInitialPrompt,
                        isGenerating = dictationUiState.isAiGenerating,
                        progressMessage = dictationUiState.aiGenerationProgressMessage,
                        hasCustomApiKey = dictationUiState.customApiKey.isNotBlank(),
                        onDismiss = { dictationViewModel.dismissAiGenerateDialog() },
                        onConfigureApiKeyClick = {
                            dictationViewModel.dismissAiGenerateDialog()
                            screenStack = screenStack + ScreenState.SETTINGS
                        },
                        onGenerate = { deckTitle, inputContent, count ->
                            dictationViewModel.generateAiDeck(
                                topicOrNotes = inputContent,
                                targetWordCount = count,
                                deckTitle = deckTitle,
                                onComplete = {
                                    // Created successfully
                                }
                            )
                        }
                    )
                }

                // Bottom-Middle Animated Workspace Switcher (Triggered by Swiping Up on the FAB)
                WorkspaceSwitcherModal(
                    isOpen = isWorkspaceSwitcherOpen,
                    currentTab = selectedTab,
                    onTabSelected = { tab ->
                        selectedTab = tab
                        if (currentScreen != ScreenState.MAIN_WORKSPACE) {
                            screenStack = listOf(ScreenState.MAIN_WORKSPACE)
                        }
                    },
                    onDismiss = { isWorkspaceSwitcherOpen = false }
                )

                // 1. Full Backup Export Generated Dialog
                if (isExportSuccessDialogOpen && pendingExportJson != null) {
                    ExportSuccessDialog(
                        fileName = pendingExportFileName,
                        jsonContent = pendingExportJson ?: "",
                        filesCount = folderUiState.allItems.count { !it.isDirectory },
                        decksCount = deckUiState.decks.size,
                        cardsCount = deckUiState.decks.sumOf { it.cards.size },
                        onDismiss = { isExportSuccessDialogOpen = false },
                        onSaveDocument = {
                            createDocumentLauncher.launch(pendingExportFileName)
                        }
                    )
                }

                // 2. Full Backup Restore Strategy / Preview Dialog
                if (isRestoreConfirmDialogOpen && pendingRestorePayload != null) {
                    RestoreConfirmationDialog(
                        payload = pendingRestorePayload!!,
                        onDismiss = {
                            isRestoreConfirmDialogOpen = false
                            pendingRestorePayload = null
                        },
                        onConfirmRestore = { merge ->
                            applyRestore(pendingRestorePayload!!, merge)
                        }
                    )
                }

                // 3. Manual Paste Raw JSON Import Dialog
                if (isManualJsonDialogOpen) {
                    ManualJsonImportDialog(
                        onDismiss = { isManualJsonDialogOpen = false },
                        onImportJson = { rawJson ->
                            val result = BackupManager.validateAndParseBackup(rawJson)
                            if (result.isValid && result.payload != null) {
                                isManualJsonDialogOpen = false
                                pendingRestorePayload = result.payload
                                isRestoreConfirmDialogOpen = true
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Error: ${result.errorMessage ?: "Invalid JSON backup"}")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
}
