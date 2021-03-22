package spot.price.service

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import spot.price.service.fetcher.ExchangeRateModel
import spot.price.service.fetcher.RatesFetcher
import javax.inject.Singleton

interface SpotService {
    fun getBestRate(fromCurrency: String, toCurrency: String): ExchangeRateModel?
}

@Singleton
class DefaultSpotServiceImpl(private val rateFetchers: List<RatesFetcher>) : SpotService {

    override fun getBestRate(fromCurrency: String, toCurrency: String): ExchangeRateModel? {
        val deferredResults = mutableListOf<Deferred<ExchangeRateModel?>>()

        val exchangeRates = runBlocking {
            rateFetchers.forEach {
                val deferred = async {
                    try {
                        it.fetchRates(fromCurrency, toCurrency)
                    } catch (e: Throwable) {
                        //TODO Handle gracefully
                        null
                    }
                }
                deferredResults.add(deferred)
            }
            deferredResults.awaitAll()
        }
        return exchangeRates.mapNotNull { it }.maxByOrNull { it.rate }
    }

}