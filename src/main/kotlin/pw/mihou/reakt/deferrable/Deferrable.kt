package pw.mihou.reakt.deferrable

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.interaction.InteractionBase
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater
import org.javacord.api.util.logging.ExceptionLogger
import pw.mihou.reakt.Reakt
import pw.mihou.reakt.utils.coroutine
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Function

object Deferrable {
    internal fun <Interaction: InteractionBase> autoDefer(
        interaction: Interaction,
        updater: AtomicReference<CompletableFuture<InteractionOriginalResponseUpdater>?>,
        ephemeral: Boolean,
        response: Function<Void?, ReaktMessage>
    ): CompletableFuture<ReaktAutoResponse> {
        var task: Job? = null
        val deferredTaskRan = AtomicBoolean(false)
        if (updater.get() == null) {
            val timeUntil = Instant.now().toEpochMilli() - interaction.creationTimestamp
                .minusMillis(Reakt.autoDeferAfterMilliseconds)
                .toEpochMilli()

            task = coroutine {
                delay(timeUntil)
                if (updater.get() == null) {
                    updater.updateAndGet { interaction.respondLater(ephemeral) }!!.exceptionally(ExceptionLogger.get())
                }
                deferredTaskRan.set(true)
            }
        }
        val future = CompletableFuture<ReaktAutoResponse>()
        coroutine {
            try {
                val message = response.apply(null)
                if (!deferredTaskRan.get() && task != null) {
                    task.cancel()
                }
                @Suppress("NAME_SHADOWING") val updater = updater.get()
                if (updater == null) {
                    val responder = interaction.createImmediateResponder()
                    if (ephemeral) {
                        responder.setFlags(MessageFlag.EPHEMERAL)
                    }
                    message.into(responder).respond()
                        .thenAccept { r -> future.complete(ReaktAutoResponse(r, null)) }
                        .exceptionally { exception ->
                            future.completeExceptionally(exception)
                            return@exceptionally null
                        }
                } else {
                    val completedUpdater = updater.join()
                    message.into(completedUpdater).update()
                        .thenAccept { r -> future.complete(ReaktAutoResponse(completedUpdater, r)) }
                        .exceptionally { exception ->
                            future.completeExceptionally(exception)
                            return@exceptionally null
                        }
                }
            } catch (exception: Exception) {
                future.completeExceptionally(exception)
            }
        }
        return future
    }
}

/**
 * Automatically answers either deferred or non-deferred based on circumstances, to configure the time that it should
 * consider before deferring (this is based on time now - (interaction creation time - auto defer time)), you can
 * modify [pw.mihou.reakt.Reakt.autoDeferAfterMilliseconds].
 *
 * For slash commands and context menus, we recommend using the Nexus event's methods instead which will enable better coordination
 * with middlewares.
 *
 * @param ephemeral whether to respond ephemerally or not.
 * @param response the response to send to Discord.
 * @return the response from Discord.
 */
fun <Interaction: InteractionBase> Interaction.autoDefer(ephemeral: Boolean, response: Function<Void?, ReaktMessage>): CompletableFuture<ReaktAutoResponse> =
    Deferrable.autoDefer(this, AtomicReference(null), ephemeral, response)

data class ReaktAutoResponse internal constructor(
    val updater: InteractionOriginalResponseUpdater,
    val message: Message?
) {
    fun getOrRequestMessage(): CompletableFuture<Message> =
        if (message == null) updater.update()
        else CompletableFuture.completedFuture(message)
}