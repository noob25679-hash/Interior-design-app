package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ServiceOffer::class,
        Booking::class,
        Faq::class,
        BlogPost::class,
        PortfolioItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceOfferDao(): ServiceOfferDao
    abstract fun bookingDao(): BookingDao
    abstract fun faqDao(): FaqDao
    abstract fun blogPostDao(): BlogPostDao
    abstract fun portfolioItemDao(): PortfolioItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "studio_aeterna_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
