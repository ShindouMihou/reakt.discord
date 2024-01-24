package pw.mihou.reakt.utils

import kotlinx.coroutines.*

private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
fun coroutine(task: suspend () -> Unit) =
    scope.launch {
        try {
            task()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            System.err.println("An uncaught exception was captured by Reakt coroutines.")
            exception.printStackTrace()
        }
    }