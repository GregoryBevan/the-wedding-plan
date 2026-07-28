package me.elgregos.theweddingplan.domain.guest.entity

import java.util.Locale

enum class Language {
    FR,
    EN;

    fun toLocale(): Locale = when (this) {
        FR -> Locale.FRENCH
        EN -> Locale.ENGLISH
    }

    companion object {
        fun fromNullable(value: String?, default: Language): Language =
            value?.trim()?.uppercase()?.let { candidate -> entries.firstOrNull { it.name == candidate } } ?: default
    }
}
