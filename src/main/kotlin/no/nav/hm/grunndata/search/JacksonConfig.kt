package no.nav.hm.grunndata.search

import com.fasterxml.jackson.annotation.JsonInclude

import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.InjectableValues
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper


@Factory
class JacksonConfig(@Value("\${digihot.cluster}") val digitHotCluster: String) {

    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(JacksonConfig::class.java)
    }

    @Singleton
    fun objectMapper(): ObjectMapper {
        LOG.info("Initialized JacksonConfig with digihotCluster: $digitHotCluster")
        val std = InjectableValues.Std()
        std.addValue("digihotCluster", digitHotCluster)
        return JsonMapper.builderWithJackson2Defaults()
            .injectableValues(std)
            .changeDefaultPropertyInclusion{it.withValueInclusion(JsonInclude.Include.NON_NULL)}
            .disable(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .build()
    }

}
