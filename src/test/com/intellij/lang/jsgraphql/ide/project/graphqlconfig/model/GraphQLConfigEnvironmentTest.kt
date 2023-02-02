/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model

import com.google.gson.Gson
import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLConfigEnvironment
import com.intellij.lang.jsgraphql.ide.config.expandVariables
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawEndpoint
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfigEndpoint
import com.intellij.openapi.project.guessProjectDir
import org.junit.Assert
import java.util.function.Function

class GraphQLConfigEnvironmentTest : GraphQLTestCaseBase() {

    fun testExpandedVariablesLegacy() {
        withCustomEnv {
            val data = GraphQLRawEndpoint("remoteUrl", "http://localhost/")
            val endpoint = GraphQLConfigEndpoint(
                myFixture.project, data, project.guessProjectDir()!!, null, isLegacy = true, isUIContext = false
            )

            // setup env var resolver
            GraphQLConfigEnvironment.getEnvVariable = Function { name: String? -> "$name-value" }

            // verify url with no variables
            Assert.assertEquals("http://localhost/", endpoint.url)

            // add a variable and verify it's expanded
            data.url += "\${env:test-var}"
            Assert.assertEquals("http://localhost/test-var-value", endpoint.url)

            // verify headers as well as nested header objects are expanded
            data.headers = mapOf(
                "boolean" to true,
                "number" to 3.14,
                "auth" to "$ some value before \${env:auth} \${test",
                "nested" to mapOf("nested-auth" to "$ some value before \${env:auth} \${test")
            )
            Assert.assertEquals(
                "{\"boolean\":true,\"number\":3.14,\"auth\":\"$ some value before auth-value \${test\",\"nested\":{\"nested-auth\":\"$ some value before auth-value \${test\"}}",
                Gson().toJson(endpoint.headers)
            )

            // verify that as variables change values, the nested objects are expanded
            // this verifies that the values in the original maps are not overwritten as part of the expansion
            GraphQLConfigEnvironment.getEnvVariable = Function { name: String? -> "$name-new-value" }
            Assert.assertEquals(
                "{\"boolean\":true,\"number\":3.14,\"auth\":\"$ some value before auth-new-value \${test\",\"nested\":{\"nested-auth\":\"$ some value before auth-new-value \${test\"}}",
                Gson().toJson(endpoint.headers)
            )
        }
    }

    fun testInterpolation() {
        withCustomEnv {
            val env = mapOf(
                "HOME" to "/usr/bin",
                "PATH" to """C:\Users\my-path"""
            )

            GraphQLConfigEnvironment.getEnvVariable = Function { name: String? -> env[name] }

            fun test(expected: String, value: String, isLegacy: Boolean) {
                Assert.assertEquals(
                    expected,
                    expandVariables(myFixture.project, value, myFixture.project.guessProjectDir()!!, isLegacy, false)
                )
            }

            test(
                """text${"$"}{unknown}abc_default_value""",
                """text${"$"}{unknown}abc_${"$"}{WITH_DEFAULT:"default_value"}""",
                false,
            )
            test(
                """https:\\google.com""",
                """${"$"}{URL:"https:\\google.com"}""",
                false,
            )
            test(
                """/usr/bin_C:\Users\my-path_some:default:value""",
                """${"$"}{HOME:./dir/test}_${"$"}{PATH}_${"$"}{DEFAULT:some:default:value}""",
                false,
            )
            test(
                """C:\Users\my-path""",
                """${"$"}{PATH}""",
                false,
            )
            test(
                """C:\Users\my-path""",
                """C:\Users\my-path""",
                false,
            )
            test(
                """""",
                """     """,
                false,
            )
            test(
                """abc""",
                """   abc  """,
                false,
            )

            /* Legacy */

            test(
                """/usr/bin_C:\Users\my-path""",
                """${"$"}{env:HOME}_${"$"}{env:PATH}""",
                true,
            )
        }
    }

    private fun withCustomEnv(runnable: Runnable) {
        val before = GraphQLConfigEnvironment.getEnvVariable
        try {
            runnable.run()
        } finally {
            GraphQLConfigEnvironment.getEnvVariable = before
        }
    }
}
