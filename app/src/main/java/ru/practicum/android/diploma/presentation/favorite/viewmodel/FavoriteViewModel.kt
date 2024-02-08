package ru.practicum.android.diploma.presentation.favorite.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.api.DetailsInteractor
import ru.practicum.android.diploma.domain.api.FavoritesInteractor
import ru.practicum.android.diploma.domain.models.SearchResultData
import ru.practicum.android.diploma.domain.models.vacancy.VacancyItem
import ru.practicum.android.diploma.presentation.favorite.model.FavoriteScreenState

class FavoriteViewModel(
    private val favoritesInteractor: FavoritesInteractor,
    private val detailsInteractor: DetailsInteractor
) : ViewModel() {
    private val _favoriteStateLiveData = MutableLiveData<FavoriteScreenState>()

    fun observeState(): LiveData<FavoriteScreenState> = _favoriteStateLiveData

    fun fillData() {
        viewModelScope.launch {
            favoritesInteractor
                .getFavoriteVacancies()
                .collect { vacancies ->
                    processResult(vacancies)
                }
        }
    }

    private fun processResult(vacancies: List<VacancyItem>) {
        if (vacancies.isNullOrEmpty()) {
            renderState(FavoriteScreenState.Empty(R.string.favorite_empty_list_error_text))
        } else if (vacancies.isNotEmpty()) {
            renderState(FavoriteScreenState.Content(vacancies))
        } else {
            renderState(FavoriteScreenState.Error(R.string.favorite_db_error_text))
        }
    }

    fun deleteVacancyFromFavorite(vacancy: VacancyItem) {
        viewModelScope.launch {
            val vacancyDetails = detailsInteractor.getVacancyDetails(vacancy.id).collect {
                when (it) {
                    is SearchResultData.Data -> {
                        if (it.value != null) {
                            favoritesInteractor.deleteVacancyFromFavorite(vacancyDetails = it.value)
                            fillData()
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private fun renderState(state: FavoriteScreenState) {
        _favoriteStateLiveData.postValue(state)
    }
}
