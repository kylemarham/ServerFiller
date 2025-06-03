plugins {
    id("java")
}

group = "me.seetaadev"
version = "1.2"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("com.google.code.gson:gson:2.13.1")
}

tasks.test {
    useJUnitPlatform()
}