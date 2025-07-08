package com.example.jetpackpos.di

import android.content.Context
import com.example.jetpackpos.data.db.* // Import all DAOs
import com.example.jetpackpos.data.repository.CategoryRepository
import com.example.jetpackpos.data.repository.CustomerRepository
import com.example.jetpackpos.data.repository.ShopInfoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Provides instances at the application level
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton // DAOs are typically singletons tied to the database instance
    fun provideProductDao(appDatabase: AppDatabase): ProductDao {
        return appDatabase.productDao()
    }

    @Provides
    @Singleton
    fun provideOrderDao(appDatabase: AppDatabase): OrderDao {
        return appDatabase.orderDao()
    }

    @Provides
    @Singleton
    fun provideCustomerDao(appDatabase: AppDatabase): CustomerDao {
        return appDatabase.customerDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(appDatabase: AppDatabase): CategoryDao {
        return appDatabase.categoryDao()
    }

    @Provides
    @Singleton
    fun provideShopInfoDao(appDatabase: AppDatabase): ShopInfoDao {
        return appDatabase.shopInfoDao()
    }

    // Repositories are already provided as @Singleton in their own classes
    // if they are constructor injected with @Inject and @Singleton annotations.
    // So, explicit provides methods for repositories are not strictly needed here
    // unless we want to provide an interface implementation or do some other configuration.
    // For this project, constructor injection with @Singleton on the repository class is sufficient.
    // Example:
    // @Provides
    // @Singleton
    // fun provideCustomerRepository(customerDao: CustomerDao): CustomerRepository {
    //     return CustomerRepository(customerDao)
    // }
    // But since CustomerRepository is annotated with @Singleton and @Inject constructor,
    // Hilt can provide it directly.
}
