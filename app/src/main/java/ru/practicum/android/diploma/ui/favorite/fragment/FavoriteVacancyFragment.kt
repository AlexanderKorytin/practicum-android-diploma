package ru.practicum.android.diploma.ui.favorite.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentFavoriteVacancyBinding
import ru.practicum.android.diploma.domain.models.vacancy.VacancyItem
import ru.practicum.android.diploma.presentation.favorite.model.FavoriteScreenState
import ru.practicum.android.diploma.presentation.favorite.viewmodel.FavoriteViewModel
import ru.practicum.android.diploma.presentation.vacancy.VacancyAdapter
import ru.practicum.android.diploma.util.BindingFragment
import ru.practicum.android.diploma.util.VACANCY_ID
import ru.practicum.android.diploma.util.debounce

class FavoriteVacancyFragment : BindingFragment<FragmentFavoriteVacancyBinding>() {
    private val viewModel by viewModel<FavoriteViewModel>()

    private val vacancyAdapter = VacancyAdapter { vacancyItem ->
        vacancyClickDebounce?.let { vacancyClickDebounce -> vacancyClickDebounce(vacancyItem) }
    }
    private var vacancyClickDebounce: ((VacancyItem) -> Unit)? = null

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentFavoriteVacancyBinding {
        return FragmentFavoriteVacancyBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvSearchResult.adapter = vacancyAdapter
        viewModel.fillData()
        viewModel.observeState().observe(viewLifecycleOwner) {
            render(it)
        }

        setOnVacancyClickListener()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fillData()
    }

    private fun setOnVacancyClickListener() {
        vacancyClickDebounce = debounce(
            CLICK_DEBOUNCE_DELAY_MILLIS,
            viewLifecycleOwner.lifecycleScope,
            false
        ) { vacancyItem ->
            val vacancyBundle = bundleOf(VACANCY_ID to vacancyItem.id)
            findNavController().navigate(R.id.action_favoriteVacancyFragment_to_vacancyDetailsFragment, vacancyBundle)
        }
    }

    private fun render(state: FavoriteScreenState) {
        when (state) {
            is FavoriteScreenState.Content -> showContent(state.vacancies)
            is FavoriteScreenState.Empty -> showEmpty(state.messageRes.toString())
            is FavoriteScreenState.Error -> shorError(state.messageRes.toString())
        }
    }

    private fun showContent(vacancies: List<VacancyItem>) {
        binding.rvSearchResult.isVisible = true
        binding.llErrorPlaceholder.isVisible = false
        vacancyAdapter.clearData()
        vacancyAdapter.addVacancies(vacancies)
        vacancyAdapter.notifyDataSetChanged()
    }

    private fun showEmpty(messageRes: String) {
        with(binding) {
            rvSearchResult.isVisible = false
            llErrorPlaceholder.isVisible = true
            tvErrorPlaceholder.text = getString(R.string.favorite_empty_list_error_text)
            ivPicPlaceholder.setImageResource(R.drawable.ic_empty_list_favorite_pic)
        }
    }

    private fun shorError(messageRes: String) {
        with(binding) {
            rvSearchResult.isVisible = false
            llErrorPlaceholder.isVisible = true
            tvErrorPlaceholder.text = getString(R.string.favorite_db_error_text)
            ivPicPlaceholder.setImageResource(R.drawable.ic_error_favorite_list_pic)
        }
    }

    companion object {
        private const val CLICK_DEBOUNCE_DELAY_MILLIS = 200L
    }
}
