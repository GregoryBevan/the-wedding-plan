package me.elgregos.theweddingplan.infrastructure.shared

import tools.jackson.databind.json.JsonMapper

internal val genericJsonMapper: JsonMapper = JsonMapper.builder().build()
