package no.nav.hm.grunndata.search

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test

@MicronautTest
class QueryTest(private val searchService: SearchService, private val objectMapper: ObjectMapper)  {

    val suppliers = listOf("AB Transistor Sweden")
    //@Test
    fun queryTest() {
        suppliers.forEach {
            val query = """
            {
                "query": "SELECT title, supplier.name, isoCategory from products where status='ACTIVE' and hasAgreement=false and supplier.name LIKE '%$it%'";
            }
            """.trimIndent()
            println(searchService.sqlQuery(params = mapOf("format" to "csv"), query))
        }
    }
}