package com.example.jetpackpos.data.repository

import com.example.jetpackpos.data.db.CategoryDao
import com.example.jetpackpos.data.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(private val categoryDao: CategoryDao) {

    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    fun getCategoryById(categoryId: Long): Flow<Category?> = categoryDao.getCategoryById(categoryId)

    suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    suspend fun isCategoryNameExisting(name: String, currentCategoryId: Long? = null): Boolean {
        if (name.isBlank()) return true // Prevent blank names from being considered "not existing"
        val category = categoryDao.getCategoryByName(name).firstOrNull()
        return category != null && (currentCategoryId == null || category.id != currentCategoryId)
    }

    suspend fun clearAllCategories() {
        categoryDao.clearAllCategories()
    }
}
