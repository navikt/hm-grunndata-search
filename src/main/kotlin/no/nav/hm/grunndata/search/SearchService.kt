package no.nav.hm.grunndata.search

import jakarta.inject.Singleton
import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.util.EntityUtils
import org.opensearch.client.Request
import org.slf4j.LoggerFactory
import org.opensearch.client.RestClient

@Singleton
class SearchService(private val osclient: RestClient) {

    fun searchWithBody(index: String, params: Map<String, String>?, body: String): String {
        val entity = StringEntity(body, ContentType.APPLICATION_JSON)
        val request: Request = newRequest("POST", "/$index/_search", params, entity)
        return performRequest(request, params)
    }


    fun searchWithQuery(index: String, params: Map<String, String>?): String {
        val request: Request = newRequest("GET", "/$index/_search", params, null)
        return performRequest(request, params)
    }

    fun lookupWithQuery(index: String, params: Map<String, String>?, id: String): String {
        val request: Request = newRequest("GET", "/$index/_doc/$id", params, null)
        return performRequest(request, params)
    }

    private fun performRequest(request: Request, params: Map<String, String>?): String {
        if (params != null) require(onlyAllowedParams(params)) { "Disallowed request params present in " + params.keys }
        return try {
            val responseEntity: HttpEntity = osclient.performRequest(request).entity
            EntityUtils.toString(responseEntity)
        } catch (e: Exception) {
            LOG.error("Connection error to Opensearch", e)
            throw e
        }
    }

    fun sqlQuery(params: Map<String, String>?, query: String): String {
        val entity = StringEntity(query, ContentType.APPLICATION_JSON)
        val request: Request = newRequest("POST", "/_plugins/_sql", params, entity)
        return performRequest(request, params)
    }

    private fun newRequest(
        method: String,
        endpoint: String,
        params: Map<String, String>?,
        entity: StringEntity?
    ): Request {
        val request = Request(method, endpoint)
        params?.forEach(request::addParameter)
        request.entity = entity
        return request
    }


    private fun onlyAllowedParams(params: Map<String, String>): Boolean {
        return ALLOWED_REQUEST_PARAMS.containsAll(params.keys)
    }

    companion object {
        const val PRODUCTS = "products"
        const val EXTERNAL_PRODUCTS = "external_products"
        const val AGREEMENTS = "agreements"
        const val SUPPLIERS = "suppliers"
        const val NEWS = "news"
        const val ALTERNATIVES = "alternative_products"
        private val LOG = LoggerFactory.getLogger(SearchService::class.java)
        val ALLOWED_REQUEST_PARAMS = setOf("q", "filter_path", "pretty")
    }

}
