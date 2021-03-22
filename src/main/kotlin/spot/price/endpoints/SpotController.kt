package spot.price.endpoints

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import spot.price.service.SpotService
import spot.price.service.fetcher.ExchangeRateModel

@Controller("/spot")
class SpotController(private val spotService: SpotService) {

    @Get
    @ExecuteOn(TaskExecutors.IO)
    fun getRates(from: String, to: String): ExchangeRateModel? {
        return spotService.getBestRate(from, to)
    }
}
