package no.nav.hm.grunndata.db.search

import io.micronaut.http.HttpHeaders.CACHE_CONTROL
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import org.slf4j.LoggerFactory


@Controller
class DocApi(private val searchService: SearchService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(DocApi::class.java)
    }


    @Get(uris=["/products/_doc/{id}{?params*}"])
    fun getProductById(params: Map<String, String>?, id: String): HttpResponse<String> {
        LOG.info("Got get lookup request for product $params $id")
        return HttpResponse.ok(searchService.lookupWithQuery(SearchService.PRODUCTS, params, id))
            .header(CACHE_CONTROL, "public, immutable, max-age=3600")
    }

    @Get(uris=["/agreements/_doc/{id}{?params*}"])
    fun searchAgreementWithQuery(params: Map<String, String>?, id: String): HttpResponse<String> {
        LOG.info("Got get lookup request for agreement $params $id ")
        return HttpResponse.ok(searchService.lookupWithQuery(SearchService.AGREEMENTS, params, id))
            .header(CACHE_CONTROL, "public, immutable, max-age=3600")
    }

    @Get(uris=["/suppliers/_doc/{id}{?params*}"])
    fun searchSupplierWithQuery(params: Map<String, String>?, id:String): HttpResponse<String> {
        LOG.info("Got get lookup request for supplier $params $id")
        return HttpResponse.ok(searchService.lookupWithQuery(SearchService.SUPPLIERS, params,id))
            .header(CACHE_CONTROL, "public, immutable, max-age=3600")
    }
}
