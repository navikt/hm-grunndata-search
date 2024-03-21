package no.nav.hm.grunndata.search

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.micronaut.context.annotation.Value
import io.micronaut.core.annotation.Introspected
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.net.URI

private val LOG = LoggerFactory.getLogger(BestillingsordningGraphQLFetchers::class.java)

@Singleton
class BestillingsordningGraphQLFetchers(
    private val searchService: SearchService,
    private val objectMapper: ObjectMapper,
    @Value("\${digihotSortiment.bestillingsordning}")
    private val bestillingsordningUrl: String,
) {
    private val bestillingsordningMap: Map<String, BestillingsordningDTO> =
        objectMapper.readValue(URI(bestillingsordningUrl).toURL(), object : TypeReference<List<BestillingsordningDTO>>(){}).associateBy { it.hmsnr }

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
                bestillingsordning = bestillingsordningMap[hmsnr]?.let { true } ?: false,
            )
        }
    }
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
