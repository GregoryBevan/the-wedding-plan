package me.elgregos.theweddingplan.support

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.core.task.SyncTaskExecutor
import org.springframework.scheduling.annotation.AsyncConfigurer
import java.util.concurrent.Executor

/**
 * Runs `@Async` methods inline on the calling thread during integration tests, so the fire-and-forget
 * playlist sync completes before the endpoint assertion runs. This keeps the tests deterministic while
 * production still offloads the sync to a real task-executor thread.
 */
@TestConfiguration
class SynchronousAsyncConfig : AsyncConfigurer {

    override fun getAsyncExecutor(): Executor = SyncTaskExecutor()
}

