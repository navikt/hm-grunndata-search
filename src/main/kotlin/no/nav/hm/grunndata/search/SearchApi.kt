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

    @Post(uris=["/products/_search{?params*}"])
    fun searchProductWithBody(@QueryValue params: Map<String, String>?, @Body body: String): HttpResponse<String> {
        return HttpResponse.ok(searchService.searchWithBody(SearchService.PRODUCTS, params, body))
    }

    @Get(uris=["/products/_search{?params*}"])
    fun searchProductWithQuery(params: Map<String, String>?): HttpResponse<String> {
        LOG.info("Got get request for product $params")
        return HttpResponse.ok(searchService.searchWithQuery(SearchService.PRODUCTS, params))
            .header(CACHE_CONTROL, "public, immutable, max-age=3600")
    }


    @Post(uris=["/agreements/_search{?params*}"])
    fun searchAgreementWithBody(@QueryValue params: Map<String, String>?, @Body body: String): HttpResponse<String> {
        return HttpResponse.ok(searchService.searchWithBody(SearchService.AGREEMENTS, params, body))
    }

    @Get(uris=["/agreements/_search{?params*}"])
    fun searchAgreementWithQuery(params: Map<String, String>?): HttpResponse<String> {
        LOG.info("Got get request for product $params")
        return HttpResponse.ok(searchService.searchWithQuery(SearchService.AGREEMENTS, params))
            .header(CACHE_CONTROL, "public, immutable, max-age=3600")
    }

    @Post(uris=["/suppliers/_search{?params*}"])
    fun searchSupplierWithBody(@QueryValue params: Map<String, String>?, @Body body: String): HttpResponse<String> {
        return HttpResponse.ok(searchService.searchWithBody(SearchService.SUPPLIERS, params, body))
    }

    @Get(uris=["/suppliers/_search{?params*}"])
    fun searchSupplierWithQuery(params: Map<String, String>?): HttpResponse<String> {
        LOG.info("Got get request for product $params")
        return HttpResponse.ok(searchService.searchWithQuery(SearchService.SUPPLIERS, params))
            .header(CACHE_CONTROL, "public, immutable, max-age=3600")
    }
}
