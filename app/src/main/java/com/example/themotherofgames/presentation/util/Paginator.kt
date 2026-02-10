package com.example.themotherofgames.presentation.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
class Paginator<Game>(
    private val scope: CoroutineScope,
    private val initialKey: Int = 1,
    private val onRequest: suspend (nextKey: Int) -> Result<List<Game>>,
    private val onSuccess: suspend (items: List<Game>, newKey: Int) -> Unit
) {
    private var currentJob: Job? = null
    private var currentKey = initialKey
    private val _state = MutableStateFlow<PaginationState>(PaginationState.Idle)
    val state: StateFlow<PaginationState> = _state.asStateFlow()
    
    fun reset() {
        currentJob?.cancel()
        currentKey = initialKey
        _state.value = PaginationState.Idle
    }
    
    fun loadNextItems() {
        if (_state.value != PaginationState.Idle) return
        
        currentJob = scope.launch {
            try {
                _state.value = if (currentKey == initialKey) {
                    PaginationState.InitialLoading
                } else {
                    PaginationState.PaginationLoading
                }
                
                val result = onRequest(currentKey)
                
                result.fold(
                    onSuccess = { items ->
                        if (items.isEmpty()) {
                            _state.value = PaginationState.EndReached
                        } else {
                            val nextKey = currentKey + 1
                            onSuccess(items, nextKey)
                            currentKey = nextKey
                            _state.value = PaginationState.Idle
                        }
                    },
                    onFailure = { throwable ->
                        _state.value = PaginationState.Error(throwable, true)
                    }
                )
            } catch (e: Exception) {
                _state.value = PaginationState.Error(e, true)
            }
        }
    }
    
    fun retry() {
        if (_state.value is PaginationState.Error) {
            _state.value = PaginationState.Idle
            loadNextItems()
        }
    }
}
