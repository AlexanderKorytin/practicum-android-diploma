package ru.practicum.android.diploma.ui.search.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentSearchBinding
import ru.practicum.android.diploma.domain.models.vacancy.VacancyItem
import ru.practicum.android.diploma.presentation.vacancy.VacancyAdapter
import ru.practicum.android.diploma.presentation.vacancy.models.PageLoadingState
import ru.practicum.android.diploma.presentation.vacancy.models.ScreenStateVacancies
import ru.practicum.android.diploma.presentation.vacancy.viewmodel.SearchViewModel
import ru.practicum.android.diploma.util.BindingFragment
import ru.practicum.android.diploma.util.VACANCY_ID
import ru.practicum.android.diploma.util.debounce

class SearchFragment : BindingFragment<FragmentSearchBinding>() {

    private val viewModel by viewModel<SearchViewModel>()
    private val vacancyAdapter = VacancyAdapter { vacancyItem ->
        vacancyClickDebounce?.let { vacancyClickDebounce -> vacancyClickDebounce(vacancyItem) }
    }
    private var vacancyClickDebounce: ((VacancyItem) -> Unit)? = null
    private var currentItemsFound = ZERO_ITEMS

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSearchBinding {
        return FragmentSearchBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind()
        setOnVacancyClickListener()
        setOnScrollListener()
        newSearch()
        viewModel.checkedSettings()
        binding.btnFilter.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_filterFragment)
        }
        viewModel.screenState.observe(viewLifecycleOwner) {
            render(it)
        }
        viewModel.toastState.observe(viewLifecycleOwner) {
            errorWhilePageLoadingNotification(it)
        }
        viewModel.isSettingsNotEmpty.observe(viewLifecycleOwner) {
            binding.btnFilter.setImageResource(if (it) R.drawable.ic_filter_on else R.drawable.ic_filter_off)
        }
    }

    private fun errorWhilePageLoadingNotification(state: PageLoadingState) {
        vacancyAdapter.removeLoading()
        Snackbar.make(
            binding.root,
            if (state is PageLoadingState.ServerError) {
                getString(
                    R.string.error_while_loading_page
                )
            } else {
                getString(R.string.no_internet_while_loading_page)
            },
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun bind() {
        with(binding) {
            etSearch.doAfterTextChanged { text ->
                viewModel.updateSettingsToBase()
                if (text.isNullOrEmpty()) {
                    btnClear.setImageResource(R.drawable.ic_search)
                } else {
                    btnClear.setImageResource(R.drawable.ic_close)
                    viewModel.debounceSearch(text.toString())
                }
            }

            btnClear.setOnClickListener {
                etSearch.text.clear()
                viewModel.clearCurrentQuery()
            }

            rvSearchResult.adapter = vacancyAdapter
        }
    }

    private fun newSearch() {
        viewModel.newSearch()
    }

    private fun setOnVacancyClickListener() {
        vacancyClickDebounce = debounce(
            CLICK_DEBOUNCE_DELAY_MILLIS,
            viewLifecycleOwner.lifecycleScope,
            false
        ) { vacancyItem ->
            viewModel.setSettingsBase()
            val vacancyBundle = bundleOf(VACANCY_ID to vacancyItem.id)
            findNavController().navigate(R.id.action_searchFragment_to_vacancyDetailsFragment, vacancyBundle)
        }
    }

    private fun render(state: ScreenStateVacancies) {
        when (state) {
            is ScreenStateVacancies.Content -> { showContent(state.listVacancies, state.foundItems) }

            is ScreenStateVacancies.Empty -> { showEmptyState(state.message) }

            is ScreenStateVacancies.Error -> { showServerErrorState(state.message) }

            is ScreenStateVacancies.IsLoading -> { showLoadingState() }

            is ScreenStateVacancies.NoInternet -> { showNoInternetState(state.message) }

            is ScreenStateVacancies.NextPageIsLoading -> { showNextPageLoading() }

            is ScreenStateVacancies.NextPageIsLoaded -> { showNextPageLoaded(state.listVacancies) }

            ScreenStateVacancies.NextPageLoadingError -> { showNextPageLoadingError() }
        }
    }

    private fun showNextPageLoadingError() {
        with(binding) {
            rvSearchResult.isVisible = true
            tvResultCountChips.isVisible = true
            tvErrorPlaceholder.isVisible = false
            ivPicPlaceholder.isVisible = false
            pbCircle.isVisible = false
        }
    }

    private fun showContent(vacanciesList: List<VacancyItem>, itemsFound: Int) {
        with(binding) {
            rvSearchResult.isVisible = true
            tvResultCountChips.isVisible = true
            tvErrorPlaceholder.isVisible = false
            ivPicPlaceholder.isVisible = false
            pbCircle.isVisible = false
            tvResultCountChips.text = resources.getQuantityString(
                R.plurals.items_found_number,
                itemsFound,
                itemsFound
            )
            vacancyAdapter.clearData()
            vacancyAdapter.addVacancies(vacanciesList)
            currentItemsFound = tvResultCountChips.text.toString()
        }
    }

    private fun showEmptyState(message: Int) {
        with(binding) {
            rvSearchResult.isVisible = false
            tvResultCountChips.isVisible = false
            tvErrorPlaceholder.isVisible = true
            ivPicPlaceholder.isVisible = true
            pbCircle.isVisible = false
            ivPicPlaceholder.setImageResource(R.drawable.ic_nothing_found_pic)
            tvErrorPlaceholder.text = getString(message)
        }
    }

    private fun showServerErrorState(message: Int) {
        with(binding) {
            rvSearchResult.isVisible = false
            tvResultCountChips.isVisible = false
            tvErrorPlaceholder.isVisible = true
            ivPicPlaceholder.isVisible = true
            pbCircle.isVisible = false
            ivPicPlaceholder.setImageResource(R.drawable.ic_nothing_found_pic)
            tvErrorPlaceholder.text = getString(message)
        }
    }

    private fun showLoadingState() {
        with(binding) {
            rvSearchResult.isVisible = false
            tvResultCountChips.isVisible = false
            tvErrorPlaceholder.isVisible = false
            ivPicPlaceholder.isVisible = false
            pbCircle.isVisible = true
        }
    }

    private fun showNoInternetState(message: Int) {
        with(binding) {
            rvSearchResult.isVisible = false
            tvResultCountChips.isVisible = false
            tvErrorPlaceholder.isVisible = true
            ivPicPlaceholder.isVisible = true
            pbCircle.isVisible = false
            ivPicPlaceholder.setImageResource(R.drawable.ic_no_internet_pic)
            tvErrorPlaceholder.text = getString(message)
        }
    }

    private fun showNextPageLoading() {
        with(binding) {
            rvSearchResult.isVisible = true
            tvResultCountChips.isVisible = true
            tvErrorPlaceholder.isVisible = false
            ivPicPlaceholder.isVisible = false
            pbCircle.isVisible = false
        }
        vacancyAdapter.addLoading()
    }

    private fun showNextPageLoaded(listVacancies: List<VacancyItem>) {
        vacancyAdapter.removeLoading()
        with(binding) {
            rvSearchResult.isVisible = true
            tvResultCountChips.isVisible = true
            tvErrorPlaceholder.isVisible = false
            ivPicPlaceholder.isVisible = false
            pbCircle.isVisible = false
        }
        vacancyAdapter.addVacancies(listVacancies)
    }

    private fun setOnScrollListener() {
        binding.rvSearchResult.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // если dy > 0 список крутится вниз и наоборот
                if (dy > 0) {
                    val pos =
                        (binding.rvSearchResult.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                    val itemsCount = vacancyAdapter.itemCount
                    if (pos >= itemsCount - 1) {
                        viewModel.onLastItemReached()
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        binding.tvResultCountChips.text = currentItemsFound
    }

    companion object {
        private const val CLICK_DEBOUNCE_DELAY_MILLIS = 200L
        private const val ZERO_ITEMS = ""
    }
}
