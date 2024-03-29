import org.jetbrains.changelog.Changelog
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val pluginGroup = prop("pluginGroup")
val pluginVersion = prop("pluginVersion")
val pluginSinceBuild = prop("pluginSinceBuild")
val pluginUntilBuild = prop("pluginUntilBuild")
val platformVersion = prop("platformVersion")
val platformType = prop("platformType")
val platformIdeVersions = prop("platformIdeVersions")

plugins {
    idea
    java
    kotlin("jvm") version "1.8.10"

    id("org.jetbrains.intellij") version "1.13.3"
    id("org.jetbrains.grammarkit") version "2022.3.1"
    id("com.github.ManifestClasspath") version "0.1.0-RELEASE"
    id("org.jetbrains.changelog") version "2.0.0"
}

group = pluginGroup
version = pluginVersion

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

intellij {
    version.set(platformVersion)
    type.set(platformType)
    plugins.set(listOf("JavaScript", "com.intellij.css", "org.intellij.intelliLang", "java", "org.jetbrains.plugins.yaml"))
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
    implementation("com.atlassian.commonmark:commonmark:0.17.0")
    implementation(group = "org.yaml", name = "snakeyaml", version = "1.33")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    implementation("com.graphql-java:java-dataloader:2.2.3")
    implementation("org.reactivestreams:reactive-streams:1.0.4")

    testImplementation(group = "junit", name = "junit", version = "4.13.1")
}

tasks {
    patchPluginXml {
        version.set(pluginVersion)
        sinceBuild.set(pluginSinceBuild)
        untilBuild.set(pluginUntilBuild)

        changeNotes.set(
            """
            ${changelog.renderItem(changelog.get(pluginVersion), Changelog.OutputType.HTML)}
            <br />
            See the <a href="https://github.com/jimkyndemeyer/js-graphql-intellij-plugin/blob/master/CHANGELOG.md">CHANGELOG</a> for more details and history.
            """.trimIndent()
        )
    }

    runIde {
        maxHeapSize = "2g"
    }

    runPluginVerifier {
        ideVersions.set(platformIdeVersions.split(',').map { it.trim() })
    }

    compileJava {
        options.encoding = "UTF-8"
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            languageVersion = "1.8"
            // see https://plugins.jetbrains.com/docs/intellij/using-kotlin.html#kotlin-standard-library
            apiVersion = "1.7"
            freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }
}

fun prop(name: String): String = extra.properties[name] as? String ?: error("Property `$name` is not defined in gradle.properties")
