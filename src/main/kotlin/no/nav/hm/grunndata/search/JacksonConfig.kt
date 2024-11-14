package no.nav.hm.grunndata.search

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.InjectableValues
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.micronaut.context.annotation.Value
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import jakarta.inject.Singleton


@Singleton
class JacksonConfig(@Value("\${digihot.cluster}") val digitHotCluster: String) : BeanCreatedEventListener<ObjectMapper> {

    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(JacksonConfig::class.java)
    }

    override fun onCreated(event: BeanCreatedEvent<ObjectMapper>): ObjectMapper {
        val objectMapper = event.bean
        val std = InjectableValues.Std()
        LOG.info("Got digihotCluster: $digitHotCluster")
        std.addValue("digihotCluster", digitHotCluster)
        objectMapper.registerModule(JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .setInjectableValues(std)
        return objectMapper
    }
}
