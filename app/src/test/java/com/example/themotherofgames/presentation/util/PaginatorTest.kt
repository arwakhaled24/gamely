package com.example.themotherofgames.presentation.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for Paginator class
 * Demonstrates testing best practices for coroutine-based pagination
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PaginatorTest {

    private lateinit var testScope: TestScope
    private lateinit var testDispatcher: StandardTestDispatcher
    private lateinit var successfulItems: MutableList<List<String>>
    private var requestCallCount = 0

    @Before
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        testScope = TestScope(testDispatcher)
        successfulItems = mutableListOf()
        requestCallCount = 0
    }

    @After
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun `initial state should be Idle`() = runTest {
        // Given
        val paginator = createPaginator()

        // Then
        assertEquals(PaginationState.Idle, paginator.state.value)
        assertEquals(1, paginator.getCurrentKey())
    }

    @Test
    fun `loadNextItems should transition from Idle to InitialLoading to Idle on success`() = runTest {
        // Given
        val states = mutableListOf<PaginationState>()
        val paginator = createPaginator(
            onRequest = { page ->
                Result.success(listOf("Item $page"))
            }
        )

        // Collect states
        val job = launch {
            paginator.state.collect { states.add(it) }
        }

        // When
        paginator.loadNextItems()
        advanceUntilIdle()

        // Then
        assertEquals(3, states.size)
        assertTrue(states[0] is PaginationState.Idle)
        assertTrue(states[1] is PaginationState.InitialLoading)
        assertTrue(states[2] is PaginationState.Idle)

        job.cancel()
    }

    @Test
    fun `loadNextItems should call onSuccess with correct items and next key`() = runTest {
        // Given
        val expectedItems = listOf("Item1", "Item2", "Item3")
        var capturedItems: List<String>? = null
        var capturedNextKey: Int? = null

        val paginator = createPaginator(
            onRequest = { Result.success(expectedItems) },
            onSuccess = { items, nextKey ->
                capturedItems = items
                capturedNextKey = nextKey
            }
        )

        // When
        paginator.loadNextItems()
        advanceUntilIdle()

        // Then
        assertEquals(expectedItems, capturedItems)
        assertEquals(2, capturedNextKey) // initialKey (1) + incrementBy (1)
        assertEquals(2, paginator.getCurrentKey())
    }

    @Test
    fun `loadNextItems should update state to Error on failure`() = runTest {
        // Given
        val exception = RuntimeException("Network error")
        val paginator = createPaginator(
            onRequest = { Result.failure(exception) }
        )

        // When
        paginator.loadNextItems()
        advanceUntilIdle()

        // Then
        val state = paginator.state.value
        assertTrue(state is PaginationState.Error)
        assertEquals(exception, (state as PaginationState.Error).throwable)
        assertTrue(state.canRetry)
    }

    @Test
    fun `loadNextItems should transition to EndReached when empty list returned`() = runTest {
        // Given
        val paginator = createPaginator(
            onRequest = { Result.success(emptyList()) }
        )

        // When
        paginator.loadNextItems()
        advanceUntilIdle()

        // Then
        assertEquals(PaginationState.EndReached, paginator.state.value)
    }

    @Test
    fun `loadNextItems should use PaginationLoading for subsequent pages`() = runTest {
        // Given
        val states = mutableListOf<PaginationState>()
        val paginator = createPaginator(
            onRequest = { page ->
                Result.success(listOf("Item $page"))
            }
        )

        val job = launch {
            paginator.state.collect { states.add(it) }
        }

        // When - Load first page
        paginator.loadNextItems()
        advanceUntilIdle()
        states.clear() // Clear states from first load

        // Load second page
        paginator.loadNextItems()
        advanceUntilIdle()

        // Then
        assertTrue(states[0] is PaginationState.PaginationLoading)
        assertTrue(states[1] is PaginationState.Idle)

        job.cancel()
    }

    @Test
    fun `loadNextItems should prevent duplicate requests when already loading`() = runTest {
        // Given
        val paginator = createPaginator(
            onRequest = { page ->
                requestCallCount++
                Result.success(listOf("Item $page"))
            }
        )

        // When - Rapid multiple calls
        paginator.loadNextItems()
        paginator.loadNextItems()
        paginator.loadNextItems()
        advanceUntilIdle()

        // Then - Only one request should be made
        assertEquals(1, requestCallCount)
    }

    @Test
    fun `loadNextItems should not load when state is Error`() = runTest {
        // Given
        val paginator = createPaginator(
            onRequest = { page ->
                if (requestCallCount == 0) {
                    requestCallCount++
                    Result.failure(RuntimeException("Error"))
                } else {
                    requestCallCount++
                    Result.success(listOf("Item $page"))
                }
            }
        )

        // When - First load fails
        paginator.loadNextItems()
        advanceUntilIdle()
        
        // Try to load again without retry
        paginator.loadNextItems()
        advanceUntilIdle()

        // Then - Should only have made one request
        assertEquals(1, requestCallCount)
        assertTrue(paginator.state.value is PaginationState.Error)
    }

    @Test
    fun `loadNextItems should not load when state is EndReached`() = runTest {
        // Given
        val paginator = createPaginator(
            onRequest = { 
                requestCallCount++
                Result.success(emptyList()) 
            }
        )

        // When
        paginator.loadNextItems()
        advanceUntilIdle()
        
        paginator.loadNextItems()
        advanceUntilIdle()

        // Then
        assertEquals(1, requestCallCount)
        assertEquals(PaginationState.EndReached, paginator.state.value)
    }

    @Test
    fun `reset should clear state and reset key`() = runTest {
        // Given
        val paginator = createPaginator(
            onRequest = { Result.success(listOf("Item")) }
        )

        // Load some pages
        paginator.loadNextItems()
        advanceUntilIdle()
        paginator.loadNextItems()
        advanceUntilIdle()

        // When
        paginator.reset()

        // Then
        assertEquals(PaginationState.Idle, paginator.state.value)
        assertEquals(1, paginator.getCurrentKey())
    }

    @Test
    fun `retry should work after error`() = runTest {
        // Given
        var shouldFail = true
        val paginator = createPaginator(
            onRequest = {
                if (shouldFail) {
                    shouldFail = false
                    Result.failure(RuntimeException("Error"))
                } else {
                    Result.success(listOf("Item"))
                }
            }
        )

        // When - First attempt fails
        paginator.loadNextItems()
        advanceUntilIdle()
        assertTrue(paginator.state.value is PaginationState.Error)

        // Retry
        paginator.retry()
        advanceUntilIdle()

        // Then
        assertEquals(PaginationState.Idle, paginator.state.value)
    }

    @Test
    fun `retry should not work when state is not Error`() = runTest {
        // Given
        val paginator = createPaginator(
            onRequest = { 
                requestCallCount++
                Result.success(listOf("Item")) 
            }
        )

        // When
        paginator.retry()
        advanceUntilIdle()

        // Then - No request should be made
        assertEquals(0, requestCallCount)
        assertEquals(PaginationState.Idle, paginator.state.value)
    }

    @Test
    fun `isLoading should return true during loading`() = runTest {
        // Given
        val paginator = createPaginator(
            onRequest = { Result.success(listOf("Item")) }
        )

        // When
        assertFalse(paginator.isLoading())
        
        paginator.loadNextItems()
        assertTrue(paginator.isLoading())
        
        advanceUntilIdle()
        assertFalse(paginator.isLoading())
    }

    @Test
    fun `pagination with custom initialKey and incrementBy`() = runTest {
        // Given
        val paginator = Paginator(
            scope = testScope,
            initialKey = 10,
            incrementBy = 5,
            onRequest = { page -> Result.success(listOf("Item $page")) },
            onSuccess = { _, _ -> }
        )

        // When
        assertEquals(10, paginator.getCurrentKey())
        
        paginator.loadNextItems()
        advanceUntilIdle()
        
        // Then
        assertEquals(15, paginator.getCurrentKey()) // 10 + 5
    }

    @Test
    fun `multiple successful pages should increment key correctly`() = runTest {
        // Given
        val paginator = createPaginator(
            onRequest = { page -> Result.success(listOf("Item $page")) }
        )

        // When - Load 3 pages
        paginator.loadNextItems() // Page 1
        advanceUntilIdle()
        assertEquals(2, paginator.getCurrentKey())

        paginator.loadNextItems() // Page 2
        advanceUntilIdle()
        assertEquals(3, paginator.getCurrentKey())

        paginator.loadNextItems() // Page 3
        advanceUntilIdle()

        // Then
        assertEquals(4, paginator.getCurrentKey())
        assertEquals(PaginationState.Idle, paginator.state.value)
    }

    @Test
    fun `concurrent loadNextItems calls should be thread-safe`() = runTest {
        // Given
        val paginator = createPaginator(
            onRequest = { page ->
                requestCallCount++
                Result.success(listOf("Item $page"))
            }
        )

        // When - Multiple concurrent calls
        repeat(10) {
            launch { paginator.loadNextItems() }
        }
        advanceUntilIdle()

        // Then - Only one request should succeed due to mutex
        assertEquals(1, requestCallCount)
    }

    /**
     * Helper function to create a Paginator with default test configuration
     */
    private fun createPaginator(
        initialKey: Int = 1,
        incrementBy: Int = 1,
        onRequest: suspend (Int) -> Result<List<String>> = { Result.success(emptyList()) },
        onSuccess: suspend (List<String>, Int) -> Unit = { items, nextKey ->
            successfulItems.add(items)
        }
    ): Paginator<String> {
        return Paginator(
            scope = testScope,
            initialKey = initialKey,
            incrementBy = incrementBy,
            onRequest = onRequest,
            onSuccess = onSuccess
        )
    }
}
