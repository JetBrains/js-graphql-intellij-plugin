package com.intellij.lang.jsgraphql.ide.introspection

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.intellij.ide.util.PropertiesComponent
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfigEndpoint
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfigSecurity
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfigSecurity.Companion.getSecurityConfig
import com.intellij.lang.jsgraphql.ide.notifications.handleGenericRequestError
import com.intellij.lang.jsgraphql.types.util.EscapeUtil
import com.intellij.lang.jsgraphql.ui.GraphQLUIProjectService
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.util.net.IdeHttpClientHelpers
import com.intellij.util.net.ssl.CertificateManager.Companion.getInstance
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.conn.ssl.DefaultHostnameVerifier
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.util.PublicSuffixMatcherLoader
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.client.LaxRedirectStrategy
import org.apache.http.util.EntityUtils
import java.io.IOException
import java.security.KeyManagementException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.UnrecoverableKeyException
import java.security.cert.CertificateException
import java.util.concurrent.CancellationException
import javax.net.ssl.HostnameVerifier

@Service(Service.Level.PROJECT)
class GraphQLQueryClient(private val project: Project) {

  companion object {
    private val LOG = logger<GraphQLQueryClient>()

    @JvmStatic
    fun getInstance(project: Project): GraphQLQueryClient = project.service()

    @JvmStatic
    fun parseResponseJsonAsMap(responseJson: String): Map<String, Any?> {
      val response = Gson().fromJson(sanitizeResponseJson(responseJson), Map::class.java)
                     ?: throw JsonSyntaxException("Invalid introspection JSON value")
      @Suppress("UNCHECKED_CAST")
      return response as Map<String, Any?>
    }

    @JvmStatic
    fun parseResponseJson(responseJson: String): JsonObject {
      return Gson().fromJson(sanitizeResponseJson(responseJson), JsonObject::class.java)
             ?: throw JsonSyntaxException("Invalid introspection JSON value")
    }

    /**
     * Ensures that the JSON response falls within the GraphQL specification character range such that it can be expressed as valid GraphQL SDL in the editor
     *
     * @param responseJson the JSON to sanitize
     * @return a sanitized version where the character ranges are within those allowed by the GraphQL Language Specification
     */
    private fun sanitizeResponseJson(responseJson: String): String {
      // Strip out emojis (e.g. the one in the GitHub schema) since they're outside the allowed range
      @Suppress("RegExpDuplicateAlternationBranch")
      return responseJson.replace("[\ud83c\udf00-\ud83d\ude4f]|[\ud83d\ude80-\ud83d\udeff]".toRegex(), "")
    }

    @JvmStatic
    fun prepareQueryPayload(query: String): String =
      """{"query": "${EscapeUtil.escapeJsonString(query)}"}"""
  }

  @JvmOverloads
  fun sendRequest(
    endpoint: GraphQLConfigEndpoint,
    payload: String,
    retryAction: Runnable? = null,
  ): String? {
    return sendRequest(endpoint, createRequest(endpoint, payload, retryAction) ?: return null, retryAction)
  }

  @JvmOverloads
  fun createRequest(
    endpoint: GraphQLConfigEndpoint,
    payload: String,
    retryAction: Runnable? = null,
  ): HttpUriRequest? {
    val url = endpoint.url ?: run { LOG.warn("Endpoint URL is null, unable to create request"); return null }

    return try {
      HttpPost(url).apply {
        entity = StringEntity(payload, ContentType.APPLICATION_JSON)
        GraphQLUIProjectService.setHeadersFromOptions(endpoint, this)
      }
    }
    catch (e: Exception) { // IllegalStateException | IllegalArgumentException
      handleGenericRequestError(project, url, e, NotificationType.ERROR, retryAction)
      null
    }
  }

  @JvmOverloads
  fun sendRequest(
    endpoint: GraphQLConfigEndpoint,
    request: HttpUriRequest,
    retryAction: Runnable? = null,
  ): String? {
    val url = endpoint.url ?: run { LOG.warn("Endpoint URL is null, skipping sending request"); return null }
    val config = endpoint.config
    val sslConfig = if (config != null) getSecurityConfig(config) else null

    return try {
      createHttpClient(url, sslConfig).use { httpClient ->
        httpClient.execute(request).use { response ->
          EntityUtils.toString(response.entity)
        }
      }
    }
    catch (e: CancellationException) {
      throw e
    }
    catch (e: Exception) { // IOException | GeneralSecurityException
      handleGenericRequestError(project, url, e, NotificationType.WARNING, retryAction)
      null
    }
  }

  @Throws(NoSuchAlgorithmException::class, KeyManagementException::class, KeyStoreException::class, IOException::class, UnrecoverableKeyException::class, CertificateException::class)
  fun createHttpClient(url: String, sslConfig: GraphQLConfigSecurity?): CloseableHttpClient =
    HttpClients.custom()
      .setDefaultRequestConfig(createRequestConfig(url))
      .setSSLContext(getInstance().sslContext)
      .setDefaultCredentialsProvider(createCredentialsProvider(url))
      .setRedirectStrategy(LaxRedirectStrategy.INSTANCE)
      .setSSLHostnameVerifier(createHostnameVerifier())
      .apply { GraphQLIntrospectionSSLBuilder.loadCustomSSLConfiguration(sslConfig, this) }
      .build()

  private fun createRequestConfig(url: String): RequestConfig =
    RequestConfig.custom()
      .setConnectTimeout(Registry.intValue("graphql.request.connect.timeout", 5000))
      .setSocketTimeout(Registry.intValue("graphql.request.timeout", 15000))
      .apply { IdeHttpClientHelpers.ApacheHttpClient4.setProxyForUrlIfEnabled(this, url) }
      .build()

  private fun createCredentialsProvider(url: String): CredentialsProvider =
    BasicCredentialsProvider().apply {
      IdeHttpClientHelpers.ApacheHttpClient4.setProxyCredentialsForUrlIfEnabled(this, url)
    }

  private fun createHostnameVerifier(): HostnameVerifier =
    if (PropertiesComponent.getInstance(project).isTrueValue(GraphQLIntrospectionService.GRAPHQL_TRUST_ALL_HOSTS))
      NoopHostnameVerifier.INSTANCE
    else
      DefaultHostnameVerifier(PublicSuffixMatcherLoader.getDefault())
}