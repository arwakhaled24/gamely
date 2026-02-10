package com.example.themotherofgames.presentation.viewmodel

import com.example.themotherofgames.domain.model.Game
import com.example.themotherofgames.domain.usecase.GetGamesUseCase
import com.example.themotherofgames.presentation.util.PaginationState
import com.example.themotherofgames.presentation.viewmodel.games.GamesAction
import com.example.themotherofgames.presentation.viewmodel.games.GamesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

/**
 * Unit tests for GamesViewModel
 * Demonstrates testing MVI pattern with pagination
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GamesViewModelTest {

    private lateinit var testDispatcher: StandardTestDispatcher
    private lateinit var getGamesUseCase: GetGamesUseCase
    private lateinit var viewModel: GamesViewModel

    // Sample test data
    private val game1 = Game(id = 1, name = "Game 1", released = "2024-01-01", backgroundImage = "url1", rating = 4.5)
    private val game2 = Game(id = 2, name = "Game 2", released = "2024-01-02", backgroundImage = "url2", rating = 4.8)
    private val game3 = Game(id = 3, name = "Game 3", released = "2024-01-03", backgroundImage = "url3", rating = 4.2)

    @Before
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        
        getGamesUseCase = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        if (::viewModel.isInitialized) {
            viewModel.onCleared()
        }
    }

    @Test
    fun `initial state should have empty games list`() = runTest {
        // Given
        whenever(getGamesUseCase(any())).thenReturn(Result.success(emptyList()))
        
        // When
        viewModel = GamesViewModel(getGamesUseCase)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.games.isEmpty())
    }

    @Test
    fun `LoadGames action should load first page successfully`() = runTest {
        // Given
        val expectedGames = listOf(game1, game2)
        whenever(getGamesUseCase(1)).thenReturn(Result.success(expectedGames))
        
        viewModel = GamesViewModel(getGamesUseCase)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(expectedGames, state.games)
        assertEquals(2, state.currentPage)
        assertEquals(PaginationState.Idle, state.paginationState)
        assertTrue(state.canLoadMore)
    }

    @Test
    fun `LoadMoreGames action should load next page and append items`() = runTest {
        // Given
        val page1Games = listOf(game1, game2)
        val page2Games = listOf(game3)
        
        whenever(getGamesUseCase(1)).thenReturn(Result.success(page1Games))
        whenever(getGamesUseCase(2)).thenReturn(Result.success(page2Games))
        
        viewModel = GamesViewModel(getGamesUseCase)
        advanceUntilIdle()

        // When - Load more games
        viewModel.sendAction(GamesAction.LoadMoreGames)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(3, state.games.size)
        assertEquals(page1Games + page2Games, state.games)
        assertEquals(3, state.currentPage)
    }

    @Test
    fun `LoadGames should show InitialLoading state`() = runTest {
        // Given
        var capturedLoadingState: PaginationState? = null
        whenever(getGamesUseCase(1)).thenAnswer {
            capturedLoadingState = viewModel.uiState.value.paginationState
            Result.success(listOf(game1))
        }
        
        // When
        viewModel = GamesViewModel(getGamesUseCase)
        advanceUntilIdle()

        // Then
        assertTrue(capturedLoadingState is PaginationState.InitialLoading)
    }

    @Test
    fun `LoadMoreGames should show PaginationLoading state`() = runTest {
        // Given
        var capturedLoadingState: PaginationState? = null
        whenever(getGamesUseCase(1)).thenReturn(Result.success(listOf(game1)))
        whenever(getGamesUseCase(2)).thenAnswer {
            capturedLoadingState = viewModel.uiState.value.paginationState
            Result.success(listOf(game2))
        }
        
        viewModel = GamesViewModel(getGamesUseCase)
        advanceUntilIdle()

        // When
        viewModel.sendAction(GamesAction.LoadMoreGames)
        advanceUntilIdle()

        // Then
        assertTrue(capturedLoadingState is PaginationState.PaginationLoading)
    }

    @Test
    fun `error during initial load should update state with error`() = runTest {
        // Given
        val exception = RuntimeException("Network error")
        whenever(getGamesUseCase(1)).thenReturn(Result.failure(exception))
        
        // When
        viewModel = GamesViewModel(getGamesUseCase)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.games.isEmpty())
        assertTrue(state.paginationState is PaginationState.Error)
        assertEquals("Network error", state.error)
        assertTrue(state.canRetry)
    }

    @Test
    fun `error during pagination should keep existing items`() = runTest {
        // Given
        val page1Games = listOf(game1, game2)
        val exception = RuntimeException("Network error")
        
        whenever(getGamesUseCase(1)).thenReturn(Result.success(page1Games))
        whenever(getGamesUseCase(2)).thenReturn(Result.failure(exception))
        
        viewModel = GamesViewModel(getGamesUseCase)
        advanceUntilIdle()

        // When - Load more games (fails)
        viewModel.sendAction(GamesAction.LoadMoreGames)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(page1Games, state.games) // Should keep first page items
        assertTrue(state.paginationState is PaginationState.Error)
        assertEquals("Network error", state.error)
    }

    @Test
    fun `RetryLoading action should retry after error`() = runTest {
        // Given
        val exception = RuntimeException("Network error")
        whenever(getGamesUseCase(1))
            .thenReturn(Result.failure(exception))
            .thenReturn(Result.success(listOf(game1)))
        
        viewModel = GamesViewModel(getGamesUseCase)
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value.paginationState is PaginationState.Error)

        // When - Retry
        viewModel.sendAction(GamesAction.RetryLoading)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(1, state.games.size)
        assertEquals(PaginationState.Idle, state.paginationState)
        assertNull(state.error)
    }

    @Test
    fun `empty response should set EndReached state`() = runTest {
        // Given
        whenever(getGamesUseCase(1)).thenReturn(Result.success(emptyList()))
        
        // When
        viewModel = GamesViewModel(getGamesUseCase)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(PaginationState.EndReached, state.paginationState)
        assertFalse(state.canLoadMore)
    }

    @Test
    fun `LoadGames should reset pagination state`() = runTest {
        // Given
        val page1Games = listOf(game1, game2)
        val page2Games = listOf(game3)
        
        whenever(getGamesUseCase(1)).thenReturn(Result.success(page1Games))
        whenever(getGamesUseCase(2)).thenReturn(Result.success(page2Games))
        
        viewModel = GamesViewModel(getGamesUseCase)
        advanceUntilIdle()
        
        // Load more games
        viewModel.sendAction(GamesAction.LoadMoreGames)
        advanceUntilIdle()
        
        assertEquals(3, viewModel.uiState.value.games.size)

        // When - Load games again (reset)
        viewModel.sendAction(GamesAction.LoadGames)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(2, state.games.size) // Only first page
        assertEquals(2, state.currentPage) // Reset to page 2 (next would be page 2)
    }

    @Test
    fun `SearchGames should filter games by name`() = runTest {
        // Given
        val games = listOf(game1, game2, game3)
        whenever(getGamesUseCase(1)).thenReturn(Result.success(games))
        
        viewModel = GamesViewModel(getGamesUseCase)
        advanceUntilIdle()

        // When - Search for "Game 1"
        viewModel.sendAction(GamesAction.SearchGames("Game 1"))
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(3, state.games.size) // All games still in state
        assertEquals(1, state.filteredGames.size) // But filtered list has 1
        assertEquals(game1, state.filteredGames.first())
        assertEquals("Game 1", state.searchQuery)
    }

    @Test
    fun `SearchGames with empty query should show all games`() = runTest {
        // Given
        val games = listOf(game1, game2, game3)
        whenever(getGamesUseCase(1)).thenReturn(Result.success(games))
        
        viewModel = GamesViewModel(getGamesUseCase)
        advanceUntilIdle()
        
        // Search for something first
        viewModel.sendAction(GamesAction.SearchGames("Game 1"))
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.filteredGames.size)

        // When - Clear search
        viewModel.sendAction(GamesAction.SearchGames(""))
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(3, state.filteredGames.size)
        assertEquals("", state.searchQuery)
    }

    @Test
    fun `isInitialLoading computed property should work correctly`() = runTest {
        // Given
        whenever(getGamesUseCase(1)).thenReturn(Result.success(listOf(game1)))
        
        // When
        viewModel = GamesViewModel(getGamesUseCase)
        
        // Then - Should be loading initially
        assertTrue(viewModel.uiState.value.isInitialLoading)
        
        advanceUntilIdle()
        
        // Then - Should not be loading after completion
        assertFalse(viewModel.uiState.value.isInitialLoading)
    }

    @Test
    fun `isPaginationLoading computed property should work correctly`() = runTest {
        // Given
        whenever(getGamesUseCase(1)).thenReturn(Result.success(listOf(game1)))
        whenever(getGamesUseCase(2)).thenReturn(Result.success(listOf(game2)))
        
        viewModel = GamesViewModel(getGamesUseCase)
        advanceUntilIdle()

        // When - Load more
        viewModel.sendAction(GamesAction.LoadMoreGames)

        // Then - Should be pagination loading
        assertTrue(viewModel.uiState.value.isPaginationLoading)
        
        advanceUntilIdle()
        
        // Then - Should not be loading after completion
        assertFalse(viewModel.uiState.value.isPaginationLoading)
    }

    @Test
    fun `multiple rapid LoadMoreGames actions should not cause duplicate requests`() = runTest {
        // Given
        whenever(getGamesUseCase(1)).thenReturn(Result.success(listOf(game1)))
        whenever(getGamesUseCase(2)).thenReturn(Result.success(listOf(game2)))
        
        viewModel = GamesViewModel(getGamesUseCase)
        advanceUntilIdle()

        // When - Rapid multiple load more actions
        repeat(5) {
            viewModel.sendAction(GamesAction.LoadMoreGames)
        }
        advanceUntilIdle()

        // Then - Should only call page 2 once
        verify(getGamesUseCase, times(1)).invoke(1) // Initial load
        verify(getGamesUseCase, times(1)).invoke(2) // Load more
        verifyNoMoreInteractions(getGamesUseCase)
    }

    @Test
    fun `onCleared should clean up paginator`() = runTest {
        // Given
        whenever(getGamesUseCase(1)).thenReturn(Result.success(listOf(game1)))
        
        viewModel = GamesViewModel(getGamesUseCase)
        advanceUntilIdle()

        // When
        viewModel.onCleared()

        // Then - No crash should occur
        // This test mainly ensures cleanup happens without errors
    }
}
