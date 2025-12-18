package no.nav.hm.grunndata.search

import com.fasterxml.jackson.annotation.JacksonInject
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue
import com.fasterxml.jackson.annotation.JsonProperty
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
            "products" to fetcher { productsFetcher(it) },
        )
    }

    private fun productsFetcher(args: DataFetchingEnvironment): List<Product> {
        val hmsnrs: List<String> = args.getArgumentAs("hmsnrs") ?: return emptyList()
        val hmsnrsSet = hmsnrs.toSet()
        require(hmsnrsSet.count() <= maxNumberOfResults) { "too many hmsnrs in request (max=$maxNumberOfResults)" }

        val res = runCatching {
            searchService.searchWithBody(SearchService.PRODUCTS, emptyMap(), hmsnrsSearchQuery(hmsnrsSet))
        }.onFailure { e ->
            LOG.error("Exception caught and ignored while searching for products (graphql)", e)
        }.onSuccess {
            // LOG.info("Resultat: ${objectMapper.prettifyJson(it)}")
        }.getOrNull()?.let { objectMapper.readValue<OpenSearchResponse>(it) } ?: OpenSearchResponse.empty()
        return res.hits.hits.map { it.product }
    }
}

private val maxNumberOfResults: Int = 500
private val hmsnrsSearchQuery = { hmsnrs: Set<String> ->
    hmsnrs.forEach { hmsnr ->
        checkNotNull(hmsnr.toIntOrNull()) { "Hmsnr ikke gyldig: $hmsnr" }
    }
    """{"query": {"terms": {"hmsArtNr": [${hmsnrs.joinToString { """"$it"""" }}]}}, "size": $maxNumberOfResults}"""
}

data class OpenSearchResponse (
    val hits: OpenSearchResponseHits,
) {
    companion object {
        fun empty() = OpenSearchResponse(hits = OpenSearchResponseHits(hits = emptyList()))
    }
}

data class OpenSearchResponseHits (
    val hits: List<OpenSearchResponseHit>,
)

data class OpenSearchResponseHit (
    @JsonAlias("_source")
    val product: Product,
)

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
    val isoCategoryTitleShort: String?,
    val isoCategoryText: String?,
    val isoCategoryTextShort: String?,
    val main: Boolean = true,
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
    val agreements: List<AgreementInfoDoc> = emptyList(),
    val hasAgreement: Boolean = false,
    @JacksonInject("digihotCluster")
    @JsonProperty(value= "digihotCluster", required=false)
    val digihotCluster: String?=null
) {
    fun dataAsText(): String = data.joinToString { it.toString() }

    fun productURL(): String {
       return when (digihotCluster) {
           "prod" -> "https://finnhjelpemiddel.nav.no/produkt/${seriesId}"
           "dev" -> "https://finnhjelpemiddel.intern.dev.nav.no/produkt/${seriesId}"
           "localhost" -> "http://localhost:8080/produkt/${seriesId}"
           else -> "https://finnhjelpemiddel.nav.no/produkt/${seriesId}"
       }
    }

    fun productVariantURL(): String {
       return when (digihotCluster) {
           "prod" -> "https://finnhjelpemiddel.nav.no/produkt/hmsartnr/${hmsArtNr}"
           "dev" -> "https://finnhjelpemiddel.intern.dev.nav.no/produkt/hmsartnr/${hmsArtNr}"
           "localhost" -> "http://localhost:8080/produkt/hmsartnr/${hmsArtNr}"
           else -> "https://finnhjelpemiddel.nav.no/produkt/hmsartnr/${hmsArtNr}"
       }
    }
}


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
    val postId: UUID? = null,
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
    val digitalSoknad: Boolean? = null,
    val sortimentKategori: String? = null,
    val pakrevdGodkjenningskurs: PakrevdGodkjenningskurs? = null,
    val produkttype: Produkttype? = null,
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
    val dybdeCM: Int?,
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
enum class Produkttype {
    Hovedprodukt,
    Tilbehoer,
    Del,
}

@Introspected
data class PakrevdGodkjenningskurs (
    val tittel: String,
    val isokode: String,
    val kursId: Int,
)

@Introspected
data class TechData (
    val key:    String,
    val value:  String,
    val unit:   String,
) {
    override fun toString(): String = "$key ${value}${unit}".trim()
}

@Introspected
data class CompatibleWith (
    val seriesIds: Set<UUID> = emptySet(),
)
