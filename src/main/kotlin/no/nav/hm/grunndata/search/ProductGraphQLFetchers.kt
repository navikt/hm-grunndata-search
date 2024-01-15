package no.nav.hm.grunndata.search

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.micronaut.core.annotation.Introspected
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.UUID

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
            searchService.searchWithBody(SearchService.PRODUCTS, null, hmsnrSearchQuery(hmsnr))
        }.onFailure { e ->
            LOG.error("Exception while searching for products", e)
        }.getOrNull()
        // LOG.info("Resultat: ${objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(res))}")
        return res?.let { objectMapper.readValue<OpenSearchResponse>(it) }?.hits?.hits?.firstOrNull()?.source
    }
}

private val hmsnrSearchQuery = { hmsnr: String ->
    checkNotNull(hmsnr.toIntOrNull()) { "Hmsnr ikke gyldig: $hmsnr" }
    """{"query": {"match": {"hmsArtNr": "$hmsnr"}}}"""
}

data class OpenSearchResponse (
    val hits: OpenSearchResponseHits,
)

data class OpenSearchResponseHits (
    val hits: List<OpenSearchResponseHit>,
)

data class OpenSearchResponseHit (
    @JsonAlias("_source")
    val source: Product,
)

@Introspected
data class Product (
    val id: String,
    val supplier: ProductSupplier,
    val title: String,
    val articleName: String,
    val attributes: AttributesDoc,
    val status: ProductStatus,
    val hmsArtNr: String?=null,
    val identifier: String,
    val supplierRef: String,
    val isoCategory: String,
    val isoCategoryTitle: String?,
    val isoCategoryText: String?,
    val isoCategoryTextShort: String?,
    val accessory: Boolean = false,
    val sparePart: Boolean = false,
    val seriesId: String?=null,
    val data: List<TechData> = emptyList(),
    val media: List<MediaDoc> = emptyList(),
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val expired: LocalDateTime,
    val createdBy: String,
    val updatedBy: String,
    val filters: TechDataFilters,
    val agreementInfo: AgreementInfoDoc?,
    val agreements: List<AgreementInfoDoc> = emptyList(),
    val hasAgreement: Boolean = false,
)

@Introspected
data class AgreementInfoDoc (
    val id: UUID,
    val identifier: String?=null,
    val title: String?=null,
    val label: String,
    val rank: Int,
    val postNr: Int,
    val postIdentifier: String?=null,
    val postTitle: String?=null,
    val reference: String,
    val expired: LocalDateTime,
)

@Introspected
data class AttributesDoc (
    val manufacturer: String? = null,
    val compatibleWith: CompatibleWith? = null,
    val keywords: List<String>? = null,
    val series: String? = null,
    val shortdescription: String? = null,
    val text: String? = null,
    val url: String? = null,
    val bestillingsordning: Boolean? = null,
    val tenderId: String? = null,
    val hasTender: Boolean? = null,
)

@Introspected
data class MediaDoc (
    val uri: String,
    val priority: Int = 1,
    val type: MediaType = MediaType.IMAGE,
    val text: String?=null,
    val source: MediaSourceType = MediaSourceType.HMDB,
)

@Introspected
data class TechDataFilters (
    val fyllmateriale: String?,
    val setebreddeMaksCM: Int?,
    val setebreddeMinCM: Int?,
    val brukervektMinKG: Int?,
    val materialeTrekk: String?,
    val setedybdeMinCM: Int?,
    val setedybdeMaksCM: Int?,
    val setehoydeMaksCM: Int?,
    val setehoydeMinCM: Int?,
    val totalVektKG: Int?,
    val lengdeCM: Int?,
    val breddeCM: Int?,
    val beregnetBarn: String?,
    val brukervektMaksKG: Int?,
)

@Introspected
data class ProductSupplier(
    val id: String,
    val identifier: String,
    val name: String,
)

@Introspected
enum class ProductStatus {
    ACTIVE,
    @JsonEnumDefaultValue
    INACTIVE,
    DELETED,
}

@Introspected
enum class MediaSourceType {
    HMDB,
    REGISTER,
    EXTERNALURL,
    IMPORT,
    @JsonEnumDefaultValue
    UNKNOWN,
}

@Introspected
enum class MediaType {
    PDF,
    IMAGE,
    VIDEO,
    XLS,
    @JsonEnumDefaultValue
    OTHER,
}

@Introspected
data class TechData (
    val key:    String,
    val value:  String,
    val unit:   String,
)

@Introspected
data class CompatibleWith (
    val seriesIds: Set<UUID> = emptySet(),
)
