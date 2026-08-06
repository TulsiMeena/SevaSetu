package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val username: String, // Treat username/email as unique key
    val passwordHash: String,
    val fullName: String,
    val phoneNumber: String,
    val createdAt: Long = System.currentTimeMillis()
)
