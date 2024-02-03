package ru.practicum.android.diploma.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.practicum.android.diploma.presentation.favorite.viewmodel.FavoriteViewModel
import ru.practicum.android.diploma.presentation.settings.IndustriesViewModel
import ru.practicum.android.diploma.presentation.settings.viewmodel.LocalityTypeViewModel
import ru.practicum.android.diploma.presentation.settings.viewmodels.IndustriesViewModel
import ru.practicum.android.diploma.presentation.settings.viewmodels.SettingsViewModel
import ru.practicum.android.diploma.presentation.vacancy.viewmodel.DetailsViewModel
import ru.practicum.android.diploma.presentation.vacancy.viewmodel.SearchViewModel

val viewModelModule = module {
    viewModel {
        SearchViewModel(vacanciesInteractor = get(), settingsInteractor = get())
    }

    viewModel {
        FavoriteViewModel(favoritesInteractor = get())
    }

    viewModel<DetailsViewModel> {
        DetailsViewModel(detailsInteractor = get(), externalNavigator = get(), favoritesInteractor = get())
    }

    viewModel<IndustriesViewModel> {
        IndustriesViewModel(industriesInteractor = get())
    }

    viewModel<LocalityTypeViewModel> {
        LocalityTypeViewModel(placesInteractor = get())
    {

    viewModel<SettingsViewModel> {
        SettingsViewModel(settingsInteractor = get())
    }
}
