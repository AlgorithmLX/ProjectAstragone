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
    implementation(rootProject.libs.bundles.kotlinx)
    implementation(rootProject.libs.bundles.ktor)
    implementation(rootProject.libs.bundles.exposed)
    implementation(rootProject.libs.bundles.retrofit)
    implementation(rootProject.libs.telegram.bot)
    implementation(rootProject.libs.logback.classic)
    implementation(rootProject.libs.redis)
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
