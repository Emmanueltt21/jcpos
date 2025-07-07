package com.example.jetpackpos.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.jetpackpos.data.model.Order
import com.example.jetpackpos.data.model.OrderItem
import com.example.jetpackpos.data.model.Product

@Database(
    entities = [Product::class, Order::class, OrderItem::class],
    version = 1, // Initial version
    exportSchema = false // For simplicity in this project, set to false. For production, consider exporting.
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jetpack_pos_database"
                )
                // Add migrations here if needed in the future
                .fallbackToDestructiveMigration() // Simple strategy for now; for production, use proper migrations.
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
