package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "services")
data class ServiceItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String,
    val description: String,
    val location: String,
    val priceRate: String, // e.g. "₹500/hour" or "₹1200 fixed"
    val sellerFullName: String,
    val sellerUsername: String,
    val contactPhone: String,
    val createdAt: Long = System.currentTimeMillis()
)
