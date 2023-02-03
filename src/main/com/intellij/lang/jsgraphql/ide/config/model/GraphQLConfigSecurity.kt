package com.intellij.lang.jsgraphql.ide.config.model

import com.intellij.util.asSafely


data class GraphQLConfigSecurity(
    var clientCertificate: GraphQLConfigCertificate? = null,
    var clientCertificateKey: GraphQLConfigCertificate? = null,
) {
    companion object {

        private const val EXTENSION_SSL = "sslConfiguration"

        private const val CLIENT_CERTIFICATE = "clientCertificate"
        private const val CLIENT_CERTIFICATE_KEY = "clientCertificateKey"
        private const val PATH = "path"
        private const val FORMAT = "format"

        private const val UNSUPPORTED_CERT_ERROR = "Unsupported certificate format, only PEM is currently supported"

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun getSecurityConfig(config: GraphQLProjectConfig?): GraphQLConfigSecurity? {
            if (config == null) {
                return null
            }

            val extensions = config.extensions
            val sslExtension =
                extensions[EXTENSION_SSL].asSafely<Map<String, Any?>>()?.takeIf { it.isNotEmpty() } ?: return null

            val sslConfig = GraphQLConfigSecurity()

            val clientCertificate = sslExtension[CLIENT_CERTIFICATE] as Map<String, Any?>?
            if (!clientCertificate.isNullOrEmpty()) {
                sslConfig.clientCertificate = readCertificate(clientCertificate)
            }

            val clientCertificateKey = sslExtension[CLIENT_CERTIFICATE_KEY] as Map<String, Any?>?
            if (!clientCertificateKey.isNullOrEmpty()) {
                sslConfig.clientCertificateKey = readCertificate(clientCertificateKey)
            }

            return sslConfig
        }

        private fun readCertificate(clientCertificate: Map<String, Any?>): GraphQLConfigCertificate {
            return GraphQLConfigCertificate().apply {
                path = clientCertificate[PATH] as String?
                val certFormat = clientCertificate[FORMAT] as String?
                if (certFormat != null && !certFormat.equals(GraphQLConfigCertificate.Encoding.PEM.name, true)) {
                    // TODO: return null and show notification instead
                    throw RuntimeException(UNSUPPORTED_CERT_ERROR)
                }
                format = GraphQLConfigCertificate.Encoding.PEM
            }
        }
    }
}

data class GraphQLConfigCertificate(
    var path: String? = null,
    var format: Encoding = Encoding.PEM,
) {
    enum class Encoding {
        PEM
    }
}
