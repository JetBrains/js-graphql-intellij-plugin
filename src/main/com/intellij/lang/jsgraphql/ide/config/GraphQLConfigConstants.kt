@file:JvmName("GraphQLConfigConstants")

package com.intellij.lang.jsgraphql.ide.config

const val GRAPHQLCONFIG = ".graphqlconfig"
const val GRAPHQLCONFIG_JSON = ".graphqlconfig.json"
const val GRAPHQLCONFIG_YML = ".graphqlconfig.yml"
const val GRAPHQLCONFIG_YAML = ".graphqlconfig.yaml"

const val GRAPHQLCONFIG_COMMENT = ".graphqlconfig="

val LEGACY_CONFIG_NAMES: Set<String> = linkedSetOf(
    GRAPHQLCONFIG,
    GRAPHQLCONFIG_JSON,
    GRAPHQLCONFIG_YML,
    GRAPHQLCONFIG_YAML,
)

const val GRAPHQL_CONFIG_YML = "graphql.config.yml"
const val GRAPHQL_RC = ".graphqlrc"

val MODERN_CONFIG_NAMES: Set<String> = linkedSetOf(
//    "graphql.config.ts",
//    "graphql.config.js",
//    "graphql.config.cjs",
    "graphql.config.json",
    "graphql.config.yaml",
    GRAPHQL_CONFIG_YML,
//    "graphql.config.toml",
    GRAPHQL_RC,
//    ".graphqlrc.ts",
//    ".graphqlrc.js",
//    ".graphqlrc.cjs",
    ".graphqlrc.json",
    ".graphqlrc.yml",
    ".graphqlrc.yaml",
//    ".graphqlrc.toml",
//    "package.json",
)

val CONFIG_NAMES: Set<String> = LinkedHashSet<String>().apply {
    addAll(MODERN_CONFIG_NAMES)
    addAll(LEGACY_CONFIG_NAMES)
}
