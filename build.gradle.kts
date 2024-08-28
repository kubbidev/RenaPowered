import java.io.ByteArrayOutputStream

plugins {
    id("java")
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    // project settings
    group = "me.kubbidev.renapowered"
    version = "1.1-SNAPSHOT"

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // include source in when publishing
        withSourcesJar()
    }

    repositories {
        mavenCentral()
        mavenLocal()
    }

    dependencies {
        // lombok dependencies & annotation processor
        compileOnly("org.projectlombok:lombok:1.18.32")
        annotationProcessor("org.projectlombok:lombok:1.18.32")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    fun determinePatchVersion(): Int {
        // get the name of the last tag
        val tagInfo = ByteArrayOutputStream()
        exec {
            commandLine("git", "describe", "--tags")
            standardOutput = tagInfo
        }
        val tagString = String(tagInfo.toByteArray())
        if (tagString.contains("-")) {
            return tagString.split("-")[1].toInt()
        }
        return 0
    }

    val majorVersion = "1"
    val minorVersion = "1"
    val patchVersion = determinePatchVersion()
    val projectVersion = "$majorVersion.$minorVersion.$patchVersion"

    // make projectVersion accessible in subprojects
    project.extra["projectVersion"] = projectVersion
}