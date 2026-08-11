package com.tbasic.fitnesstracker.localization

data class SupportedLanguage(
    val code: String,
    val name: String,
    val flag: String
)

val supportedLanguages = listOf(
    SupportedLanguage("en", "English", "🇬🇧"),
    SupportedLanguage("hr", "Hrvatski", "🇭🇷"),
    SupportedLanguage("de", "Deutsch", "🇩🇪"),
    SupportedLanguage("fr", "Français", "🇫🇷"),
    SupportedLanguage("it", "Italiano", "🇮🇹")
)
