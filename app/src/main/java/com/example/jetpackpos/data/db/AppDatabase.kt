package com.example.jetpackpos.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.jetpackpos.data.model.Category
import com.example.jetpackpos.data.model.Customer
import com.example.jetpackpos.data.model.Order
import com.example.jetpackpos.data.model.OrderItem
import com.example.jetpackpos.data.model.Product
import com.example.jetpackpos.data.model.ShopInfo

@Database(
    entities = [
        Product::class,
        Order::class,
        OrderItem::class,
        Customer::class,
        Category::class,
        ShopInfo::class
    ],
    version = 2, // Incremented version
    exportSchema = false // For simplicity, set to false. For production, consider exporting and providing migrations.
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun customerDao(): CustomerDao
    abstract fun categoryDao(): CategoryDao
    abstract fun shopInfoDao(): ShopInfoDao

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
                // IMPORTANT: For production apps, provide proper Room migrations.
                // .addMigrations(MIGRATION_1_2, MIGRATION_2_3, ...)
                .fallbackToDestructiveMigration() // OK for dev, but causes data loss on schema change for users.
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
