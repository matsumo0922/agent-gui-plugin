plugins {
    id("java")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.intellij.platform)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    compileOnly(libs.kotlinx.serialization.json)
    implementation(libs.java.diff.utils)
    implementation(libs.kotlinx.collections.immutable)

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    testImplementation("io.mockk:mockk:1.13.16")
    testImplementation("app.cash.turbine:turbine:1.2.0")
    testRuntimeOnly(libs.kotlinx.serialization.json)

    implementation("me.matsumo.claude.agent:agent:local") {
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-serialization-json")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-serialization-json-jvm")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-serialization-core")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-serialization-core-jvm")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core-jvm")
    }

    intellijPlatform {
        intellijIdea("2025.3.3")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.JUnit5)
        composeUI()
        bundledPlugin("org.jetbrains.kotlin")
        bundledModule("intellij.platform.jewel.markdown.core")
        bundledModule("intellij.platform.jewel.markdown.ideLafBridgeStyling")
        bundledModule("intellij.platform.jewel.markdown.extensions.gfmTables")
        bundledModule("intellij.platform.jewel.markdown.extensions.gfmStrikethrough")
        bundledModule("intellij.platform.jewel.markdown.extensions.gfmAlerts")
        bundledModule("intellij.platform.jewel.markdown.extensions.autolink")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "253.31033"
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

    test {
        useJUnitPlatform()
    }
}

// Register a separate unitTest task for pure unit tests (no IntelliJ Platform sandbox)
val unitTest by tasks.registering(Test::class) {
    description = "Runs pure unit tests without IntelliJ Platform test infrastructure."
    group = "verification"

    useJUnitPlatform()

    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath

    // Remove IntelliJ Platform's PathClassLoader and sandbox JVM args
    jvmArgs = jvmArgs.orEmpty().filter {
        !it.contains("PathClassLoader") && !it.contains("idea.home.path")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
