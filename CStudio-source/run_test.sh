kotlinc -cp app/build/tmp/kotlin-classes/debug:$(find /root/.gradle/caches/modules-2/files-2.1 -name "*.jar" | tr '\n' ':') test.kt
java -cp .:app/build/tmp/kotlin-classes/debug:$(find /root/.gradle/caches/modules-2/files-2.1 -name "*.jar" | tr '\n' ':') TestKt
