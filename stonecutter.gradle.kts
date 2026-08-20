plugins {
    id("dev.kikugie.stonecutter")
}

// Версия, которая «активна» в src/ по умолчанию (под неё работает IDE).
// Переключение: ./gradlew "Set active project to <версия>"
// Сборка всех версий сразу: ./gradlew buildAndCollect (jar'ы -> build/libs/<mod.version>/)
// Публикация всех версий на Modrinth: ./gradlew publishMods (нужен env MODRINTH_TOKEN).
stonecutter active "1.21.11"

// Точечные правки исходников под версии, где Mojang переименовал API.
// Базовый src/ пишем под 1.20.6–1.21.11, отличия новых версий описываем здесь.
// Док: https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    replacements {
        // 26.1: классы сообщений чата уехали из net.minecraft.client в подпакет
        // multiplayer.chat, сами имена не поменялись.
        string(current.parsed >= "26.1") {
            replace("net.minecraft.client.GuiMessageTag", "net.minecraft.client.multiplayer.chat.GuiMessageTag")
            replace("net.minecraft.client.GuiMessage", "net.minecraft.client.multiplayer.chat.GuiMessage")
        }
    }
}
