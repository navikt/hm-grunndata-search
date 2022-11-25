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


    @Get("/product/_doc/{id}{?params*}")
    fun getProductById(params: Map<String, String>?, id: String): HttpResponse<String> {
        LOG.info("Got get lookup request for product $params $id")
        return HttpResponse.ok(searchService.lookupWithQuery(SearchService.PRODUCT, params, id))
            .header(CACHE_CONTROL, "public, immutable, max-age=3600")
    }

    @Get("/agreement/_doc/{id}{?params*}")
    fun searchAgreementWithQuery(params: Map<String, String>?, id: String): HttpResponse<String> {
        LOG.info("Got get lookup request for agreement $params $id ")
        return HttpResponse.ok(searchService.lookupWithQuery(SearchService.AGREEMENT, params, id))
            .header(CACHE_CONTROL, "public, immutable, max-age=3600")
    }

    @Get("/supplier/_doc/{id}{?params*}")
    fun searchSupplierWithQuery(params: Map<String, String>?, id:String): HttpResponse<String> {
        LOG.info("Got get lookup request for supplier $params $id")
        return HttpResponse.ok(searchService.lookupWithQuery(SearchService.SUPPLIER, params,id))
            .header(CACHE_CONTROL, "public, immutable, max-age=3600")
    }
}
