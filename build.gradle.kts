/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java Library project to get you started.
 * For more details take a look at the Java Libraries chapter in the Gradle
 * user guide available at https://docs.gradle.org/5.1.1/userguide/java_library_plugin.html
 */

plugins {
    // Apply the java-library plugin to add support for Java Library
    `java-library`
    maven
}
val deployerJars by configurations.creating

repositories {
    // Use jcenter for resolving your dependencies.
    // You can declare any Maven/Ivy/file repository here.
    mavenCentral()
}

dependencies {
    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api("org.apache.commons:commons-math3:3.6.1")
    deployerJars("org.apache.maven.wagon:wagon-ssh:2.2")
    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation("com.google.guava:guava:26.0-jre")

    // Use JUnit test framework
    testImplementation("junit:junit:4.12")
}


tasks.named<Upload>("uploadArchives") {
    repositories.withGroovyBuilder {
        "mavenDeployer" {
            "repository"("url" to "file://localhost/tmp/mixin/")
        }
    }
}
