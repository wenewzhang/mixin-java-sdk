# mixin-java-sdk

## Upload library to server through scp
->build.gradle.kts
```kotlin
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

  tasks.getByName<Upload>("uploadArchives") {
      repositories.withGroovyBuilder {
      "mavenDeployer" {
          setProperty("configuration", deployerJars)
          "repository"("url" to "scp://server/home/mixin") {
              "authentication"("userName" to "mixin", "password" to "password")
          }
      }
      }
  }
```
wagon-ssh use ssh-rsa-cert algorithm for accept hosts,delete the ip from known_hosts;
generate a new one
```bash
//delete
vi ~/.ssh/known_hosts
ssh -o HostKeyAlgorithms=ssh-rsa-cert-v01@openssh.com,ssh-rsa server_ip
```
