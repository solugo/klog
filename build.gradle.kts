import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
    id("org.jetbrains.dokka") version "1.6.10"
    id("de.solugo.gitversion") version "1.0.0"
    id("maven-publish")
}

group = "de.solugo.klog"
version = gitVersion.version()

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:1.7.36")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<DokkaTask> {
    logging.captureStandardOutput(LogLevel.ERROR)
}

tasks.create<Jar>("javadocJar") {
    dependsOn("dokkaJavadoc")
    from(tasks.getByName("dokkaJavadoc"))
    archiveClassifier.set("javadoc")

    artifacts.add("archives", this)
}

tasks.create<Jar>("sourcesJar") {
    from(sourceSets.getByName("main").allSource)
    archiveClassifier.set("sources")

    artifacts.add("archives", this)
}

publishing {
    val repositoryUrl = properties["repositoryUrl"]?.toString()
    val repositoryUsername = properties["repositoryUsername"]?.toString()
    val repositoryPassword = properties["repositoryPassword"]?.toString()

    val signingKey = properties["signingKey"]?.toString()
    val signingPassword = properties["signingPassword"]?.toString()

    publications {
        create<MavenPublication>("main") {
            from(components.getByName("kotlin"))

            pluginManager.withPlugin("signing") {
                extensions.configure<SigningExtension> {
                    setRequired { gradle.taskGraph.hasTask("publish") }
                    useInMemoryPgpKeys(signingKey, signingPassword)
                    sign(this@create)
                }
            }

            artifact(tasks.findByName("javadocJar"))
            artifact(tasks.findByName("sourcesJar"))

            pom {
                name.set("$groupId:$artifactId")
                description.set("KLog logging library")
                url.set("https://github.com/solugo/klog")
                developers {
                    developer {
                        name.set("Frederic Kneier")
                        email.set("frederic@kneier.net")
                    }
                }
                scm {
                    url.set("https://github.com/solugo/klog/tree/main")
                    connection.set("https://github.com/solugo/klog.git")
                    developerConnection.set("git@github.com:solugo/klog.git")
                }
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
        }
    }
    repositories {
        if (repositoryUrl != null && repositoryUsername != null && repositoryPassword != null) {
            maven {
                url = uri(repositoryUrl)
                credentials {
                    username = repositoryUsername
                    password = repositoryPassword
                }
            }
        }
    }
}