package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sepfol.app.ui.folder.FolderItem

@Entity(
    tableName = "folder_items",
    indices = [
        Index(value = ["parentId"]),
        Index(value = ["lastModified"])
    ]
)
data class FolderItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isDirectory: Boolean,
    val extension: String,
    val mimeType: String,
    val parentId: String?,
    val contentData: String?,
    val sizeBytes: Long,
    val lastModified: Long,
    val itemCount: Int,
    val isPinned: Boolean,
    val isFavorite: Boolean
) {
    fun toDomain(): FolderItem = FolderItem(
        id = id,
        name = name,
        isDirectory = isDirectory,
        extension = extension,
        mimeType = mimeType,
        parentId = parentId,
        contentData = contentData,
        sizeBytes = sizeBytes,
        lastModified = lastModified,
        itemCount = itemCount,
        isPinned = isPinned,
        isFavorite = isFavorite
    )

    companion object {
        fun fromDomain(item: FolderItem): FolderItemEntity = FolderItemEntity(
            id = item.id,
            name = item.name,
            isDirectory = item.isDirectory,
            extension = item.extension,
            mimeType = item.mimeType,
            parentId = item.parentId,
            contentData = item.contentData,
            sizeBytes = item.sizeBytes,
            lastModified = item.lastModified,
            itemCount = item.itemCount,
            isPinned = item.isPinned,
            isFavorite = item.isFavorite
        )
    }
}
