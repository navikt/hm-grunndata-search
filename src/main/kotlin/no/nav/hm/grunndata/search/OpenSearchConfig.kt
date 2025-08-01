package no.nav.hm.grunndata.search

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import org.apache.hc.client5.http.auth.AuthScope
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials
import org.apache.hc.client5.http.config.ConnectionConfig
import org.apache.hc.client5.http.impl.IdleConnectionEvictor
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder
import org.apache.hc.core5.http.HttpHost
import org.apache.hc.core5.ssl.SSLContextBuilder
import org.apache.hc.core5.util.TimeValue
import org.apache.hc.core5.util.Timeout
import org.opensearch.client.json.jackson.JacksonJsonpMapper
import org.opensearch.client.opensearch.OpenSearchClient
import org.opensearch.client.transport.OpenSearchTransport
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder
import org.slf4j.LoggerFactory


@Factory
class OpenSearchConfig(private val openSearchEnv: OpenSearchEnv, private val objectMapper: ObjectMapper) {

    companion object {
        private val LOG = LoggerFactory.getLogger(OpenSearchConfig::class.java)
    }

    @Singleton
    fun buildOpenSearchClient(connectionManager: PoolingAsyncClientConnectionManager): OpenSearchClient {
        val host = HttpHost.create(openSearchEnv.url)
        val credentialsProvider  = BasicCredentialsProvider()
        credentialsProvider.setCredentials(
            AuthScope(host),
            UsernamePasswordCredentials(openSearchEnv.user, openSearchEnv.password.toCharArray())
        )
        val builder = ApacheHttpClient5TransportBuilder.builder(host)
            .setMapper(JacksonJsonpMapper(objectMapper))
            .setHttpClientConfigCallback { httpClientBuilder: HttpAsyncClientBuilder ->
                httpClientBuilder
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .setConnectionManager(connectionManager)

            }
        val transport: OpenSearchTransport = builder.build()
        val client = OpenSearchClient(transport)
        LOG.info("Opensearch client using ${openSearchEnv.user} and url ${openSearchEnv.url}")
        return client
    }

    @Singleton
    fun connectionManager(): PoolingAsyncClientConnectionManager {
        val sslcontext = if ("https://localhost:9200" == openSearchEnv.url && "admin" == openSearchEnv.user) {
            LOG.warn("Using dev/test sslcontext cause url is ${openSearchEnv.url} and user is ${openSearchEnv.user}")
            SSLContextBuilder
                .create()
                .loadTrustMaterial(
                    null
                ) { chains: Array<X509Certificate?>?, authType: String? -> true }
                .build()
        } else {
            SSLContext.getDefault()
        }
        val tlsStrategy = ClientTlsStrategyBuilder.create()
            .setSslContext(sslcontext)
            .build()
        val connectionConfig = ConnectionConfig.custom()
            .setSocketTimeout(Timeout.ofSeconds(20))
            .setConnectTimeout(Timeout.ofSeconds(10))
            .setTimeToLive(TimeValue.of(5, TimeUnit.MINUTES))
            .build()
        return PoolingAsyncClientConnectionManagerBuilder
            .create()
            .setDefaultConnectionConfig(connectionConfig)
            .setMaxConnTotal(128)
            .setMaxConnPerRoute(128)
            .setTlsStrategy(tlsStrategy)
            .build()
    }

    @Singleton
    fun idleConnectionEvictor(connectionManager: PoolingAsyncClientConnectionManager): IdleConnectionEvictor {
        return IdleConnectionEvictor(
            connectionManager, TimeValue.of(15, TimeUnit.MINUTES)).apply { start() }
    }

}

@ConfigurationProperties("opensearch")
class OpenSearchEnv {
    var user: String = "admin"
    var password: String = "admin"
    var url: String = "https://localhost:9200"
}
