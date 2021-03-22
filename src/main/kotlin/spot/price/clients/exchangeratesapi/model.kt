package spot.price.clients.liverates

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.DateDeserializers
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import java.math.BigDecimal
import java.time.Instant
import java.util.Date

data class ExchangeRate(
    val base: String,
    val rates: Map<String, BigDecimal>,
    val date: Date
)