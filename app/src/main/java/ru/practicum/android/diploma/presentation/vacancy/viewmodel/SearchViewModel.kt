package ru.practicum.android.diploma.presentation.vacancy.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.data.models.EMPTY_PARAM_NUM
import ru.practicum.android.diploma.data.models.EMPTY_PARAM_SRT
import ru.practicum.android.diploma.data.models.ValuesSearchId
import ru.practicum.android.diploma.domain.api.VacanciesInteractor
import ru.practicum.android.diploma.domain.api.settings.SettingsInteractor
import ru.practicum.android.diploma.domain.models.SearchResultData
import ru.practicum.android.diploma.domain.models.settings.SearchSettings
import ru.practicum.android.diploma.domain.models.vacancy.Vacancies
import ru.practicum.android.diploma.presentation.vacancy.models.PageLoadingState
import ru.practicum.android.diploma.presentation.vacancy.models.ScreenStateVacancies
import ru.practicum.android.diploma.presentation.vacancy.models.SingleLiveEvent
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class SearchViewModel(
    private val vacanciesInteractor: VacanciesInteractor,
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {

    init {
        setSettingsBase()
    }

    private val _screenState: MutableLiveData<ScreenStateVacancies> = MutableLiveData()
    private val _showToastState = SingleLiveEvent<PageLoadingState>()
    private var _isSettingsNotEmpty: MutableLiveData<Boolean> = MutableLiveData()
    val isSettingsNotEmpty: LiveData<Boolean> = _isSettingsNotEmpty
    private var searchJob: Job? = null
    val screenState: LiveData<ScreenStateVacancies> = _screenState
    val toastState: LiveData<PageLoadingState> = _showToastState
    private var currentPage = AtomicInteger(FIRST_PAGE)
    private var isNextPageLoading = AtomicBoolean(false)
    private var currentQuery = EMPTY_QUERY
    private var foundItemsCount = AtomicInteger(ZERO_COUNT)

    fun getVacancies(query: String, pageNum: Int = FIRST_PAGE) {
        if (pageNum != FIRST_PAGE) {
            _screenState.postValue(ScreenStateVacancies.NextPageIsLoading)
        } else {
            _screenState.postValue(ScreenStateVacancies.IsLoading)
        }
        viewModelScope.launch(Dispatchers.IO) {
            vacanciesInteractor.getVacancies(query, pageNum).collect { result ->
                processingResult(result)
            }
        }
    }

    fun setSettingsBase() {
        val settings = settingsInteractor.getSettings()
        settingsInteractor.saveSettings(settings.copy(settingsId = ValuesSearchId.BASE))
    }

    fun checkedSettings() {
        val settings = settingsInteractor.getSettings()
        if (isSettingsNotEmpty(settings)) {
            _isSettingsNotEmpty.postValue(true)
        } else {
            _isSettingsNotEmpty.postValue(false)
        }
    }

    fun newSearch() {
        if (currentQuery != EMPTY_QUERY) {
            val settings = settingsInteractor.getSettings()
            if (settings.settingsId == ValuesSearchId.BASE) {
                viewModelScope.launch {
                    currentPage.set(FIRST_PAGE)
                    getVacancies(currentQuery, currentPage.get())
                }
            } else {
                settingsInteractor.saveSettings(settings.copy(settingsId = ValuesSearchId.BASE))
            }
        }
    }

    fun debounceSearch(query: String) {
        if (query != currentQuery) {
            currentQuery = query
            searchJob?.cancel()
            if (currentQuery.length > ONE_LETTER) {
                searchJob = viewModelScope.launch {
                    delay(SEARCH_DEBOUNCE_DELAY_MILLIS)
                    currentPage.set(FIRST_PAGE)
                    getVacancies(currentQuery)
                }
            }
        }
    }

    private fun processingResult(result: SearchResultData<Vacancies>) {
        when (result) {
            is SearchResultData.NoInternet -> {
                if (currentPage.get() == FIRST_PAGE) {
                    _screenState.postValue(ScreenStateVacancies.NoInternet(result.message))
                } else {
                    _showToastState.postValue(PageLoadingState.InternetError)
                }
            }

            is SearchResultData.ErrorServer -> {
                if (currentPage.get() == FIRST_PAGE) {
                    _screenState.postValue(ScreenStateVacancies.Error(result.message))
                } else {
                    _showToastState.postValue(PageLoadingState.ServerError)
                    _screenState.postValue(ScreenStateVacancies.NextPageLoadingError)
                }
            }

            is SearchResultData.Empty -> {
                _screenState.postValue(ScreenStateVacancies.Empty(result.message))
            }

            is SearchResultData.Data -> {
                if (currentPage.get() == FIRST_PAGE) {
                    _screenState.postValue(
                        ScreenStateVacancies.Content(
                            result.value?.foundItems!!,
                            result.value.listVacancies
                        )
                    )
                    foundItemsCount.set(result.value.foundItems)
                } else {
                    _screenState.postValue(
                        result.value?.let { ScreenStateVacancies.NextPageIsLoaded(it.listVacancies) }
                    )
                }
            }
        }
        isNextPageLoading.set(false)
    }

    fun onLastItemReached() {
        if (currentPage.get() < foundItemsCount.get() / ITEMS_PER_PAGE && !isNextPageLoading.get()) {
            isNextPageLoading.set(true)
            currentPage.set(currentPage.get() + 1)
            getVacancies(currentQuery, currentPage.get())
        }
    }

    fun updateSettingsToBase() {
        val settings = settingsInteractor.getSettings()
        settingsInteractor.saveSettings(settings.copy(settingsId = ValuesSearchId.BASE))
    }

    fun clearCurrentQuery() {
        currentQuery = EMPTY_QUERY
    }

    private fun isSettingsNotEmpty(settings: SearchSettings): Boolean {
        return !(
            !settings.isSalarySpecified
                && settings.salary == EMPTY_PARAM_NUM
                && settings.country.countryId == EMPTY_PARAM_SRT
                && settings.place.areaId == EMPTY_PARAM_SRT
                && settings.industry.industryName == EMPTY_PARAM_SRT
            )
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY_MILLIS = 2000L
        private const val ITEMS_PER_PAGE = 20
        private const val FIRST_PAGE = 0
        private const val EMPTY_QUERY = ""
        private const val ZERO_COUNT = 0
        private const val ONE_LETTER = 1
    }
}
