package no.nav.hm.grunndata.db.search

import io.micronaut.http.HttpHeaders.CACHE_CONTROL
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import org.slf4j.LoggerFactory


@Controller
class SearchApi(private val searchService: SearchService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SearchApi::class.java)
    }

    @Post("/product/_search{?params*}")
    fun searchProductWithBody(@QueryValue params: Map<String, String>?, @Body body: String): HttpResponse<String> {
        return HttpResponse.ok(searchService.searchWithBody(SearchService.PRODUCT, params, body))
    }

    @Get("/product/_search{?params*}")
    fun searchProductWithQuery(params: Map<String, String>?): HttpResponse<String> {
        LOG.info("Got get request for product $params")
        return HttpResponse.ok(searchService.searchWithQuery(SearchService.PRODUCT, params))
            .header(CACHE_CONTROL, "public, immutable, max-age=3600")
    }

    @Post("/agreement/_search{?params*}")
    fun searchAgreementWithBody(@QueryValue params: Map<String, String>?, @Body body: String): HttpResponse<String> {
        return HttpResponse.ok(searchService.searchWithBody(SearchService.AGREEMENT, params, body))
    }

    @Get("/agreement/_search{?params*}")
    fun searchAgreementWithQuery(params: Map<String, String>?): HttpResponse<String> {
        LOG.info("Got get request for product $params")
        return HttpResponse.ok(searchService.searchWithQuery(SearchService.AGREEMENT, params))
            .header(CACHE_CONTROL, "public, immutable, max-age=3600")
    }

    @Post("/supplier/_search{?params*}")
    fun searchSupplierWithBody(@QueryValue params: Map<String, String>?, @Body body: String): HttpResponse<String> {
        return HttpResponse.ok(searchService.searchWithBody(SearchService.SUPPLIER, params, body))
    }

    @Get("/supplier/_search{?params*}")
    fun searchSupplierWithQuery(params: Map<String, String>?): HttpResponse<String> {
        LOG.info("Got get request for product $params")
        return HttpResponse.ok(searchService.searchWithQuery(SearchService.SUPPLIER, params))
            .header(CACHE_CONTROL, "public, immutable, max-age=3600")
    }
}
