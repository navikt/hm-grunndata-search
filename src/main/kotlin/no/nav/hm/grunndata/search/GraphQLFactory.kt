package no.nav.hm.grunndata.search


import graphql.GraphQL
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.core.annotation.Introspected
import io.micronaut.core.io.ResourceResolver
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import java.io.BufferedReader
import java.io.InputStreamReader

@Factory
class GraphQLFactory {
    @Bean
    @Singleton
    fun graphQL(
        resourceResolver: ResourceResolver,
        productGraphQLFetchers: ProductGraphQLFetchers,
        bestillingsordningGraphQLFetchers: BestillingsordningGraphQLFetchers,
    ): GraphQL {
        val schemaParser = SchemaParser()
        val schemaGenerator = SchemaGenerator()

        // Parse the schema.
        val typeRegistry = TypeDefinitionRegistry();
        typeRegistry.merge(schemaParser.parse(BufferedReader(InputStreamReader(
            resourceResolver.getResourceAsStream("classpath:schema.graphqls").get()))))

        // Create the runtime wiring.
        val runtimeWiring = RuntimeWiring.newRuntimeWiring()
            .type("Query") { typeWiring -> typeWiring
                .dataFetchers(productGraphQLFetchers.fetchers())
                .dataFetchers(bestillingsordningGraphQLFetchers.fetchers())
            }
            .build()

        // Create the executable schema.
        val graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring)

        // Return the GraphQL bean.
        return GraphQL.newGraphQL(graphQLSchema).build()
    }
}

@Introspected
data class Pagination (
    val offset: Int?,
    val limit: Int?,
) {
    fun isSetOrDefaults() = copy(
        offset = offset ?: 0,
        limit = limit ?: 10,
    )

    companion object {
        fun from(args: DataFetchingEnvironment) = (
            args.getArgumentAs("pagination") ?: Pagination(null, null)
        ).isSetOrDefaults()
    }
}

fun <R> fetcher(block: suspend (args: DataFetchingEnvironment) -> R): DataFetcher<R> = DataFetcher { runBlocking(Dispatchers.IO) { block(it) } }

// Used to convert from java hashmaps into kotlin data types
val objectConverter: ObjectMapper = JsonMapper.builderWithJackson2Defaults()
    .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build()

fun ObjectMapper.prettifyJson(s: String?) = writerWithDefaultPrettyPrinter().writeValueAsString(readTree(s))

inline fun <T: DataFetchingEnvironment, reified R> T.getArgumentAs(arg: String): R? {
    return arguments[arg]?.let {  objectConverter.convertValue(it, object : tools.jackson.core.type.TypeReference<R>() {}) }
}
