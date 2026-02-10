package com.example.themotherofgames.presentation.util

sealed interface PaginationState {
    data object Idle : PaginationState
    data object InitialLoading : PaginationState
    data object PaginationLoading : PaginationState
    data class Error(val throwable: Throwable, val canRetry: Boolean = true) : PaginationState
    data object EndReached : PaginationState
}
