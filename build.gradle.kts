plugins {
    id("java")
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    // project settings
    group = "me.kubbidev.renapowered"
    version = "1.0-SNAPSHOT"

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

    // make fullVersion accessible in subprojects
    project.extra["fullVersion"] = "1.0.0"
    project.extra["apiVersion"] = "1.0"
}