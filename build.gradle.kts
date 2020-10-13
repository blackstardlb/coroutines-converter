import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.12.0"
    id("com.github.b3er.local.properties") version "1.1"
}

group = "nl.blackstardlb"
version = "1.0"

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
    testImplementation(kotlin("test-junit"))
    testImplementation("io.strikt:strikt-core:0.27.0")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

gradlePlugin {
    plugins {
        create("coroutines-converter") {
            id = "nl.blackstardlb.coroutines-converter"
            implementationClass = "nl.blackstardlb.gradle.coroutinesconverter.CoroutinesConverterPlugin"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "Bintray"
            url = uri("https://api.bintray.com/maven/blackstardlb/CoroutinesConverter/coroutines-converter/;publish=1;override=1")
            credentials {
                username = project.property("bintrayUser")?.toString() ?: System.getenv("BINTRAY_USER")
                password = project.property("bintrayApiKey")?.toString() ?: System.getenv("BINTRAY_KEY")
            }
        }
    }
}

pluginBundle {
    website = "https://github.com/blackstardlb/coroutines-converter"
    vcsUrl = "https://github.com/blackstardlb/coroutines-converter"
    description = "Convert coroutines to futures / reactor for java projects"
    tags = listOf("coroutines", "completablefutures", "reactor")
    plugins {
        getByName("coroutines-converter") {
            displayName = "Coroutines Converter Plugin"
        }
    }
}
