package ru.practicum.android.diploma.data.dto.responses.vacancy.list

import com.google.gson.annotations.SerializedName

data class ResponseListDto(
    val found: Int, // всего найдено по запросу
    val items: List<Item>, // список вакансий
    val page: Int, // номер страницы
    val pages: Int, // всего страниц
    @SerializedName("per_page")
    val perPage: Int, // кол-во итемов на странице
)
