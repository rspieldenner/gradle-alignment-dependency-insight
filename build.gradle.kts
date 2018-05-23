import nebula.test.dependencies.DependencyGraphBuilder
import nebula.test.dependencies.GradleDependencyGenerator
import nebula.test.dependencies.ModuleBuilder

buildscript {
    repositories { jcenter() }
    dependencies {
        classpath("com.netflix.nebula:nebula-test:latest.release")
    }
}

plugins {
    `java-library`
    `maven-publish`
}

group = "com.github.rspieldenner.example"
version = "0.1.0"

repositories {
    jcenter()
    maven("$buildDir/repo/mavenrepo")
}

dependencies {
    implementation("g1:a0:1.0.0")
    implementation("g0:n0:0.2.0")
    components {
        withModule("g0:n0") {
            val version = id.version
            allVariants {
                withDependencyConstraints {
                    add("g0:n1:$version") {
                        because("Aligning with g0:n0 at version: $version")
                    }
                }
            }
        }
        withModule("g0:n1") {
            val version = id.version
            allVariants {
                withDependencyConstraints {
                    add("g0:n0:$version") {
                        because("Aligning with g0:n1 at version: $version")
                    }
                }
            }
        }
    }
}

tasks {
    "genDeps" {
        doLast {
            val graph = DependencyGraphBuilder()
                    .addModule("g0:n0:0.1.0")
                    .addModule("g0:n0:0.2.0")
                    .addModule("g0:n1:0.1.0")
                    .addModule("g0:n1:0.2.0")
                    .addModule(ModuleBuilder("g1", "a0", "1.0.0")
                        .addDependency("g0:n1:0.1.0").build()
            ).build()
            val generator = GradleDependencyGenerator(graph, "$buildDir/repo")
            generator.generateTestMavenRepo()
        }
    }
}

publishing {
    repositories {
        maven {
            // change to point to your repo, e.g. http://my.org/repo
            url = uri("$buildDir/pubrepo")
        }
    }
    (publications) {
        "mavenJava"(MavenPublication::class) {
            from(components["java"])
        }
    }
}
