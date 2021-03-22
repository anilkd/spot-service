package spot.price.service

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import spot.price.service.fetcher.EuropeanExchangeRatesFetcher
import spot.price.service.fetcher.Exchange
import spot.price.service.fetcher.ExchangeRateModel
import spot.price.service.fetcher.LiveRatesFetcher
import java.math.BigDecimal

internal class DefaultSpotServiceImplTest {

    private val liveRatesFetcher = mockk<LiveRatesFetcher>()
    private val europeanExchangeRatesFetcher = mockk<EuropeanExchangeRatesFetcher>()

    @Test
    fun `should pick best exchange rate from available exchanges`() {
        //given
        every { liveRatesFetcher.fetchRates(any(), any()) }.returns(
            ExchangeRateModel(
                from = "GBP",
                to = "USD",
                rate = BigDecimal("1.38612000"),
                exchange = Exchange.LiveRates
            )
        )
        every { europeanExchangeRatesFetcher.fetchRates(any(), any()) }.returns(
            ExchangeRateModel(
                from = "GBP",
                to = "USD",
                rate = BigDecimal("1.3864953418"),
                exchange = Exchange.EuropeanExchange
            )
        )

        //when
        val bestRate =
            DefaultSpotServiceImpl(listOf(liveRatesFetcher, europeanExchangeRatesFetcher)).getBestRate("GBP", "USD")

        //then
        assertThat(bestRate).isEqualTo(
            ExchangeRateModel(
                from = "GBP",
                to = "USD",
                rate = BigDecimal("1.3864953418"),
                exchange = Exchange.EuropeanExchange
            )
        )
    }
}