package spot.price

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import io.micronaut.runtime.Micronaut.build
import javax.inject.Singleton


fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("spot.price")
		.start()
}

@Singleton
internal class ObjectMapperBeanEventListener : BeanCreatedEventListener<ObjectMapper> {
	override fun onCreated(event: BeanCreatedEvent<ObjectMapper>): ObjectMapper {
		val mapper = event.bean
		mapper.registerModule(JavaTimeModule())
		return mapper
	}
}