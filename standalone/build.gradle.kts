plugins {
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":common"))
    compileOnly(project(":common:loader-utils"))
    compileOnly(project(":standalone:app"))

    compileOnly("org.spongepowered:configurate-yaml:3.7.2")
}

tasks.shadowJar {
    archiveFileName = "renapowered-standalone.jarinjar"

    dependencies {
        include(dependency("me.kubbidev.renapowered:.*"))
    }

    relocate("com.github.benmanes.caffeine", "me.kubbidev.renapowered.lib.caffeine")
    relocate("okio", "me.kubbidev.renapowered.lib.okio")
    relocate("okhttp3", "me.kubbidev.renapowered.lib.okhttp3")
    relocate("com.mongodb", "me.kubbidev.renapowered.lib.mongodb")
    relocate("org.bson", "me.kubbidev.renapowered.lib.bson")
    relocate("ninja.leaping.configurate", "me.kubbidev.renapowered.lib.configurate")
    relocate("org.yaml.snakeyaml", "me.kubbidev.renapowered.lib.yaml")
    relocate("net.dv8tion.jda", "me.kubbidev.renapowered.lib.jda")
    relocate("com.neovisionaries.ws.client", "me.kubbidev.renapowered.lib.neovisionaries")
}

artifacts {
    archives(tasks.shadowJar)
}