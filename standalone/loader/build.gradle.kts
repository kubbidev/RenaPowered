import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    alias(libs.plugins.shadow)
    id("application")
}

dependencies {
    implementation(project(":common:loader-utils"))
    implementation(project(":standalone:app"))
}

application {
    mainClass = "me.kubbidev.renapowered.standalone.loader.StandaloneLoader"
}

tasks.processResources {
    include("*.xml")
}

tasks.shadowJar {
    archiveFileName = "RenaPowered-Standalone-${project.extra["projectVersion"]}.jar"

    from(project(":standalone").tasks.shadowJar.get().archiveFile)

    transform(Log4j2PluginsCacheFileTransformer())
}

artifacts {
    archives(tasks.shadowJar)
}