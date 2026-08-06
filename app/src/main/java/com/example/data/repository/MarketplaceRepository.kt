package com.example.data.repository

import com.example.data.dao.UserDao
import com.example.data.dao.ServiceDao
import com.example.data.entity.User
import com.example.data.entity.ServiceItem
import kotlinx.coroutines.flow.Flow

class MarketplaceRepository(
    private val userDao: UserDao,
    private val serviceDao: ServiceDao
) {
    // Services flow
    val allServices: Flow<List<ServiceItem>> = serviceDao.getAllServices()

    fun getServicesBySeller(sellerUsername: String): Flow<List<ServiceItem>> =
        serviceDao.getServicesBySeller(sellerUsername)

    fun searchServices(query: String): Flow<List<ServiceItem>> {
        val formattedQuery = "%$query%"
        return serviceDao.searchServices(formattedQuery)
    }

    suspend fun insertService(service: ServiceItem) {
        serviceDao.insertService(service)
    }

    suspend fun deleteServiceById(id: Int) {
        serviceDao.deleteServiceById(id)
    }

    // User session operations
    suspend fun registerUser(user: User): Boolean {
        // Check if user already exists
        val existing = userDao.getUserByUsername(user.username)
        return if (existing == null) {
            userDao.registerUser(user)
            true
        } else {
            false
        }
    }

    suspend fun loginUser(username: String, passwordHash: String): User? {
        val user = userDao.getUserByUsername(username)
        return if (user != null && user.passwordHash == passwordHash) {
            user
        } else {
            null
        }
    }
}
