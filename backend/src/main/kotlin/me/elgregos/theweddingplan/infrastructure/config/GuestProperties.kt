package me.elgregos.theweddingplan.infrastructure.config

import me.elgregos.theweddingplan.domain.guest.entity.Language
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.guest")
data class GuestProperties(
    val defaultLanguage: Language = Language.FR,
)

