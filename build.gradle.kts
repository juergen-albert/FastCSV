plugins {
    `java-library`
    `maven-publish`
    signing
    checkstyle
    pmd
    jacoco
    id("com.github.spotbugs") version "5.0.14"
    id("me.champeau.jmh") version "0.7.1"
    id("info.solidsoft.pitest") version "1.9.11"
    id("ru.vyarus.animalsniffer") version "1.7.1"
}

group = "de.siegmar"
version = "3.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

sourceSets {
    create("example") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
    create("intTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val intTestImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
}
val intTestRuntimeOnly by configurations.getting

configurations["intTestRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    intTestImplementation(platform("org.junit:junit-bom:5.9.3"))
    intTestImplementation("org.junit.jupiter:junit-jupiter")
    intTestRuntimeOnly("org.junit.platform:junit-platform-launcher")

    signature("com.toasttab.android:gummy-bears-api-33:0.5.1@signature")
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    excludeFilter = file("config/spotbugs/config.xml")
    reports.maybeCreate("xml").required = false
    reports.maybeCreate("html").required = true
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

pitest {
    junit5PluginVersion = "1.1.2"
    targetClasses = setOf("blackbox.*", "de.siegmar.*")
    timestampedReports = false
}

pmd {
    isConsoleOutput = true
    ruleSets = emptyList()
    ruleSetFiles = files("config/pmd/config.xml")
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "1.0".toBigDecimal()
            }
        }
    }
}

tasks.jmh {
    warmupIterations = 2
    iterations = 5
    benchmarkMode = listOf("thrpt")
    fork = 2
    operationsPerInvocation = 1
}

animalsniffer {
    sourceSets = listOf(project.sourceSets.main.get())
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "fastcsv"
            from(components["java"])

            pom {
                name = "FastCSV"
                description = "Ultra fast and simple RFC 4180 compliant CSV library."
                url = "https://github.com/osiegmar/FastCSV"
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://opensource.org/licenses/MIT"
                    }
                }
                scm {
                    url = "https://github.com/osiegmar/FastCSV"
                    connection = "scm:git:https://github.com/osiegmar/FastCSV.git"
                }
                developers {
                    developer {
                        id = "osiegmar"
                        name = "Oliver Siegmar"
                        email = "oliver@siegmar.de"
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "ossrh"
            credentials(PasswordCredentials::class)
            url = if (version.toString().endsWith("SNAPSHOT")) {
                uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            } else {
                uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            }
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}