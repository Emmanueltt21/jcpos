package com.example.jetpackpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackpos.data.model.Category
import com.example.jetpackpos.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CategoryListUiState {
    object Loading : CategoryListUiState
    data class Success(val categories: List<Category>) : CategoryListUiState
    data class Error(val message: String) : CategoryListUiState
}

@HiltViewModel
class CategoryListViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CategoryListUiState>(CategoryListUiState.Loading)
    val uiState: StateFlow<CategoryListUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<String>() // For delete messages
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadCategories()
    }

    fun loadCategories() { // Public for refresh
        viewModelScope.launch {
            _uiState.value = CategoryListUiState.Loading
            categoryRepository.getAllCategories()
                .map<List<Category>, CategoryListUiState> { CategoryListUiState.Success(it) }
                .catch { e -> _uiState.value = CategoryListUiState.Error(e.message ?: "Failed to load categories") }
                .collect { _uiState.value = it }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                categoryRepository.deleteCategory(category)
                _eventFlow.emit("Category '${category.name}' deleted.")
                // No need to manually refresh list, Flow should update
            } catch (e: Exception) {
                _eventFlow.emit("Error deleting category: ${e.message}")
            }
        }
    }
}
