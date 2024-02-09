package ru.practicum.android.diploma.data.network.vacancies

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.jupiter.api.Assertions
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import ru.practicum.android.diploma.data.dto.DetailsRequest
import ru.practicum.android.diploma.data.dto.responses.vacancy.Area
import ru.practicum.android.diploma.data.dto.responses.vacancy.Employer
import ru.practicum.android.diploma.data.dto.responses.vacancy.details.KeySkill
import ru.practicum.android.diploma.data.dto.responses.vacancy.details.ProfessionalRole
import ru.practicum.android.diploma.data.dto.responses.vacancy.details.ResponseDetailsDto
import ru.practicum.android.diploma.data.dto.responses.vacancy.details.mapToVacancyDetails
import ru.practicum.android.diploma.data.network.RetrofitNetworkClient
import ru.practicum.android.diploma.domain.models.SearchResultData

@RunWith(MockitoJUnitRunner::class)
class RepositoryDetailsImplTest {

    @Test
    fun `should return same vacancyDetails`() = runTest {

        val testNetworkClient = mock<RetrofitNetworkClient>()
        val testVacancyDetailDto = ResponseDetailsDto(
            id = "42",
            name = "test",
            salary = null,
            address = null,
            alternateUrl = "test",
            area = Area("test"),
            contacts = null,
            description = "test",
            employer = Employer(id = "42", name = "test", trusted = true, logoUrls = null),
            experience = null,
            keySkills = listOf(KeySkill("test")),
            professionalRoles = listOf(
                ProfessionalRole(
                    id = "42",
                    name = "test"
                )
            ),
            publishedAt = "test",
            employment = null,
            schedule = null
        )
        Mockito.`when`(testNetworkClient.getCurrentVacancy(DetailsRequest("42")))
            .thenReturn(Result.success(testVacancyDetailDto))
        val repositoryDetails = RepositoryDetailsImpl(testNetworkClient)

        val expected = testVacancyDetailDto.mapToVacancyDetails()
        val actual = (repositoryDetails.getVacancyDetails("42").first() as SearchResultData.Data).value

        Assertions.assertEquals(actual, expected)
    }
}
