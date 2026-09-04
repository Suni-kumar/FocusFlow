package com.example.data.repository

import com.example.data.local.dao.FolderDao
import com.example.data.local.entity.FolderItemEntity
import com.sepfol.app.ui.folder.FolderInitialData
import com.sepfol.app.ui.folder.FolderItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FolderRepository(
    private val dao: FolderDao? = null
) {
    private val inMemoryItems = MutableStateFlow(FolderInitialData.getStarterItems())

    val allItems: Flow<List<FolderItem>> = if (dao != null) {
        dao.getAllItems().map { list -> list.map { it.toDomain() } }
    } else {
        inMemoryItems.asStateFlow()
    }

    suspend fun insertItem(item: FolderItem) {
        if (dao != null) {
            dao.insertItem(FolderItemEntity.fromDomain(item))
        } else {
            inMemoryItems.value = listOf(item) + inMemoryItems.value.filter { it.id != item.id }
        }
    }

    suspend fun insertItems(items: List<FolderItem>) {
        if (dao != null) {
            dao.insertItems(items.map { FolderItemEntity.fromDomain(it) })
        } else {
            val newIds = items.map { it.id }.toSet()
            inMemoryItems.value = items + inMemoryItems.value.filter { it.id !in newIds }
        }
    }

    suspend fun updateItem(item: FolderItem) {
        if (dao != null) {
            dao.updateItem(FolderItemEntity.fromDomain(item))
        } else {
            inMemoryItems.value = inMemoryItems.value.map { if (it.id == item.id) item else it }
        }
    }

    suspend fun deleteItem(id: String) {
        if (dao != null) {
            dao.deleteItemById(id)
        } else {
            inMemoryItems.value = inMemoryItems.value.filter { it.id != id }
        }
    }

    suspend fun deleteItems(ids: List<String>) {
        if (dao != null) {
            dao.deleteItemsByIds(ids)
        } else {
            val idSet = ids.toSet()
            inMemoryItems.value = inMemoryItems.value.filter { it.id !in idSet }
        }
    }

    suspend fun clearAll() {
        if (dao != null) {
            dao.clearAll()
        } else {
            inMemoryItems.value = emptyList()
        }
    }

    suspend fun seedInitialData(items: List<FolderItem>) {
        if (dao != null) {
            dao.insertItems(items.map { FolderItemEntity.fromDomain(it) })
        } else {
            inMemoryItems.value = items
        }
    }

    suspend fun getItemCount(): Int {
        return dao?.getCount() ?: inMemoryItems.value.size
    }

    companion object {
        fun createInMemory(): FolderRepository = FolderRepository(null)
    }
}
