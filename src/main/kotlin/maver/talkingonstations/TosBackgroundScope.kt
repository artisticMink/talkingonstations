package maver.talkingonstations

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Background scope for fire & forget tasks.
 */
object TosBackgroundScope {
    private val errorHandler = CoroutineExceptionHandler { _, exception ->
        TosInspector.error("Background task failed: $exception", this::class)
    }
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + errorHandler)
}