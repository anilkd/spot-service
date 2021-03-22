package spot.price.clients.liverates


import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
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
import java.sql.Timestamp
import java.util.Date
import javax.inject.Inject

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
@PropertySource(
    Property(name = "micronaut.http.services.liverates.urls", value = "http://localhost:8889")
)
class LiveRatesClientTest {
    private val apiMockServer = WireMockServer(
        WireMockConfiguration.wireMockConfig().port(8889)
            .networkTrafficListener(ConsoleNotifyingWiremockNetworkTrafficListener())
    )

    @Inject
    lateinit var liveRatesClient: LiveRatesClient

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
        stubResponse(200, responseBody = mockLiveRatesApiResponse)

        //when
        val exchangeRates = liveRatesClient.fetchRates()

        //then
        assertThat(exchangeRates).isEqualTo(
            listOf(LiveRate(currency = "GBP/USD", rate = "1.38628", timestamp = Date(Timestamp(1616187302408).time)))

        )
    }

    //TODO Gracefully handle 500
    @Test
    fun `should gracefully handle service unavailable`() = runBlocking<Unit> {
        //given
        stubResponse(503, responseBody = "ERROR")

        //when
        val exception = catchThrowable { liveRatesClient.fetchRates() }

        //then
        assertThat(exception).hasMessage("Service Unavailable")
    }


    private fun stubResponse(status: Int, responseBody: String) {
        WireMock.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/rates"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)
                )
        )

    }

    private val mockLiveRatesApiResponse = """
    [{
        "currency": "GBP/USD",
        "rate": "1.38628",
        "bid": "1.38628",
        "ask": "1.38644",
        "high": "1.39585",
        "low": "1.38297",
        "open": "1.39224",
        "close": "1.38628",
        "timestamp": "1616187302408"
    }]
    """.trimIndent()
}

