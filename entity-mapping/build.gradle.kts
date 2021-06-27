plugins {
    java
    kotlin("jvm") version "1.4.21"
    kotlin("plugin.allopen") version "1.4.21"
    kotlin("plugin.noarg") version "1.4.21"
}

allOpen {
    annotation("javax.persistence.Entity")
}

noArg {
    annotation("javax.persistence.Entity")
}

group = "com.jayden"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.hibernate:hibernate-entitymanager:5.5.3.Final")
    implementation("com.h2database:h2:1.4.200")
    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
}
