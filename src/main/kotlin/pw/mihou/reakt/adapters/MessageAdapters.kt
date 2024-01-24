package pw.mihou.reakt.adapters

import pw.mihou.reakt.Reakt

import org.javacord.api.DiscordApi
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.Messageable
import org.javacord.api.event.message.CertainMessageEvent
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

/**
 * An internal extension that acknowledges the message result and calls [Reakt.acknowledgeUpdate].
 * This is used to enable `onUpdate` calls to render.
 *
 * @param react the [Reakt] instance.
 */
private fun CompletableFuture<Message>.ack(react: Reakt): CompletableFuture<Message> {
    return this.thenApply { message ->
        react.acknowledgeUpdate(message)
        return@thenApply message
    }
}

/**
 * An experimental feature to use the new Reakt rendering mechanism to render Discord messages
 * with a syntax similar to a template engine that sports states (writable) that can easily update message
 * upon state changes.
 *
 * @param lifetime the lifetime it takes before the [Reakt] destroys itself.
 * @param react the entire procedure over how rendering the response works.
 */
@JvmSynthetic
fun CertainMessageEvent.R(lifetime: Duration = 1.hours, react: Reakt.() -> Unit): CompletableFuture<Message> {
    val r = Reakt(this.api, Reakt.RenderMode.Message, lifetime)
    react(r)

    return r.messageBuilder!!
        .replyTo(message)
        .send(channel)
        .ack(r)
}

/**
 * An experimental feature to use the new Reakt rendering mechanism to render Discord messages
 * with a syntax similar to a template engine that sports states (writable) that can easily update message
 * upon state changes.
 *
 * @param lifetime the lifetime it takes before the [Reakt] destroys itself.
 * @param react the entire procedure over how rendering the response works.
 */
@JvmSynthetic
fun Messageable.R(api: DiscordApi, lifetime: Duration = 1.hours, react: Reakt.() -> Unit): CompletableFuture<Message> {
    val r = Reakt(api, Reakt.RenderMode.Message, lifetime)
    react(r)

    return r.messageBuilder!!.send(this).ack(r)
}


/**
 * An experimental feature to use the new Reakt rendering mechanism to render Discord messages
 * with a syntax similar to a template engine that sports states (writable) that can easily update message
 * upon state changes.
 *
 * @param lifetime the lifetime it takes before the [Reakt] destroys itself.
 * @param react the entire procedure over how rendering the response works.
 */
fun Message.R(lifetime: Duration = 1.hours, react: Reakt.() -> Unit): CompletableFuture<Message> {
    val r = Reakt(api, Reakt.RenderMode.Message, lifetime)
    react(r)

    r.acknowledgeUpdate(this)
    return r.messageUpdater!!.replaceMessage().ack(r)
}