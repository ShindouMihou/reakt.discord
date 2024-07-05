package pw.mihou.reakt.adapters

import org.javacord.api.interaction.InteractionBase
import pw.mihou.reakt.Reakt
import pw.mihou.reakt.ReaktConstructor
import pw.mihou.reakt.SuspendingReaktConstructor
import pw.mihou.reakt.deferrable.ReaktAutoResponse
import pw.mihou.reakt.deferrable.autoDefer
import pw.mihou.reakt.utils.suspend
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

/**
 * An experimental feature to use the new Reakt rendering mechanism to render Discord messages
 * with a syntax similar to a template engine that sports states (writables) that can easily update message
 * upon state changes.
 *
 * This internally uses [autoDefer] to handle sending of the response, ensuring that we can handle long-running renders
 * and others that may happen due to situations such as data fetching, etc.
 *
 * @param ephemeral whether to send the response as ephemeral or not.
 * @param lifetime indicates how long before the [Reakt] instance self-destructs to free up references.
 * @param reakt the entire procedure over how rendering the response works.
 */
@JvmSynthetic
fun <Interaction: InteractionBase> Interaction.R(ephemeral: Boolean, lifetime: Duration = 1.hours, reakt: ReaktConstructor): CompletableFuture<ReaktAutoResponse> {
    val r = Reakt(
        api = this.api,
        user = this.user,
        messageAuthor = null,
        textChannel = this.channel.orElseThrow(),
        Reakt.RenderMode.Interaction,
        lifetime
    )
    return autoDefer(ephemeral) {
        reakt(r)

        return@autoDefer r.message!!
    }.thenApply {
        val message = it.getOrRequestMessage().join()

        r.interactionUpdater = it.updater
        r.acknowledgeUpdate(message)

        return@thenApply it
    }.exceptionally(Reakt::suggestions)
}

@JvmSynthetic
fun <Interaction: InteractionBase> Interaction.AsyncR(ephemeral: Boolean, lifetime: Duration = 1.hours, react: SuspendingReaktConstructor) =
    R(ephemeral, lifetime) { this suspend react }