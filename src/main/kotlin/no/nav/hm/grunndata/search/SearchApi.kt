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
        return HttpResponse.ok(searchService.searchWithBody(SearchService.PRODUCTS, params, body))
    }

    @Get(uris=["/products/_search{?params*}"])
    fun searchProductWithQuery(params: Map<String, String>?): HttpResponse<String> {
        LOG.info("Got query request for product")
        return HttpResponse.ok(searchService.searchWithQuery(SearchService.PRODUCTS, params))
            .header(CACHE_CONTROL, "public, immutable, max-age=300")
    }

    @Post(uris=["/agreements/_search{?params*}"])
    fun searchAgreementWithBody(@QueryValue params: Map<String, String>?, @Body body: String): HttpResponse<String> {
        LOG.info("Got body request for agreement")
        return HttpResponse.ok(searchService.searchWithBody(SearchService.AGREEMENTS, params, body))
    }

    @Get(uris=["/agreements/_search{?params*}"])
    fun searchAgreementWithQuery(params: Map<String, String>?): HttpResponse<String> {
        LOG.info("Got query request for agreement")
        return HttpResponse.ok(searchService.searchWithQuery(SearchService.AGREEMENTS, params))
            .header(CACHE_CONTROL, "public, immutable, max-age=300")
    }

    @Post(uris=["/suppliers/_search{?params*}"])
    fun searchSupplierWithBody(@QueryValue params: Map<String, String>?, @Body body: String): HttpResponse<String> {
        LOG.info("Got body request for supplier")
        return HttpResponse.ok(searchService.searchWithBody(SearchService.SUPPLIERS, params, body))
    }

    @Get(uris=["/suppliers/_search{?params*}"])
    fun searchSupplierWithQuery(params: Map<String, String>?): HttpResponse<String> {
        LOG.info("Got query request for supplier")
        return HttpResponse.ok(searchService.searchWithQuery(SearchService.SUPPLIERS, params))
            .header(CACHE_CONTROL, "public, immutable, max-age=300")
    }

    @Post(uris=["/news/_search{?params*}"])
    fun searchNewsWithBody(@QueryValue params: Map<String, String>?, @Body body: String): HttpResponse<String> {
        LOG.info("Got body request for news")
        return HttpResponse.ok(searchService.searchWithBody(SearchService.NEWS, params, body))
    }

    @Get(uris=["/news/_search{?params*}"])
    fun searchNewsWithQuery(params: Map<String, String>?): HttpResponse<String> {
        LOG.info("Got query request for news")
        return HttpResponse.ok(searchService.searchWithQuery(SearchService.NEWS, params))
            .header(CACHE_CONTROL, "public, immutable, max-age=300")
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
        data class Response(
            val offset: Int,
            val limit: Int,
            val total: Int,
            val results: List<JsonNode>,
        )

        val since = params["since"]?.let { LocalDate.parse(it) } ?: LocalDate.now().minusDays(1)
        val offset = params["offset"]?.toIntOrNull() ?: 0
        val limit = params["limit"]?.toIntOrNull() ?: 1000

        LOG.info("Got request for external_products (since=$since, offset=$offset, limit=$limit)")

        val query = """
            {
                "query": {
                    "bool": {
                        "filter": [
                            {
                                "range": {
                                    "updated": {
                                        "gte": "$since",
                                        "format": "yyyy-MM-dd"
                                    }
                                }
                            }
                        ],
                        "must": [
                            {
                                "match": {
                                    "status": "ACTIVE"
                                }
                            }
                        ]
                    }
                },
                "sort": [
                    {
                        "updated": {
                            "order": "asc"
                        }
                    }
                ],
                "from": $offset,
                "size": $limit
            }
        """.trimIndent()

        val results: OSResponse = objectMapper.readValue(searchService.searchWithBody(SearchService.EXTERNAL_PRODUCTS, mapOf(), query))
        return HttpResponse.ok(objectMapper.writeValueAsString(Response(
            offset = offset,
            limit = limit,
            total = results.hits?.total?.value ?: 0,
            results = results.hits?.hits?.map { it.externalProduct } ?: listOf(),
        )))
    }
}
