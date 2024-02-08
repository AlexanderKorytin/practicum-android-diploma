package ru.practicum.android.diploma.data.network.vacancies

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.jupiter.api.Assertions
import org.mockito.Mockito
import org.mockito.Mockito.mock
import ru.practicum.android.diploma.data.dto.DetailsRequest
import ru.practicum.android.diploma.data.dto.responses.vacancy.details.ResponseDetailsDto
import ru.practicum.android.diploma.data.network.RetrofitNetworkClient
import ru.practicum.android.diploma.domain.models.SearchResultData
import ru.practicum.android.diploma.domain.models.vacancy.VacancyDetails

class RepositoryDetailsImplTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should return same id`() = runTest {
        val testNetworkClient = mock<RetrofitNetworkClient>()
        val testVacancyDetailDto = mock<ResponseDetailsDto>()
        val testVacancyDetails = mock<VacancyDetails>()
        Mockito.`when`(testVacancyDetails.vacancyId).thenReturn("42")
        Mockito.`when`(testVacancyDetailDto.id).thenReturn("42")
        Mockito.`when`(testNetworkClient.getCurrentVacancy(DetailsRequest("42")))
            .thenReturn(Result.success(testVacancyDetailDto))
        val repositoryDetails = RepositoryDetailsImpl(testNetworkClient)

        val expected: String? = testVacancyDetails.vacancyId
        var actual: String? = null
        repositoryDetails.getVacancyDetails("42").collect {
            actual = (it as SearchResultData.Data).value?.vacancyId
        }
        advanceUntilIdle()

        Assertions.assertEquals(actual, expected)
    }
}
