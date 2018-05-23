# Gradle Alignment Dependency Insight Testing

run `./gradlew dependencyInsight --configuration compileClasspath --dependency n1`

see

```
> Task :dependencyInsight
g0:n1:0.2.0 (via constraint, Aligning with g0:n0 at version: 0.2.0)
   variant "runtime+master+default" [
      Requested attributes not found in the selected variant:
         org.gradle.usage = java-api
   ]
\--- g0:n0:0.2.0
     +--- compileClasspath
     \--- g0:n1:0.2.0 (*)

g0:n1:0.1.0 -> 0.2.0
   variant "runtime+master+default" [
      Requested attributes not found in the selected variant:
         org.gradle.usage = java-api
   ]
\--- g1:a0:1.0.0
     \--- compileClasspath

```


run `./gradlew dependencyInsight --configuration compileClasspath --dependency n0`

see

```
> Task :dependencyInsight
g0:n0:0.2.0
   variant "default" [
      Requested attributes not found in the selected variant:
         org.gradle.usage = java-api
   ]
+--- compileClasspath
\--- g0:n1:0.2.0
     +--- g1:a0:1.0.0
     |    \--- compileClasspath
     \--- g0:n0:0.2.0 (*)

```