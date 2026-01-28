package no.nav.hm.grunndata.search

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micronaut.http.HttpHeaders.CACHE_CONTROL
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalDateTime

@Controller
class SearchApi(
    private val searchService: SearchService,
    private val objectMapper: ObjectMapper,
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(SearchApi::class.java)
    }

    @Post(uris=["/products/_search{?params*}"])
    fun searchProductWithBody(@QueryValue params: Map<String, String>?, @Body body: String): HttpResponse<String> {
        LOG.info("Got body request for product")
        return HttpResponse.ok(searchService.searchWithBody(SearchService.PRODUCTS, params?: emptyMap(), body))
    }

    @Get(uris=["/products/_search{?params*}"])
    fun searchProductWithQuery(params: Map<String, String>?): HttpResponse<String> {
        LOG.info("Got query request for product")
        return HttpResponse.ok(searchService.searchWithQuery(SearchService.PRODUCTS, params?: emptyMap()))
            .header(CACHE_CONTROL, "public, immutable, max-age=300")
    }

    @Post(uris=["/agreements/_search{?params*}"])
    fun searchAgreementWithBody(@QueryValue params: Map<String, String>?, @Body body: String): HttpResponse<String> {
        LOG.info("Got body request for agreement")
        return HttpResponse.ok(searchService.searchWithBody(SearchService.AGREEMENTS, params?: emptyMap(), body))
    }

    @Get(uris=["/agreements/_search{?params*}"])
    fun searchAgreementWithQuery(params: Map<String, String>?): HttpResponse<String> {
        LOG.info("Got query request for agreement")
        return HttpResponse.ok(searchService.searchWithQuery(SearchService.AGREEMENTS, params?: emptyMap()))
            .header(CACHE_CONTROL, "public, immutable, max-age=300")
    }

    @Post(uris=["/suppliers/_search{?params*}"])
    fun searchSupplierWithBody(@QueryValue params: Map<String, String>?, @Body body: String): HttpResponse<String> {
        LOG.info("Got body request for supplier")
        return HttpResponse.ok(searchService.searchWithBody(SearchService.SUPPLIERS, params?: emptyMap(), body))
    }

    @Get(uris=["/suppliers/_search{?params*}"])
    fun searchSupplierWithQuery(params: Map<String, String>?): HttpResponse<String> {
        LOG.info("Got query request for supplier")
        return HttpResponse.ok(searchService.searchWithQuery(SearchService.SUPPLIERS, params?: emptyMap()))
            .header(CACHE_CONTROL, "public, immutable, max-age=300")
    }

    @Post(uris=["/news/_search{?params*}"])
    fun searchNewsWithBody(@QueryValue params: Map<String, String>?, @Body body: String): HttpResponse<String> {
        LOG.info("Got body request for news")
        return HttpResponse.ok(searchService.searchWithBody(SearchService.NEWS, params?: emptyMap(), body))
    }

    @Get(uris=["/news/_search{?params*}"])
    fun searchNewsWithQuery(params: Map<String, String>?): HttpResponse<String> {
        LOG.info("Got query request for news")
        return HttpResponse.ok(searchService.searchWithQuery(SearchService.NEWS, params?: emptyMap()))
            .header(CACHE_CONTROL, "public, immutable, max-age=300")
    }

    @Post(uris=["/servicejobs/_search{?params*}"])
    fun searchServicejobsWithBody(@QueryValue params: Map<String, String>?, @Body body: String): HttpResponse<String> {
        LOG.info("Got body request for servicejobs")
        return HttpResponse.ok(searchService.searchWithBody(SearchService.SERVICEJOBS, params?: emptyMap(), body))
    }

    @Get(uris=["/servicejobs/_search{?params*}"])
    fun searchServicejobsWithQuery(params: Map<String, String>?): HttpResponse<String> {
        LOG.info("Got query request for servicejobs")
        return HttpResponse.ok(searchService.searchWithQuery(SearchService.SERVICEJOBS, params?: emptyMap()))
            .header(CACHE_CONTROL, "public, immutable, max-age=300")
    }

    @Post(uris = ["/alternative_products/_search{?params*}"])
    fun searchAlternativesWithBody(@QueryValue params: Map<String, String>?, @Body body: String): HttpResponse<String> {
        LOG.info("Got body request for alternatives")
        return HttpResponse.ok(searchService.searchWithBody(SearchService.ALTERNATIVES, params?: emptyMap(), body))
    }

    @Post(uris=["/external_products{?params*}"])
    fun getExternalProducts(@QueryValue params: Map<String, String>): HttpResponse<String> {
        data class OSResponseHit(
            @JsonAlias("_source")
            val externalProduct: JsonNode,
        )
        data class OSResponseTotal(
            val value: Int,
        )
        data class OSResponseHits(
            val hits: List<OSResponseHit>?,
            val total: OSResponseTotal,
        )
        data class OSResponse(
            val hits: OSResponseHits?,
        )
        data class ResponseQuery(
            val limit: Int,
            val since: LocalDateTime,
            val before: LocalDateTime?,
        )
        data class Response(
            val query: ResponseQuery,
            val hasMore: Boolean,
            val results: List<JsonNode>,
        )

        val since = params["since"]?.let { since ->
            runCatching { LocalDateTime.parse(since) }.getOrElse {
                // Support date-only
                LocalDate.parse(since).atStartOfDay()
            }
        } ?: LocalDate.now().minusDays(1).atStartOfDay()

        val before = params["before"]?.let { before ->
            runCatching { LocalDateTime.parse(before) }.getOrElse {
                // Support date-only
                LocalDate.parse(before).atStartOfDay()
            }
        }

        val maxLimit = 1000
        val limit = params["limit"]?.toIntOrNull() ?: maxLimit
        if (limit > 1000) {
            return HttpResponse.badRequest("""{"error": "limit cannot be larger than $maxLimit"}""")
        }

        LOG.info("Got request for external_products (since=$since, before=$before, limit=$limit)")

        val beforeExtraQueryContent = if (before != null) { """ , "lt": "$before" """ } else { "" }

        val query = """
            {
                "query": {
                    "range": { "updated": { "gte": "$since" $beforeExtraQueryContent } }
                },
                "sort": [ { "updated": { "order": "desc" } } ],
                "from": 0,
                "size": $limit
            }
        """.trimIndent()

        val results: OSResponse = objectMapper.readValue(searchService.searchWithBody(SearchService.EXTERNAL_PRODUCTS, mapOf(), query))
        return HttpResponse.ok(objectMapper.writeValueAsString(Response(
            query = ResponseQuery(
                limit = limit,
                since = since,
                before = before,
            ),
            hasMore = results.hits?.total?.value != (results.hits?.hits?.count() ?: 0),
            results = results.hits?.hits?.map { it.externalProduct } ?: listOf(),
        )))
    }
}
