package spot.price.clients.exchangeratesapi


import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.http.trafficlistener.ConsoleNotifyingWiremockNetworkTrafficListener
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.PropertySource
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import spot.price.clients.liverates.ExchangeRate
import spot.price.clients.liverates.ExchangeRatesAPIClient
import java.math.BigDecimal
import java.text.SimpleDateFormat
import javax.inject.Inject

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
@PropertySource(
    Property(name = "micronaut.http.services.exchangesratesapi.urls", value = "http://localhost:8889")
)
class ExchangeRatesAPIClientTest {
    private val apiMockServer = WireMockServer(
        WireMockConfiguration.wireMockConfig().port(8889)
            .networkTrafficListener(ConsoleNotifyingWiremockNetworkTrafficListener())
    )

    @Inject
    lateinit var exchangeRatesAPIClient: ExchangeRatesAPIClient

    @BeforeAll
    fun startApiMockServer() {
        apiMockServer.start()
        WireMock.configureFor("localhost", apiMockServer.port())
    }

    @AfterAll
    fun stopApiMockServer() = apiMockServer.stop()

    @BeforeEach
    fun setUp() {
        apiMockServer.resetAll()

    }

    @Test
    fun `should fetch exchange rate for GBP to USD`() = runBlocking<Unit> {
        //given
        stubResponse(200, fromCurrency = "GBP", toCurrency = "USD", responseBody = mockExchangeApiResponse)

        //when
        val exchangeRate = exchangeRatesAPIClient.fetchRates("GBP", listOf("USD"))

        //then
        assertThat(exchangeRate).isEqualTo(
            ExchangeRate(
                base = "GBP", rates = mapOf(
                    "USD" to BigDecimal("1.3864953418")
                ), date = SimpleDateFormat("yyyy-MM-dd").parse("2021-03-19")
            )
        )
    }

    //TODO Gracefully handle 500
    @Test
    fun `should gracefully handle service unavailable`() = runBlocking<Unit> {
        //given
        stubResponse(503, fromCurrency = "GBP", toCurrency = "USD", responseBody = "ERROR")

        //when
        val exception = catchThrowable { exchangeRatesAPIClient.fetchRates("GBP", listOf("USD")) }

        //then
        assertThat(exception).hasMessage("Service Unavailable")
    }


    private fun stubResponse(status: Int, fromCurrency: String, toCurrency: String, responseBody: String) {
        WireMock.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/latest"))
                .withQueryParam("base", equalTo(fromCurrency))
                .withQueryParam("symbols", equalTo(toCurrency))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)
                )
        )

    }

    private val mockExchangeApiResponse = """
        {
        "rates": {
        "USD": 1.3864953418
        },
        "base": "GBP",
        "date": "2021-03-19"
        }
    """.trimIndent()
}

