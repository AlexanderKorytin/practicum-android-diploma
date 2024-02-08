package ru.practicum.android.diploma.presentation.vacancy

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.models.vacancy.VacancyItem

class VacancyAdapter(
    private val onClick: (VacancyItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items: MutableList<ItemType> = mutableListOf()

    fun addVacancies(vacancies: List<VacancyItem>) {
        for (vacancy in vacancies) {
            items.add(ItemType.Vacancy(vacancy))
        }
        notifyItemRangeInserted(items.size - vacancies.size, vacancies.size)
    }

    fun addLoading() {
        items.add(ItemType.Loading)
        notifyItemInserted(items.size - 1)
    }

    fun removeLoading() {
        val position = items.indexOf(ItemType.Loading)
        if (position != -1) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun clearData() {
        items.clear()
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ItemType.Vacancy -> 0
            is ItemType.Loading -> 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> VacancyViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.view_vacancy, parent, false)
            )

            else -> LoadingViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.view_loading, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (item) {
            is ItemType.Vacancy -> (holder as VacancyViewHolder).bind(item.vacancyItem)
            is ItemType.Loading -> (holder as LoadingViewHolder).bind()
        }

        if (item is ItemType.Vacancy) {
            holder.itemView.setOnClickListener {
                onClick.invoke(item.vacancyItem)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
