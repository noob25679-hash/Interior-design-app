package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_offers")
data class ServiceOffer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val scope: String,
    val price: Double,
    val priceLabel: String = "flat fee",
    val imageUrl: String
)

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientName: String,
    val clientEmail: String,
    val clientPhone: String,
    val projectDetails: String,
    val bookingDate: String,
    val bookingTime: String,
    val status: String, // "Pending Payment", "Confirmed (Dummy Paid)", "Completed"
    val amountPaid: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "faqs")
data class Faq(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,
    val answer: String
)

@Entity(tableName = "blog_posts")
data class BlogPost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val excerpt: String,
    val content: String,
    val dateString: String,
    val imageUrl: String
)

@Entity(tableName = "portfolio_items")
data class PortfolioItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // e.g., "Living Room", "Kitchen", "Commercial"
    val imageUrl: String,
    val description: String
)
