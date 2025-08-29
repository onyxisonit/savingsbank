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
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
    include("**/TestTest.class", "**/TestSetup.class")
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