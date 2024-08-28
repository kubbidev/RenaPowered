plugins {
    alias(libs.plugins.blossom)
    id("java-library")
}

dependencies {
    api("org.apache.logging.log4j:log4j-core:2.17.2")
    api("org.apache.logging.log4j:log4j-slf4j-impl:2.17.2")
    api("net.minecrell:terminalconsoleappender:1.3.0")
    api("org.jline:jline-terminal-jansi:3.20.0")

    api("com.google.code.gson:gson:2.9.0")
    api("com.google.guava:guava:31.1-jre")
    api("io.netty:netty-all:4.1.93.Final")

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
    api("net.kyori:adventure-text-serializer-ansi:4.17.0") {
        exclude(module = "adventure-bom")
        exclude(module = "adventure-api")
        exclude(module = "annotations")
    }
    api("net.kyori:ansi:1.0.3") {
        exclude(module = "annotations")
    }
}

blossom {
    replaceTokenIn("src/main/java/me/kubbidev/renapowered/standalone/app/RenaApplication.java")
    replaceToken("@version@", project.extra["projectVersion"])
}