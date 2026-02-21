plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.intellij.platform")
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("me.matsumo.claude.agent:agent:local") {
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-serialization-json")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-serialization-json-jvm")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-serialization-core")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-serialization-core-jvm")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core-jvm")
    }

    intellijPlatform {
        intellijIdea("2025.2.4")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
        composeUI()
        bundledPlugin("org.jetbrains.kotlin")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "252.25557"
        }

        changeNotes = """
            Initial version
        """.trimIndent()
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
