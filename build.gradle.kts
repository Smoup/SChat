import me.modmuss50.mpp.ReleaseType
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    // Подбирает совместимый вариант fabric-loom под активную версию MC
    // (один loom не умеет одновременно и 1.20.6, и 26.1).
    id("dev.kikugie.loom-back-compat")
    // Публикация на Modrinth (версия задана в settings.gradle.kts).
    id("me.modmuss50.mod-publish-plugin")
}

// НЕ задавать group вручную (этого требует loom-back-compat).
version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = property("mod.name") as String

// Java: 26.1 требует JDK 25, всё остальное в нашем диапазоне (1.20.6+) — JDK 21.
val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    else -> JavaVersion.VERSION_21
}

// Версионно-специфичная: берём самую раннюю, работающую с этой MC.
// fabric-api не подключаем — мод обходится loader'ом и миксинами, а значит
// запускается с любой версией API и вовсе без неё.
val fabricLoader: String = sc.properties["deps.fabric_loader"]

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    loomx.applyMojangMappings()

    modImplementation("net.fabricmc:fabric-loader:$fabricLoader")
}

loom {
    splitEnvironmentSourceSets()

    mods {
        create("schat") {
            sourceSet(sourceSets["main"])
            sourceSet(sourceSets["client"])
        }
    }

    runConfigs.all {
        preferGradleTask = true
        runDirectory = rootProject.file("run") // общий run-каталог между версиями
    }
    @Suppress("DEPRECATION")
    runConfigs.named("client") {
        programArgs("--username", "Smoup")
    }
}

java {
    withSourcesJar()
    toolchain.languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
}

// Значения вычисляем в project-скоупе: внутри tasks { } property(...) резолвится на task.
val modVersionFull = version.toString()
val modVersion = property("mod.version").toString()
val mcCompat: String = sc.properties["mod.mc_compat"]
val mixinJava = "JAVA_${requiredJava.majorVersion}"
val modJarFile = loomx.modJar.map { it.archiveFile }
val modSourcesJarFile = loomx.modSourcesJar.map { it.archiveFile }

// --- Публикация на Modrinth -------------------------------------------------
// Отдельная Modrinth-версия на каждый MC-узел: game_versions берём из toml.
// Версии — реальные субпроекты (:1.20.6, :1.21.11, ...), поэтому неквалифицированный
// ./gradlew publishMods публикует сразу все. См. шапку stonecutter.gradle.kts.
// --- Деплой в клиент лаунчера ----------------------------------------------
// Задача живёт только в субпроекте 1.21.11, поэтому ./gradlew deployMod собирает
// и кладёт именно эту версию, какая бы ни была активна в stonecutter.
val deployTargetVersion = "1.21.11"
val deployDir: Provider<String> = providers.gradleProperty("deploy.dir").orElse(
    "C:/Users/smoup/AppData/Roaming/.tlauncher/legacy/Minecraft/game/home/Fabric $deployTargetVersion/mods"
)

val modrinthId: String = sc.properties["publish.modrinth_id"]
val publishGameVersionsRaw: String = sc.properties["publish.game_versions"]
val publishGameVersions: List<String> =
    publishGameVersionsRaw.split(' ', ',').filter { it.isNotBlank() }
val currentMc = sc.current.version
val modJarRegularFile = loomx.modJar.flatMap { it.archiveFile }

publishMods {
    file = modJarRegularFile          // несёт зависимость на сборку jar
    type = ReleaseType.STABLE
    modLoaders.add("fabric")
    changelog = providers.provider {
        rootProject.file("CHANGELOG.md").let {
            if (it.exists()) it.readText() else "Release $modVersionFull"
        }
    }
    // Прогон без реальной загрузки: ./gradlew publishMods -Pmodrinth.dryRun=true
    dryRun = providers.gradleProperty("modrinth.dryRun").map { it.toBoolean() }.orElse(false)

    modrinth {
        projectId = modrinthId
        accessToken = providers.gradleProperty("MODRINTH_TOKEN")
            .orElse(providers.environmentVariable("MODRINTH_TOKEN"))
        minecraftVersions.addAll(publishGameVersions)
        version = modVersionFull
        displayName = "SChat $modVersion (MC $currentMc)"
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = requiredJava.majorVersion.toInt()
    }

    // Обе resource-задачи (split source sets: main + client).
    withType<ProcessResources>().configureEach {
        filteringCharset = "UTF-8"

        val modProps = mapOf(
            "version" to modVersionFull,
            "minecraft_version" to mcCompat,
            "loader_version" to fabricLoader,
        )
        modProps.forEach { (k, v) -> inputs.property(k, v) }
        filesMatching("fabric.mod.json") { expand(modProps) }

        // compatibilityLevel миксинов = версия Java под текущую сборку.
        inputs.property("java", mixinJava)
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }
    }

    // Складывает собранные jar'ы всех версий в build/libs/<mod.version>/
    register<Copy>("buildAndCollect") {
        group = "build"
        from(modJarFile, modSourcesJarFile)
        into(rootProject.layout.buildDirectory.dir("libs/$modVersion"))
        dependsOn("build")
    }

    if (currentMc == deployTargetVersion) {
        register("deployMod") {
            group = "build"
            description = "Кладёт jar в папку модов клиента Fabric $deployTargetVersion"
            dependsOn("build")

            val jarFile = modJarRegularFile
            val targetDir = deployDir
            doLast {
                val mods = file(targetDir.get())
                if (!mods.isDirectory) {
                    throw GradleException("Папка модов не найдена: $mods")
                }
                mods.listFiles { file ->
                    file.isFile && file.name.startsWith("SChat-") && file.name.endsWith(".jar")
                }?.forEach { old ->
                    if (!old.delete()) {
                        throw GradleException("Не удалось удалить $old — закрой Minecraft, файл занят игрой")
                    }
                }
                val jar = jarFile.get().asFile
                val target = mods.resolve(jar.name)
                try {
                    jar.copyTo(target, overwrite = true)
                } catch (e: java.io.IOException) {
                    throw GradleException("Не удалось записать $target — закрой Minecraft, файл занят игрой", e)
                }
                logger.lifecycle("SChat -> $target")
            }
        }
    }
}
