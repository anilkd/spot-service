package spot.price.clients.liverates

import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("exchangesratesapi")
interface ExchangeRatesAPIClient {
    @Get("/latest")
    fun fetchRates(@QueryValue("base") base: String, @QueryValue("symbols") symbols: List<String>): ExchangeRate
}