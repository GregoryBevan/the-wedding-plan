package me.elgregos.theweddingplan.infrastructure.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Enables Spring's background task features: `@Scheduled` jobs (the daily playlist reconciliation)
 * and `@Async` method execution (the fire-and-forget playlist sync on RSVP submission). `@Async`
 * offloads work to a task-executor thread — the MVC endpoints stay fully synchronous.
 */
@Configuration
@EnableScheduling
@EnableAsync
class TaskConfig

