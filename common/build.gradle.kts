plugins {
    id("java-library")
}

dependencies {
    api("org.jetbrains:annotations:24.1.0")

    compileOnly(project(":common:loader-utils"))

    compileOnly("org.slf4j:slf4j-api:2.0.13")
    compileOnly("org.apache.logging.log4j:log4j-api:2.17.2")

    api("net.kyori:adventure-api:4.17.0") {
        exclude(module = "adventure-bom")
        exclude(module = "checker-qual")
        exclude(module = "annotations")
    }

    api("net.kyori:adventure-text-serializer-gson:4.17.0") {
        exclude(module = "adventure-bom")
        exclude(module = "adventure-api")
        exclude(module = "gson")
    }

    api("net.kyori:adventure-text-serializer-legacy:4.17.0") {
        exclude(module = "adventure-bom")
        exclude(module = "adventure-api")
    }

    api("net.kyori:adventure-text-serializer-plain:4.17.0") {
        exclude(module = "adventure-bom")
        exclude(module = "adventure-api")
    }

    api("net.kyori:adventure-text-minimessage:4.17.0") {
        exclude(module = "adventure-bom")
        exclude(module = "adventure-api")
    }

    compileOnly("net.dv8tion:JDA:5.1.0") {
        exclude(module = "opus-java")
    }

    api("com.google.code.gson:gson:2.7")
    api("com.google.guava:guava:19.0")

    api("com.github.ben-manes.caffeine:caffeine:2.9.0")
    api("com.squareup.okhttp3:okhttp:3.14.9")
    api("com.squareup.okio:okio:1.17.5")

    api("org.spongepowered:configurate-core:3.7.2") {
        isTransitive = false
    }
    api("org.spongepowered:configurate-yaml:3.7.2") {
        isTransitive = false
    }

    compileOnly("org.mongodb:mongodb-driver-legacy:4.5.0")
    compileOnly("org.yaml:snakeyaml:1.28")
}