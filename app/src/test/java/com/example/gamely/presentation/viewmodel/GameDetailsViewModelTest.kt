package com.example.gamely.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.gamely.domain.model.GameDetails
import com.example.gamely.domain.usecase.GetGameDetailsUseCase
import com.example.gamely.presentation.viewmodel.gamedetails.GameDetailsAction
import com.example.gamely.presentation.viewmodel.gamedetails.GameDetailsViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GameDetailsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    lateinit var viewModel: GameDetailsViewModel
    lateinit var useCase: GetGameDetailsUseCase

    val gameDetails1 = GameDetails(
        id = 1,
        name = "game name ",
        description = "game description",
        released = "2/2/2002",
        rating = 2.6,
        playtime = 8,
        backgroundImage = "kjhgajaldhhwbhnwsb",
        backgroundImageAdditional = null,
        genres = listOf("s", "w")
    )


    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        useCase = mockk(relaxed = true)
        viewModel = GameDetailsViewModel(useCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadGameDetails_returnsCorrectGame() = runTest {
        // given
        coEvery { useCase(1) } returns Result.success(gameDetails1)
        viewModel = GameDetailsViewModel(useCase)


        // when
        viewModel.sendAction(GameDetailsAction.LoadGameDetails(1))
        viewModel.uiState.first { it.gameDetails != null }


        // then
        val state = viewModel.uiState.value
        assertThat(state.gameDetails?.id, `is`(1))
        assertThat(state.gameDetails?.name, `is`("game name "))
        assertThat(state.isLoading, `is`(false))
        assertThat(state.error, nullValue())
    }

    @Test
    fun loadGameDetails_withError_setsErrorState() = runTest {
        // given
        coEvery { useCase(1) } returns Result.failure(Exception("network error"))
        viewModel = GameDetailsViewModel(useCase)

        // when
        viewModel.sendAction(GameDetailsAction.LoadGameDetails(1))
        viewModel.uiState.first { it.error != null }


        // then
        val state = viewModel.uiState.value
        assertThat(state.gameDetails, nullValue())
        assertThat(state.error, `is`("network error"))
        assertThat(state.isLoading, `is`(false))
    }

    @Test
    fun toggleDescription_changesExpandedState() = runTest {
        // given
        viewModel = GameDetailsViewModel(useCase)

        // when
        viewModel.sendAction(GameDetailsAction.ToggleDescription)
        advanceUntilIdle()

        // then
        assertThat(viewModel.uiState.value.isDescriptionExpanded, `is`(true))

        // when
        viewModel.sendAction(GameDetailsAction.ToggleDescription)
        advanceUntilIdle()

        // then
        assertThat(viewModel.uiState.value.isDescriptionExpanded, `is`(false))
    }

}
