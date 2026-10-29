package com.kamal.creditliability.presentation.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamal.creditliability.data.local.AppDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DeleteDataState(
    val isDeleting: Boolean = false,
    val completed: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DeleteAllDataViewModel @Inject constructor(
    private val database: AppDatabase
) : ViewModel() {
    private val _state = MutableStateFlow(DeleteDataState())
    val state = _state.asStateFlow()

    fun deleteAll() {
        if (_state.value.isDeleting) return
        viewModelScope.launch {
            _state.value = DeleteDataState(isDeleting = true)
            val result = runCatching {
                withContext(Dispatchers.IO) { database.clearAllTables() }
            }
            _state.value = if (result.isSuccess) {
                DeleteDataState(completed = true)
            } else {
                DeleteDataState(error = "Could not delete local data. Please retry.")
            }
        }
    }
}