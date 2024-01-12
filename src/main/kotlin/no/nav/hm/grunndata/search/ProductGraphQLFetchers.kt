package no.nav.hm.grunndata.search

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.micronaut.core.annotation.Introspected
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

private val LOG = LoggerFactory.getLogger(ProductGraphQLFetchers::class.java)

@Singleton
class ProductGraphQLFetchers(
    private val searchService: SearchService,
    private val objectMapper: ObjectMapper,
) {
    fun fetchers(): Map<String, DataFetcher<*>> {
        return mapOf(
            "product" to fetcher { productFetcher(it) },
        )
    }
    private fun productFetcher(args: DataFetchingEnvironment): Product? {
        val hmsnr: String = args.getArgument<String?>("hmsnr") ?: ""
        val res = runCatching {
            searchService.lookupWithQuery(SearchService.PRODUCTS, null, hmsnr)
        }.onFailure { e ->
            LOG.error("Exception while searching for products", e)
        }.getOrNull()
        LOG.info("Resultat: ${objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(res))}")
        return res?.let { objectMapper.readValue<OpenSearchResponse>(it) }?.result
    }
}

data class OpenSearchResponse (
    val found: Boolean,

    @JsonAlias("_index")
    val index: String,

    @JsonAlias("_source")
    val result: Product?,
)

@Introspected
data class Product (
    @JsonAlias("hmsArtNr")
    val hmsnr: String?,

    val isoCategoryTextShort: String?,
)
