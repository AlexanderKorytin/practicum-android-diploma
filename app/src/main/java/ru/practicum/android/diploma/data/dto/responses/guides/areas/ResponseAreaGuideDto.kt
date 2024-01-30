package ru.practicum.android.diploma.data.dto.responses.guides.areas

import com.google.gson.annotations.SerializedName

data class ResponseAreaGuideDto(
    val areas: List<AreaGuide>,
    val id: String,
    val name: String,
    @SerializedName("parent_id")
    val parentId: String?
)
