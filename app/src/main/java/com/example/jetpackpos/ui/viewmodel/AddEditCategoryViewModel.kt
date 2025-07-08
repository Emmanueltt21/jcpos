package com.example.jetpackpos.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackpos.data.model.Category
import com.example.jetpackpos.data.repository.CategoryRepository
import com.example.jetpackpos.ui.navigation.AddEditCategory // For ARG_CATEGORY_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryFormState(
    val name: String = "",
    val nameError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val currentCategoryId: Long? = null
)

sealed class AddEditCategoryEvent {
    object CategorySaved : AddEditCategoryEvent()
    data class Error(val message: String) : AddEditCategoryEvent()
}

@HiltViewModel
class AddEditCategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _formState = MutableStateFlow(CategoryFormState())
    val formState: StateFlow<CategoryFormState> = _formState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AddEditCategoryEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val currentCategoryId: Long? = savedStateHandle.get<String>(AddEditCategory.ARG_CATEGORY_ID)?.toLongOrNull()

    init {
        if (currentCategoryId != null) {
            _formState.value = _formState.value.copy(isLoading = true, isEditing = true, currentCategoryId = currentCategoryId)
            loadCategory(currentCategoryId)
        }
    }

    fun onNameChange(name: String) {
        _formState.update { it.copy(name = name, nameError = null, generalError = null) }
    }

    private fun loadCategory(categoryId: Long) {
        viewModelScope.launch {
            categoryRepository.getCategoryById(categoryId).firstOrNull()?.let { category ->
                _formState.value = _formState.value.copy(
                    name = category.name,
                    isLoading = false
                )
            } ?: run {
                _formState.value = _formState.value.copy(isLoading = false, generalError = "Failed to load category.")
                _eventFlow.emit(AddEditCategoryEvent.Error("Category not found."))
            }
        }
    }

    fun saveCategory() {
        val currentSate = _formState.value
        if (currentSate.name.isBlank()) {
            _formState.update { it.copy(nameError = "Category name cannot be empty.", generalError = "Name is required.") }
            return
        }

        _formState.update { it.copy(isLoading = true, generalError = null) }
        viewModelScope.launch {
            try {
                if (categoryRepository.isCategoryNameExisting(currentSate.name.trim(), currentSate.currentCategoryId)) {
                    _formState.update { it.copy(isLoading = false, nameError = "Category name already exists.", generalError = "Name already exists.") }
                    return@launch
                }

                val category = Category(
                    id = currentSate.currentCategoryId ?: 0,
                    name = currentSate.name.trim()
                )

                if (currentSate.isEditing) {
                    categoryRepository.updateCategory(category)
                } else {
                    categoryRepository.insertCategory(category)
                }
                _eventFlow.emit(AddEditCategoryEvent.CategorySaved)
            } catch (e: Exception) {
                 // Specific error for unique constraint violation on insert (ABORT strategy)
                if (e is android.database.sqlite.SQLiteConstraintException && e.message?.contains("UNIQUE constraint failed: categories.name") == true) {
                     _formState.update { it.copy(isLoading = false, nameError = "Category name already exists (database constraint).", generalError = "Name already exists.") }
                     _eventFlow.emit(AddEditCategoryEvent.Error("Category name already exists."))
                } else {
                    _formState.update { it.copy(isLoading = false, generalError = "Error saving category: ${e.message}") }
                    _eventFlow.emit(AddEditCategoryEvent.Error("Failed to save category: ${e.message}"))
                }
            }
        }
    }
}
