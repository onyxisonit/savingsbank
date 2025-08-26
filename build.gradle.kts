plugins {
    java
    application
}

group = "com.example"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.example.bank.Main")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.example.bank.Main"
    }
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}