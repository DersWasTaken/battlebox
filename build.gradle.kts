import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.0"
    kotlin("plugin.serialization") version "1.6.0"

    id("com.github.johnrengelman.shadow") version "7.1.0"

    java
    `maven-publish`
}

group = project.properties["groupId"].toString()
version = project.properties["version"].toString()

val mavenUsername = System.getenv("MAVEN_USERNAME") ?: project.properties["mavenUser"]
val mavenPassword = System.getenv("MAVEN_PASSWORD") ?: project.properties["mavenPassword"]

val mavenCredentials: PasswordCredentials.() -> Unit = {
    username = mavenUsername.toString()
    password = mavenPassword.toString()
}

repositories {
    mavenLocal()
    mavenCentral()

    maven(url = "https://repo.spongepowered.org/maven")
    maven(url = "https://jitpack.io")

    maven("https://repo.astromc.gg/repository/maven-private/") { credentials(mavenCredentials) }
}

dependencies {
    implementation(kotlin("stdlib"))

    compileOnly("com.github.Minestom:Minestom:529_extension_improvement-SNAPSHOT")

    implementation("com.github.Project-Cepi:KStom:main-SNAPSHOT")
    implementation("com.github.CatDevz:SlimeLoader:master-SNAPSHOT")

    implementation("gg.astromc:GameLib:1.0.9-SNAPSHOT")

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-native-mt")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.0-native-mt")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    compileOnly("com.charleskorn.kaml:kaml:0.38.0")

    implementation("com.github.Bloepiloepi:MinestomPvP:874d4048f3")

    testImplementation("io.kotest:kotest-assertions-core:5.0.3")
    testImplementation("io.kotest:kotest-runner-junit5:5.0.3")
}

configurations {
    testImplementation {
        extendsFrom(configurations.compileOnly.get())
    }
}

tasks {
    shadowJar {
        archiveBaseName.set(project.name)
    }

    test { useJUnitPlatform() }
    build { dependsOn(shadowJar) }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = JavaVersion.VERSION_17.toString()
    freeCompilerArgs = listOf("-Xinline-classes", "-Xopt-in=kotlin.RequiresOptIn")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.properties["groupId"].toString()
            artifactId = project.properties["artifactId"].toString()
            version = project.properties["version"].toString()

            from(components["java"])
        }
    }

    repositories {
        maven {
            url = uri("https://maven.pkg.jetbrains.space/astromc/p/astromc/maven-private")
            credentials {
                username = "${System.getenv("JB_SPACE_CLIENT_ID")}"
                password = "${System.getenv("JB_SPACE_CLIENT_SECRET")}"
            }
        }
    }
}
