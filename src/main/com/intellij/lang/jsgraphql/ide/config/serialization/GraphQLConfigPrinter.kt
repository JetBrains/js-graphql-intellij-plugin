package com.intellij.lang.jsgraphql.ide.config.serialization

import com.intellij.lang.jsgraphql.asSafely
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLConfigKeys
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawConfig
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawProjectConfig
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawSchemaPointer
import com.intellij.openapi.diagnostic.logger
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.nodes.Node
import org.yaml.snakeyaml.nodes.Tag
import org.yaml.snakeyaml.representer.Represent
import org.yaml.snakeyaml.representer.Representer


object GraphQLConfigPrinter {
  private val LOG = logger<GraphQLConfigPrinter>()

  fun toYml(config: GraphQLRawConfig): String? {
    return try {
      val options = DumperOptions().apply {
        indent = 2
        isPrettyFlow = true
        defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
      }
      val yaml = Yaml(ConfigRepresenter(), options)
      yaml.dump(config)
    }
    catch (e: Exception) {
      LOG.warn(e)
      null
    }
  }

  private class ConfigRepresenter : Representer(DumperOptions()) {
    init {
      nullRepresenter = RepresentNull()

      multiRepresenters[List::class.java] = RepresentConfigList()
      multiRepresenters[Map::class.java] = RepresentConfigMap()

      representers[GraphQLRawConfig::class.java] = RepresentConfig()
      representers[GraphQLRawProjectConfig::class.java] = RepresentProjectConfig()
      representers[GraphQLRawSchemaPointer::class.java] = RepresentSchemaPointer()
    }

    private inner class RepresentConfig : Represent {
      override fun representData(data: Any): Node {
        val config = data as GraphQLRawConfig
        val map = mutableMapOf<String, Any?>()

        map[GraphQLConfigKeys.SCHEMA] = config.schema
        map[GraphQLConfigKeys.DOCUMENTS] = config.documents
        map[GraphQLConfigKeys.INCLUDE] = config.include
        map[GraphQLConfigKeys.EXCLUDE] = config.exclude
        map[GraphQLConfigKeys.PROJECTS] = config.projects
        map[GraphQLConfigKeys.EXTENSIONS] = config.extensions

        return this@ConfigRepresenter.representData(map)
      }
    }

    private inner class RepresentProjectConfig : Represent {
      override fun representData(data: Any): Node {
        val config = data as GraphQLRawProjectConfig
        val map = mutableMapOf<String, Any?>()

        map[GraphQLConfigKeys.SCHEMA] = config.schema
        map[GraphQLConfigKeys.DOCUMENTS] = config.documents
        map[GraphQLConfigKeys.INCLUDE] = config.include
        map[GraphQLConfigKeys.EXCLUDE] = config.exclude
        map[GraphQLConfigKeys.EXTENSIONS] = config.extensions

        return this@ConfigRepresenter.representData(map)
      }
    }

    private inner class RepresentSchemaPointer : Represent {
      override fun representData(data: Any): Node {
        val schemaPointer = data as GraphQLRawSchemaPointer
        return if (schemaPointer.headers.isEmpty()) {
          representScalar(Tag.STR, schemaPointer.pattern)
        }
        else {
          representMapping(
            Tag.MAP,
            mapOf(
              schemaPointer.pattern to mapOf(
                GraphQLConfigKeys.HEADERS to schemaPointer.headers,
                GraphQLConfigKeys.INTROSPECT to schemaPointer.introspect,
              )
            ),
            DumperOptions.FlowStyle.BLOCK,
          )
        }
      }
    }

    private inner class RepresentConfigList : Represent {
      override fun representData(data: Any): Node {
        val list = data.asSafely<List<*>>()?.filterNotNull() ?: emptyList()
        return when {
          list.isEmpty() -> this@ConfigRepresenter.representData(null)
          list.size == 1 -> {
            val singleItem = list[0]
            when {
              singleItem is GraphQLRawSchemaPointer && singleItem.headers.isNotEmpty() -> {
                representSequence(
                  getTag(data.javaClass, Tag.SEQ),
                  list,
                  DumperOptions.FlowStyle.AUTO,
                )
              }

              else -> this@ConfigRepresenter.representData(singleItem)
            }
          }

          else -> representSequence(
            getTag(data.javaClass, Tag.SEQ),
            list,
            DumperOptions.FlowStyle.AUTO,
          )
        }
      }
    }

    private inner class RepresentConfigMap : Represent {
      override fun representData(data: Any): Node {
        val map = data as Map<*, *>
        val filtered = map.filterValues {
          when (it) {
            null -> false
            is Collection<*> -> it.isNotEmpty()
            else -> true
          }
        }
        if (filtered.isEmpty()) {
          return this@ConfigRepresenter.representData(null)
        }

        return representMapping(
          getTag(data.javaClass, Tag.MAP),
          filtered,
          DumperOptions.FlowStyle.AUTO,
        )
      }
    }

    private inner class RepresentNull : Represent {
      override fun representData(data: Any?): Node {
        return representScalar(Tag.NULL, "")
      }
    }
  }
}
