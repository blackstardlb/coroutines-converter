import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    id("java-gradle-plugin")
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.12.0"
}

group = "nl.blackstardlb"
version = "1.0-SNAPSHOT"

val coroutines_version = "1.3.9"

repositories {
    mavenCentral()
    mavenLocal()
    jcenter()
}

dependencies {
    implementation("com.squareup:kotlinpoet:1.6.0")
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutines_version")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.10")
//    implementation("gradle.plugin.one.chest:maven-publish-plugin:0.0.2")
    testImplementation(kotlin("test-junit"))
    testImplementation("io.strikt:strikt-core:0.27.0")
//    testImplementation("io.ktor:ktor-client-core:1.4.1")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

gradlePlugin {
    plugins {
        create("ktor-client-gen") {
            id = "nl.blackstardlb.coroutines-converter"
            implementationClass = "nl.blackstardlb.gradle.coroutinesconverter.CoroutinesConverterPlugin"
        }
    }
}

pluginBundle {
    website = "http://www.gradle.org/"
    vcsUrl = "https://github.com/gradle/gradle"
    description = "Greetings from here!"
    tags = listOf("greetings", "salutations")

    plugins {
        getByName("ktor-client-gen") {
            displayName = "Gradle Greeting plugin"
        }
    }
}
