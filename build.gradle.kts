val pluginGroup = prop("pluginGroup")
val pluginVersion = prop("pluginVersion")
val pluginSinceBuild = prop("pluginSinceBuild")
val pluginUntilBuild = prop("pluginUntilBuild")
val platformVersion = prop("platformVersion")
val platformType = prop("platformType")

plugins {
    idea
    java
    kotlin("jvm") version "1.6.21"

    id("org.jetbrains.intellij") version "1.7.0"
    id("org.jetbrains.grammarkit") version "2021.2.2"
    id("com.github.ManifestClasspath") version "0.1.0-RELEASE"
    id("org.jetbrains.changelog") version "1.3.1"
}

group = pluginGroup
version = pluginVersion

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

intellij {
    version.set(platformVersion)
    type.set(platformType)
    plugins.set(listOf("JavaScriptLanguage", "CSS", "IntelliLang", "java"))
    ideaDependencyCachePath.set(project.buildDir.absolutePath)
}

sourceSets {
    main {
        java.srcDirs("src/main", "gen")
        resources.srcDir("resources")
    }

    test {
        java.srcDirs("src/test")
        resources.srcDir("test-resources")
    }
}

kotlin {
    sourceSets {
        main {
            kotlin.srcDir("src/main")
        }

        test {
            kotlin.srcDir("src/test")
        }
    }
}

dependencies {
    implementation("commons-io:commons-io:2.7")
    implementation("com.atlassian.commonmark:commonmark:0.12.1")
    implementation(group = "org.yaml", name = "snakeyaml", version = "1.26")
    implementation("fr.opensagres.js:minimatch.java:1.1.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")
    implementation("io.sentry:sentry:5.4.3")

    implementation("com.graphql-java:java-dataloader:2.2.3")
    implementation("org.reactivestreams:reactive-streams:1.0.2")

    testImplementation(group = "junit", name = "junit", version = "4.13.1")
}

tasks {
    patchPluginXml {
        version.set(pluginVersion)
        sinceBuild.set(pluginSinceBuild)
        untilBuild.set(pluginUntilBuild)

        changeNotes.set(
            """
            <h2>New in $pluginVersion</h2>
            ${changelog.get(pluginVersion).toHTML()}
            <br />
            See the <a href="https://github.com/jimkyndemeyer/js-graphql-intellij-plugin/blob/master/CHANGELOG.md">CHANGELOG</a> for more details and history.
            """.trimIndent()
        )
    }

    runIde {
        maxHeapSize = "2g"
    }

    runPluginVerifier {
        ideVersions.set(listOf("IU-2021.2", "IU-2021.3", "IU-2022.1", "IU-2022.2"))
    }

    compileJava {
        options.encoding = "UTF-8"
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "11"
            languageVersion = "1.5"
            apiVersion = "1.5"
            freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }
}

fun prop(name: String): String = extra.properties[name] as? String ?: error("Property `$name` is not defined in gradle.properties")
