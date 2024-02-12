package pw.mihou.reakt.utils

import kotlinx.coroutines.*
import pw.mihou.reakt.Reakt
import pw.mihou.reakt.SuspendingReaktConstructor

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

internal infix fun Reakt.suspend(constructor: SuspendingReaktConstructor) {
    coroutine {
        constructor()
    }
}