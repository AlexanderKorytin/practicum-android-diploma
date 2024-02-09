package ru.practicum.android.diploma.data.network.vacancies

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
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
import java.net.ConnectException
import java.net.SocketTimeoutException

@RunWith(MockitoJUnitRunner::class)
class RepositoryDetailsImplTest {

    private lateinit var testNetworkClient: RetrofitNetworkClient
    private lateinit var testDetailsRepository: RepositoryDetailsImpl

    @Before
    fun setUp() {
        testNetworkClient = mock()
        testDetailsRepository = RepositoryDetailsImpl(testNetworkClient)
    }

    @After
    fun tearDown() {
        reset(testNetworkClient)
    }

    @Test
    fun `should return same vacancyDetails`() = runTest {
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

        val expected = testVacancyDetailDto.mapToVacancyDetails()
        val actual = (testDetailsRepository.getVacancyDetails("42").first() as SearchResultData.Data).value

        Assertions.assertEquals(actual, expected)
    }

    @Test
    fun `should return internet error state`() = runTest {
        Mockito.`when`(testNetworkClient.getCurrentVacancy(request = DetailsRequest("42"))).thenReturn(
            Result.failure(ConnectException())
        )
        val expected = SearchResultData.NoInternet<Any>(2131886278)
        val actual = testDetailsRepository.getVacancyDetails("42").first()

        Assertions.assertEquals(actual, expected)
    }

    @Test
    fun `should return internet error state timeout`() = runTest {
        Mockito.`when`(testNetworkClient.getCurrentVacancy(request = DetailsRequest("42"))).thenReturn(
            Result.failure(SocketTimeoutException())
        )
        val expected = SearchResultData.NoInternet<Any>(2131886278)
        val actual = testDetailsRepository.getVacancyDetails("42").first()

        Assertions.assertEquals(actual, expected)
    }
}
