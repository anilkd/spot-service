package spot.price.service.fetcher

import spot.price.clients.liverates.ExchangeRatesAPIClient
import spot.price.clients.liverates.LiveRatesClient
import java.math.BigDecimal
import javax.inject.Named
import javax.inject.Singleton

data class ExchangeRateModel(val from: String, val to: String, val rate: BigDecimal, val exchange: Exchange)

enum class Exchange {
    EuropeanExchange,
    LiveRates
}

interface RatesFetcher {
    fun fetchRates(fromCurrency: String, toCurrency: String): ExchangeRateModel
}

@Singleton
@Named("api.exchangeratesapi.io")
class EuropeanExchangeRatesFetcher(private val exchangeRatesAPIClient: ExchangeRatesAPIClient) : RatesFetcher {
    override fun fetchRates(fromCurrency: String, toCurrency: String): ExchangeRateModel {
        return exchangeRatesAPIClient.fetchRates(fromCurrency, listOf(toCurrency))
            .let {
                ExchangeRateModel(
                    from = it.base,
                    to = toCurrency,
                    rate = it.rates[toCurrency]!!,
                    exchange = Exchange.EuropeanExchange
                )
            }
    }
}

@Singleton
@Named("live-rates.com")
class LiveRatesFetcher(private val liveRatesClient: LiveRatesClient) : RatesFetcher {
    override fun fetchRates(fromCurrency: String, toCurrency: String): ExchangeRateModel {
        return liveRatesClient.fetchRates().first { it.currency == "${fromCurrency}/$toCurrency" }
            .let {
                ExchangeRateModel(
                    from = fromCurrency,
                    to = toCurrency,
                    rate = BigDecimal(it.rate),
                    exchange = Exchange.LiveRates
                )
            }
    }
}