package no.nav.hm.grunndata.search

import io.kotest.matchers.nulls.shouldNotBeNull
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper

@MicronautTest
class QueryTest(private val searchService: SearchService, private val objectMapper: ObjectMapper)  {

    val suppliers = listOf("AB Transistor Sweden")
   //@Test
    fun queryTest() {
        objectMapper.shouldNotBeNull()
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