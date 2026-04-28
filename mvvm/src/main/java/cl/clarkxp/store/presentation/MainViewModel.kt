package cl.clarkxp.store.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.clarkxp.store.core.utils.Resource
import cl.clarkxp.store.domain.usecase.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories

    init {
        loadCategories()
    }

    private fun loadCategories() {
        getCategoriesUseCase().onEach { result ->
            if (result is Resource.Success) {
                _categories.value = result.data ?: emptyList()
            }
        }.launchIn(viewModelScope)
    }
}