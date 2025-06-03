plugins {
    `java-library`
}

repositories {
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://nexus.bencodez.com/repository/maven-public/")
    maven("https://nexus.scarsz.me/content/groups/public/")
    maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
    mavenCentral()
}

tasks {
    java {
        toolchain {
            languageVersion.set(
                JavaLanguageVersion.of(
                    "${findProperty("java")}"
                )
            )
        }
    }

    compileJava {
        options.compilerArgs.add("-parameters")
    }
}