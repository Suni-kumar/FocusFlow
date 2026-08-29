package com.example.data.backup

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.example.data.preferences.UserPreferencesManager
import com.example.model.Flashcard
import com.example.model.FlashcardDeck
import com.sepfol.app.ui.folder.FolderItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupMetadata(
    val appName: String = "FocusFlow",
    val appVersion: String = "1.0",
    val schemaVersion: Int = 1,
    val exportTimestamp: Long = System.currentTimeMillis(),
    val exportDateFormatted: String = "",
    val totalFilesCount: Int = 0,
    val totalFoldersCount: Int = 0,
    val totalDecksCount: Int = 0,
    val totalCardsCount: Int = 0
)

data class BackupDataPayload(
    val metadata: BackupMetadata,
    val files: List<FolderItem>,
    val decks: List<FlashcardDeck>,
    val preferences: Map<String, Any>
)

data class BackupValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val payload: BackupDataPayload? = null
)

object BackupManager {

    private const val SCHEMA_VERSION = 1

    /**
     * Converts a Color to standard #AARRGGBB hex string
     */
    fun colorToHex(color: Color): String {
        val a = (color.alpha * 255).toInt().coerceIn(0, 255)
        val r = (color.red * 255).toInt().coerceIn(0, 255)
        val g = (color.green * 255).toInt().coerceIn(0, 255)
        val b = (color.blue * 255).toInt().coerceIn(0, 255)
        return String.format("#%02X%02X%02X%02X", a, r, g, b)
    }

    /**
     * Parses a hex color string or returns fallback
     */
    fun hexToColor(hex: String?, fallback: Color = Color(0xFF8B5CF6)): Color {
        if (hex.isNullOrBlank()) return fallback
        return try {
            val clean = hex.trim().removePrefix("#")
            when (clean.length) {
                6 -> {
                    val rgb = clean.toLong(16)
                    Color(
                        red = ((rgb shr 16) and 0xFF).toInt() / 255f,
                        green = ((rgb shr 8) and 0xFF).toInt() / 255f,
                        blue = (rgb and 0xFF).toInt() / 255f,
                        alpha = 1f
                    )
                }
                8 -> {
                    val argb = clean.toLong(16)
                    Color(
                        alpha = ((argb shr 24) and 0xFF).toInt() / 255f,
                        red = ((argb shr 16) and 0xFF).toInt() / 255f,
                        green = ((argb shr 8) and 0xFF).toInt() / 255f,
                        blue = (argb and 0xFF).toInt() / 255f
                    )
                }
                else -> fallback
            }
        } catch (e: Exception) {
            fallback
        }
    }

