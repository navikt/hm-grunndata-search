package no.nav.hm.grunndata.search


import com.fasterxml.jackson.annotation.JsonInclude
import io.micronaut.context.annotation.Property
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import jakarta.inject.Singleton
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.InjectableValues
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.introspect.DefaultAccessorNamingStrategy
import tools.jackson.databind.json.JsonMapper


@Singleton
class JacksonConfig(@param:Property(name="digihot.cluster") val digitHotCluster: String) : BeanCreatedEventListener<JsonMapper.Builder> {


    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(JacksonConfig::class.java)
    }

    override fun onCreated(event: BeanCreatedEvent<JsonMapper.Builder>): JsonMapper.Builder {
        LOG.info("Initialized JacksonConfig with digihotCluster: $digitHotCluster")
        val std = InjectableValues.Std()
        std.addValue("digihotCluster", digitHotCluster)
        event.bean
            .changeDefaultPropertyInclusion{it.withValueInclusion(JsonInclude.Include.NON_NULL)}
            .disable(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(SerializationFeature.INDENT_OUTPUT, false)
            .accessorNaming(DefaultAccessorNamingStrategy.Provider().withFirstCharAcceptance(true, true))
            .configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, false)
            .changeDefaultPropertyInclusion { incl -> incl.withContentInclusion(JsonInclude.Include.NON_NULL) }
            .injectableValues(std)
        return event.bean
    }
}
