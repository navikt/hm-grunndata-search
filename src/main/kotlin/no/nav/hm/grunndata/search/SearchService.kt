package no.nav.hm.grunndata.search

import jakarta.inject.Singleton
import org.opensearch.client.opensearch.OpenSearchClient
import org.opensearch.client.opensearch.generic.Request
import org.opensearch.client.opensearch.generic.Requests
import org.slf4j.LoggerFactory


@Singleton
class SearchService(private val osclient: OpenSearchClient) {

    fun searchWithBody(index: String, params: Map<String, String>, body: String): String {
        val request: Request = newRequest("POST", "/$index/_search", params, body)
        LOG.debug("search on index: $index params: $params and body: $body")
        return performRequest(request, params)
    }


    fun searchWithQuery(index: String, params: Map<String, String>): String {
        val request: Request = newRequest("GET", "/$index/_search", params, null)
        return performRequest(request, params)
    }

    fun lookupWithQuery(index: String, params: Map<String, String>, id: String): String {
        val request: Request = newRequest("GET", "/$index/_doc/$id", params, null)
        return performRequest(request, params)
    }

    fun sqlQuery(params: Map<String, String>, query: String): String {
        val request: Request = newRequest("POST", "/_plugins/_sql", params, query)
        return performRequest(request, params)
    }

    private fun performRequest(request: Request, params: Map<String, String>): String {
        require(onlyAllowedParams(params)) { "Disallowed request params present in " + params.keys }
        return try {
            osclient.generic().execute(request).use { response ->
                response.body.get().bodyAsString()
            }
        }
        catch (e: Exception) {
            LOG.error("Error performing request", e)
            throw e
        }
    }


    private fun newRequest(method: String, endpoint: String, params: Map<String, String>, body: String?): Request {
        val requestBuilder = Requests.builder().method(method).endpoint(endpoint).query(params)
        if (body != null) {
            requestBuilder.json(body)
        }
        return requestBuilder.build()
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
        val ALLOWED_REQUEST_PARAMS = setOf("q", "filter_path", "pretty", "format")
    }

}