    /**
     * Generates a standard backup filename with timestamp: focusflow_backup_YYYY-MM-DD_HH-mm.json
     */
    fun generateBackupFileName(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())
        val dateStr = sdf.format(Date())
        return "focusflow_backup_$dateStr.json"
    }

    /**
     * Generates an individual deck backup filename: focusflow_deck_title_YYYY-MM-DD.json
     */
    fun generateDeckFileName(deckTitle: String): String {
        val safeTitle = deckTitle.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(24).trim('_')
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = sdf.format(Date())
        return "focusflow_deck_${safeTitle.ifBlank { "deck" }}_$dateStr.json"
    }

    /**
     * Creates a full application backup JSON string
     */
    fun createFullBackupJson(
        allItems: List<FolderItem>,
        allDecks: List<FlashcardDeck>,
        prefsManager: UserPreferencesManager? = null
    ): String {
        val root = JSONObject()
        val now = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateFormatted = dateFormat.format(Date(now))

        val fileItems = allItems.filter { !it.isDirectory }
        val folderItems = allItems.filter { it.isDirectory }
        val totalCards = allDecks.sumOf { it.cards.size }

        // 1. Metadata
        val metaObj = JSONObject().apply {
            put("appName", "FocusFlow")
            put("appVersion", "1.0")
            put("schemaVersion", SCHEMA_VERSION)
            put("exportTimestamp", now)
            put("exportDate", dateFormatted)
            put("totalFilesCount", fileItems.size)
            put("totalFoldersCount", folderItems.size)
            put("totalDecksCount", allDecks.size)
            put("totalCardsCount", totalCards)
        }
        root.put("metadata", metaObj)

        // 2. Preferences
        val prefsObj = JSONObject().apply {
            prefsManager?.let { pm ->
                put("isDarkTheme", pm.isDarkTheme)
                put("gridColumns", pm.gridColumns)
                put("accentThemeName", pm.accentThemeName)
                put("visualEngineName", pm.visualEngineName)
                put("isHapticEnabled", pm.isHapticEnabled)
                if (pm.customApiKey.isNotBlank()) {
                    put("customApiKey", pm.customApiKey)
                }
            }
        }
        root.put("preferences", prefsObj)

        // 3. Files and Folders (Vault)
        val filesArray = JSONArray()
        for (item in allItems) {
            val itemObj = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("isDirectory", item.isDirectory)
                put("extension", item.extension)
                put("mimeType", item.mimeType)
                put("parentId", item.parentId ?: JSONObject.NULL)
                put("contentData", item.contentData ?: JSONObject.NULL)
                put("sizeBytes", item.sizeBytes)
                put("lastModified", item.lastModified)
                put("itemCount", item.itemCount)
                put("isPinned", item.isPinned)
                put("isFavorite", item.isFavorite)
            }
            filesArray.put(itemObj)
        }
        root.put("files", filesArray)

        // 4. Flashcard Decks & Cards
        val decksArray = JSONArray()
        for (deck in allDecks) {
            val deckObj = serializeDeckToJsonObject(deck)
            decksArray.put(deckObj)
        }
        root.put("decks", decksArray)

        return root.toString(2)
    }

    /**
     * Serializes a single deck into a JSONObject
     */
    fun serializeDeckToJsonObject(deck: FlashcardDeck): JSONObject {
        return JSONObject().apply {
            put("id", deck.id)
            put("title", deck.title)
            put("description", deck.description)
            put("cardCount", deck.cards.size)
            put("lastReviewed", deck.lastReviewed)
            put("progress", deck.progress.toDouble())
            put("iconName", deck.iconName)
            put("categoryColorHex", colorToHex(deck.categoryColor))
            put("isAiGenerated", deck.isAiGenerated)

            val tagsArray = JSONArray()
            deck.tags.forEach { tagsArray.put(it) }
            put("tags", tagsArray)

            val cardsArray = JSONArray()
            for (card in deck.cards) {
                val cardObj = JSONObject().apply {
                    put("id", card.id)
                    put("front", card.front)
                    put("back", card.back)
                    put("topic", card.topic)
                    val cardTags = JSONArray()
                    card.tags.forEach { cardTags.put(it) }
                    put("tags", cardTags)
                }
                cardsArray.put(cardObj)
            }
            put("cards", cardsArray)
        }
    }

    /**
     * Serializes a single deck to a standalone JSON file string
     */
    fun serializeSingleDeck(deck: FlashcardDeck): String {
        val root = JSONObject().apply {
            put("exportType", "SINGLE_DECK")
            put("appName", "FocusFlow")
            put("schemaVersion", SCHEMA_VERSION)
            put("exportTimestamp", System.currentTimeMillis())
            put("deck", serializeDeckToJsonObject(deck))
        }
        return root.toString(2)
    }

    /**
     * Validates and parses an import JSON string
     */
    fun validateAndParseBackup(jsonString: String): BackupValidationResult {
        if (jsonString.isBlank()) {
            return BackupValidationResult(isValid = false, errorMessage = "Empty backup file or content.")
        }

        try {
            val root = JSONObject(jsonString)

            // Check if this is a single deck export
            if (root.optString("exportType") == "SINGLE_DECK" || root.has("deck")) {
                val deckObj = root.optJSONObject("deck") ?: root
                val parsedDeck = parseDeckFromJsonObject(deckObj)
                if (parsedDeck != null) {
                    val meta = BackupMetadata(
                        appName = root.optString("appName", "FocusFlow"),
                        schemaVersion = root.optInt("schemaVersion", 1),
                        exportTimestamp = root.optLong("exportTimestamp", System.currentTimeMillis()),
                        totalDecksCount = 1,
                        totalCardsCount = parsedDeck.cards.size
                    )
                    val payload = BackupDataPayload(
                        metadata = meta,
                        files = emptyList(),
                        decks = listOf(parsedDeck),
                        preferences = emptyMap()
                    )
                    return BackupValidationResult(isValid = true, payload = payload)
                } else {
                    return BackupValidationResult(isValid = false, errorMessage = "Corrupted single-deck JSON format.")
                }
            }

            // Standard Full Backup verification
            val metaObj = root.optJSONObject("metadata")
            val filesArray = root.optJSONArray("files") ?: root.optJSONArray("vaultItems") ?: root.optJSONArray("items")
            val decksArray = root.optJSONArray("decks") ?: root.optJSONArray("flashcardDecks")

            if (filesArray == null && decksArray == null) {
                // If neither arrays exist, check if it is an array directly
                return BackupValidationResult(
                    isValid = false,
                    errorMessage = "Unrecognized backup structure. Expected 'files' and 'decks' records."
                )
            }

            // Parse metadata
            val metadata = if (metaObj != null) {
                BackupMetadata(
                    appName = metaObj.optString("appName", "FocusFlow"),
                    appVersion = metaObj.optString("appVersion", "1.0"),
                    schemaVersion = metaObj.optInt("schemaVersion", 1),
                    exportTimestamp = metaObj.optLong("exportTimestamp", System.currentTimeMillis()),
                    exportDateFormatted = metaObj.optString("exportDate", ""),
                    totalFilesCount = metaObj.optInt("totalFilesCount", 0),
                    totalFoldersCount = metaObj.optInt("totalFoldersCount", 0),
                    totalDecksCount = metaObj.optInt("totalDecksCount", 0),
                    totalCardsCount = metaObj.optInt("totalCardsCount", 0)
                )
            } else {
                BackupMetadata(
                    exportDateFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                )
            }

            // Parse preferences
            val prefsMap = mutableMapOf<String, Any>()
            val prefsObj = root.optJSONObject("preferences")
            if (prefsObj != null) {
                val keys = prefsObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    prefsMap[k] = prefsObj.get(k)
                }
            }

            // Parse files
            val parsedFiles = mutableListOf<FolderItem>()
            if (filesArray != null) {
                for (i in 0 until filesArray.length()) {
                    val fObj = filesArray.optJSONObject(i) ?: continue
                    val id = fObj.optString("id", "item_${System.currentTimeMillis()}_$i")
                    val name = fObj.optString("name", "Untitled")
                    val isDir = fObj.optBoolean("isDirectory", false)
                    val ext = fObj.optString("extension", if (isDir) "" else "md")
                    val mime = fObj.optString("mimeType", if (isDir) "vnd.android.document/directory" else "text/markdown")
                    val parentId = if (fObj.isNull("parentId")) null else fObj.optString("parentId", null)
                    val contentData = if (fObj.isNull("contentData")) null else fObj.optString("contentData", null)
                    val sizeBytes = fObj.optLong("sizeBytes", 0L)
                    val lastModified = fObj.optLong("lastModified", System.currentTimeMillis())
                    val itemCount = fObj.optInt("itemCount", 0)
                    val isPinned = fObj.optBoolean("isPinned", false)
                    val isFavorite = fObj.optBoolean("isFavorite", false)

                    parsedFiles.add(
                        FolderItem(
                            id = id,
                            name = name,
                            isDirectory = isDir,
                            extension = ext,
                            mimeType = mime,
                            parentId = parentId,
                            contentData = contentData,
                            sizeBytes = sizeBytes,
                            lastModified = lastModified,
                            itemCount = itemCount,
                            isPinned = isPinned,
                            isFavorite = isFavorite
                        )
                    )
                }
            }

            // Parse decks
            val parsedDecks = mutableListOf<FlashcardDeck>()
            if (decksArray != null) {
                for (i in 0 until decksArray.length()) {
                    val dObj = decksArray.optJSONObject(i) ?: continue
                    val parsedDeck = parseDeckFromJsonObject(dObj)
                    if (parsedDeck != null) {
                        parsedDecks.add(parsedDeck)
                    }
                }
            }

            val finalMetadata = metadata.copy(
                totalFilesCount = parsedFiles.count { !it.isDirectory },
                totalFoldersCount = parsedFiles.count { it.isDirectory },
                totalDecksCount = parsedDecks.size,
                totalCardsCount = parsedDecks.sumOf { it.cards.size }
            )

            val payload = BackupDataPayload(
                metadata = finalMetadata,
                files = parsedFiles,
                decks = parsedDecks,
                preferences = prefsMap
            )

            return BackupValidationResult(isValid = true, payload = payload)

        } catch (e: Exception) {
            return BackupValidationResult(
                isValid = false,
                errorMessage = "JSON parsing error: ${e.localizedMessage ?: "Invalid structure"}"
            )
        }
    }

    /**
     * Parses a FlashcardDeck from a JSONObject
     */
    fun parseDeckFromJsonObject(dObj: JSONObject): FlashcardDeck? {
        return try {
            val id = dObj.optString("id", "deck_${System.currentTimeMillis()}")
            val title = dObj.optString("title", "Imported Deck")
            val desc = dObj.optString("description", "Restored flashcard deck")
            val progress = dObj.optDouble("progress", 0.0).toFloat().coerceIn(0f, 1f)
            val iconName = dObj.optString("iconName", "psychology")
            val colorHex = dObj.optString("categoryColorHex", "#8B5CF6")
            val isAiGen = dObj.optBoolean("isAiGenerated", false)
            val lastReviewed = dObj.optString("lastReviewed", "Restored")

            val tagsList = mutableListOf<String>()
            val tagsArr = dObj.optJSONArray("tags")
            if (tagsArr != null) {
                for (t in 0 until tagsArr.length()) {
                    tagsList.add(tagsArr.optString(t))
                }
            }

            val cardsList = mutableListOf<Flashcard>()
            val cardsArr = dObj.optJSONArray("cards")
            if (cardsArr != null) {
                for (c in 0 until cardsArr.length()) {
                    val cardObj = cardsArr.optJSONObject(c) ?: continue
                    val cId = cardObj.optString("id", "c_${id}_$c")
                    val front = cardObj.optString("front", "")
                    val back = cardObj.optString("back", "")
                    val topic = cardObj.optString("topic", title)

                    val cardTags = mutableListOf<String>()
                    val cTagsArr = cardObj.optJSONArray("tags")
                    if (cTagsArr != null) {
                        for (ct in 0 until cTagsArr.length()) {
                            cardTags.add(cTagsArr.optString(ct))
                        }
                    }

                    if (front.isNotBlank() || back.isNotBlank()) {
                        cardsList.add(
                            Flashcard(
                                id = cId,
                                front = front.ifBlank { "Prompt" },
                                back = back.ifBlank { "Answer" },
                                topic = topic,
                                tags = cardTags
                            )
                        )
                    }
                }
            }

            FlashcardDeck(
                id = id,
                title = title,
                description = desc,
                cardCount = cardsList.size,
                lastReviewed = lastReviewed,
                progress = progress,
                iconName = iconName,
                categoryColor = hexToColor(colorHex),
                cards = cardsList,
                tags = tagsList,
                isAiGenerated = isAiGen
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Reads string content from Uri via ContentResolver
     */
    fun readTextFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Writes string content to Uri via ContentResolver
     */
    fun writeTextToUri(context: Context, uri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(content)
                    writer.flush()
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
