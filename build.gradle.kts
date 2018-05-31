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

configurations.all {
    resolutionStrategy {
        force("g0:n0:0.2.0")
    }
}

enum class Lifecycle {
    ALIVE,
    DEPRECATED
}

configurations.all {
    withDependencies {
        filter { it is ExternalModuleDependency && it.group == "brokenexample" && it.name == "substitute" && it.version == "1.0.0" }.forEach { dependency ->
            (dependency as ExternalModuleDependency).version {
                prefer("1.0.1")
            }
            dependency.because("Substitution for brokenexample:substitute : changed 1.0.0 with 1.0.1")
        }
    }
}

dependencies {
    implementation("g1:a0:1.0.0")
    implementation("g0:n0:0.1.0")
    implementation("brokenexample:substitute:1.0.0")
    implementation("replacement:umbrella:1.0.0")
    implementation("replacement:align1:1.0.1")
    implementation("replacement:align0:1.1.0")
    
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
        withModule("replacement:align0") {
            val version = id.version
            allVariants {
                withDependencyConstraints {
                    add("replacement:align1:$version") {
                        because("Aligning with replacement:align0 at version: $version")
                    }
                }
            }
        }
        withModule("replacement:align1") {
            val version = id.version
            allVariants {
                withCapabilities {
                    addCapability("replacement", "replaceme", version)
                }
                withDependencyConstraints {
                    add("replacement:align0:$version") {
                        because("Aligning with replacement:align1 at version: $version")
                    }
                }
            }
        }
        withModule("brokenexample:substitute") {
            if (id.version == "1.0.0") {
                allVariants {
                    attributes {
                        attribute(Attribute.of("com.github.rspieldenner.lifecycle", Lifecycle::class.java), Lifecycle.DEPRECATED)
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
                            .addDependency("g0:n1:0.1.0").build())
                    .addModule("brokenexample:substitute:1.0.0")
                    .addModule("brokenexample:substitute:1.0.1")
                    .addModule("replacement:replaceme:1.0.0")
                    .addModule(ModuleBuilder("replacement:umbrella:1.0.0")
                            .addDependency("replacement:replaceme:1.0.0").build())
                    .addModule("replacement:align0:1.0.0")
                    .addModule("replacement:align1:1.0.0")
                    .addModule("replacement:align0:1.0.1")
                    .addModule("replacement:align1:1.0.1")
                    .addModule("replacement:align0:1.1.0")
                    .addModule("replacement:align1:1.1.0")
                    .build()
            val generator = GradleDependencyGenerator(graph, "$buildDir/repo")
            generator.generateTestMavenRepo()
        }
    }

    "compileJava" {
        dependsOn("genDeps")
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
