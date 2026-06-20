package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceOfferDao {
    @Query("SELECT * FROM service_offers ORDER BY id ASC")
    fun getAllOffersFlow(): Flow<List<ServiceOffer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffer(offer: ServiceOffer)

    @Update
    suspend fun updateOffer(offer: ServiceOffer)

    @Delete
    suspend fun deleteOffer(offer: ServiceOffer)
}

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY timestamp DESC")
    fun getAllBookingsFlow(): Flow<List<Booking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking): Long

    @Delete
    suspend fun deleteBooking(booking: Booking)
}

@Dao
interface FaqDao {
    @Query("SELECT * FROM faqs ORDER BY id ASC")
    fun getAllFaqsFlow(): Flow<List<Faq>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaq(faq: Faq)

    @Update
    suspend fun updateFaq(faq: Faq)

    @Delete
    suspend fun deleteFaq(faq: Faq)
}

@Dao
interface BlogPostDao {
    @Query("SELECT * FROM blog_posts ORDER BY id DESC")
    fun getAllBlogPostsFlow(): Flow<List<BlogPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlogPost(post: BlogPost)

    @Update
    suspend fun updateBlogPost(post: BlogPost)

    @Delete
    suspend fun deleteBlogPost(post: BlogPost)
}

@Dao
interface PortfolioItemDao {
    @Query("SELECT * FROM portfolio_items ORDER BY id DESC")
    fun getAllPortfolioItemsFlow(): Flow<List<PortfolioItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortfolioItem(item: PortfolioItem)

    @Update
    suspend fun updatePortfolioItem(item: PortfolioItem)

    @Delete
    suspend fun deletePortfolioItem(item: PortfolioItem)
}
