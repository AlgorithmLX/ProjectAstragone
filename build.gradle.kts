plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

group = "com.algorithmlx"
version = ""

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(rootProject.libs.kotlin.coroutines)

    implementation(rootProject.libs.kotlin.serialization.json)
    implementation(rootProject.libs.kotlin.serialization.core)
    implementation(rootProject.libs.kotlin.datetime)
    implementation(rootProject.libs.ktor.client.core)
    implementation(rootProject.libs.ktor.client.cio)
    implementation(rootProject.libs.ktor.client.okhttp)
    implementation(rootProject.libs.ktor.client.logging)
    implementation(rootProject.libs.ktor.client.content.negotiation)
    implementation(rootProject.libs.ktor.serialization.kotlinx.json)
    implementation(rootProject.libs.telegram.bot)
    implementation(rootProject.libs.logback.classic)
    implementation(rootProject.libs.redis)
    implementation(rootProject.libs.exposed.core)
    implementation(rootProject.libs.exposed.jdbc)
    implementation(rootProject.libs.postgresql)
}

kotlin {
    jvmToolchain(21)
}

val copyDeps by tasks.registering(Copy::class) {
    from(configurations.runtimeClasspath)
    into(layout.buildDirectory.dir("libs/lib"))
}

tasks.named("build").get().finalizedBy(copyDeps)

application.mainClass = "com.algorithmlx.astragone.MainKt"

tasks.jar.get().manifest {
    attributes["Main-Class"] = application.mainClass.get()
    attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(" ") { "lib/${it.name}" }
}
