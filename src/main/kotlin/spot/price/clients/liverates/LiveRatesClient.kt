package spot.price.clients.liverates

import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("liverates")
interface LiveRatesClient {
    @Get("/rates")
    fun fetchRates(): List<LiveRate>
}