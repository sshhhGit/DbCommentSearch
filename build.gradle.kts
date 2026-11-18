plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "com.sshhhdev.dbcommentsearch"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // 설치된 IntelliJ IDEA Ultimate 2025.3 을 그대로 빌드 대상으로 사용 (IDE 재다운로드 없음)
        local(providers.gradleProperty("localIdePath"))
        // Database Tools 플러그인 (Ultimate 번들)
        bundledPlugin("com.intellij.database")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

intellijPlatform {
    buildSearchableOptions = false
    instrumentCode = false

    pluginConfiguration {
        version = project.version.toString()
        ideaVersion {
            sinceBuild = "253"
            untilBuild = provider { null } // 상한 없음 (2026.3 등에서도 설치 가능)
        }
    }

    // ./gradlew verifyPlugin : 설치된 두 IDE(2025.3, 2026.3)로 호환성 검사
    pluginVerification {
        ides {
            local(file(providers.gradleProperty("localIdePath").get()))
            providers.gradleProperty("verifyIdePath2").orNull?.let { local(file(it)) }
        }
    }
}
