package com.example.data.dao

import androidx.room.*
import com.example.data.entity.ServiceItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services ORDER BY createdAt DESC")
    fun getAllServices(): Flow<List<ServiceItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceItem)

    @Query("DELETE FROM services WHERE id = :id")
    suspend fun deleteServiceById(id: Int)

    @Query("SELECT * FROM services WHERE sellerUsername = :sellerUsername ORDER BY createdAt DESC")
    fun getServicesBySeller(sellerUsername: String): Flow<List<ServiceItem>>

    @Query("SELECT * FROM services WHERE title LIKE :searchQuery OR category LIKE :searchQuery OR description LIKE :searchQuery ORDER BY createdAt DESC")
    fun searchServices(searchQuery: String): Flow<List<ServiceItem>>
}
