plugins {
    id("project.common-conventions")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

tasks {
    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")
    }

    named<DefaultTask>("build") {
        dependsOn("shadowJar")
    }
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
}