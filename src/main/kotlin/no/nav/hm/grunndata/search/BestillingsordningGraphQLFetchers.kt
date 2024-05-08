package no.nav.hm.grunndata.search

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.context.annotation.Value
import io.micronaut.core.annotation.Introspected
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.net.URI

private val LOG = LoggerFactory.getLogger(BestillingsordningGraphQLFetchers::class.java)

@Singleton
open class BestillingsordningGraphQLFetchers(
    private val objectMapper: ObjectMapper,
    @Value("\${digihotSortiment.bestillingsordning}")
    private val bestillingsordningUrl: String,
) {
    fun fetchers(): Map<String, DataFetcher<*>> {
        return mapOf(
            "bestillingsordning" to fetcher { bestillingsordningFetcher(it) },
        )
    }

    private fun bestillingsordningFetcher(args: DataFetchingEnvironment): List<Bestillingsordning> {
        val hmsnrs: List<String> = args.getArgumentAs("hmsnrs") ?: return emptyList()
        return hmsnrs.toSet().map { hmsnr ->
            Bestillingsordning(
                hmsnr,
                bestillingsordning = cachedBestillingsordning()[hmsnr]?.let { true } ?: false,
            )
        }
    }

    @Cacheable("digihot-sortiment-bestillingsordning")
    open fun cachedBestillingsordning(): Map<String, BestillingsordningDTO> =
        objectMapper.readValue(URI(bestillingsordningUrl).toURL(), object : TypeReference<List<BestillingsordningDTO>>(){}).associateBy { it.hmsnr }
}

@Introspected
data class Bestillingsordning (
    val hmsnr: String,
    val bestillingsordning: Boolean,
)

data class BestillingsordningDTO(
    val hmsnr: String,
    val navn: String,
)
