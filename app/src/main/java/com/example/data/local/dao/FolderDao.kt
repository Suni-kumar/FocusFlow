package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.FolderItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folder_items ORDER BY isDirectory DESC, name ASC")
    fun getAllItems(): Flow<List<FolderItemEntity>>

    @Query("SELECT * FROM folder_items")
    suspend fun getAllItemsList(): List<FolderItemEntity>

    @Query("SELECT * FROM folder_items WHERE id = :id")
    suspend fun getItemById(id: String): FolderItemEntity?

    @Query("SELECT COUNT(*) FROM folder_items")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: FolderItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<FolderItemEntity>)

    @Update
    suspend fun updateItem(item: FolderItemEntity)

    @Query("DELETE FROM folder_items WHERE id = :id")
    suspend fun deleteItemById(id: String)

    @Query("DELETE FROM folder_items WHERE id IN (:ids)")
    suspend fun deleteItemsByIds(ids: List<String>)

    @Query("DELETE FROM folder_items")
    suspend fun clearAll()
}
