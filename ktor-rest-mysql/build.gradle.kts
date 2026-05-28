
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    kotlin("plugin.serialization") version "2.0.21"         
}

group = "com.kotlin"
version = "1.0.0-SNAPSHOT"

application {
    mainClass.set("io.ktor.server.tomcat.jakarta.EngineMain")
    applicationDefaultJvmArgs = listOf(
        "--enable-native-access=ALL-UNNAMED",
        "-Dio.ktor.development=true"
    )    
}


tasks.withType<JavaExec> {
    systemProperty("java.awt.headless", "true")
}

kotlin {
    jvmToolchain(21)
}

// val ktor_version: String by project 
val ktor_version = "3.5.0"

dependencies {
    val exposedVersion = "0.50.0"

    implementation("io.ktor:ktor-server-config-yaml:$ktor_version")
    implementation("io.ktor:ktor-server-config-yaml-jvm:$ktor_version")
    testImplementation("io.ktor:ktor-server-config-yaml-jvm:$ktor_version")
    
    implementation("org.apache.kafka:kafka-clients:3.7.0") 


    implementation("com.zaxxer:HikariCP:5.1.0")    
    implementation("com.mysql:mysql-connector-j:8.3.0")
    
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")    

    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktor_version") 

    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")

    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    // Image Upload
    implementation("io.ktor:ktor-client-core:3.0.0")
    implementation("io.ktor:ktor-client-cio:3.0.0")

    // TOTP
    // implementation("com.github.g0dkar:qrcode-kotlin:4.1.1") 
    implementation("io.github.g0dkar:qrcode-kotlin:4.5.0")    
    implementation("dev.samstevens.totp:totp:1.7.1")    
    implementation("com.google.zxing:core:3.5.3")
    // implementation("com.google.zxing:javase:3.5.3")
    implementation("io.github.g0dkar:qrcode-kotlin-jvm:4.5.0")    

    // Core and JDBC transport are enough for MySQL support
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion") 
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.exposed:exposed-crypt:0.50.0")         
    implementation("org.mindrot:jbcrypt:0.4")

    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.tomcat)
    implementation(libs.logback.classic)

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
}
