package pw.mihou.reakt.deferrable

import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.interaction.callback.InteractionMessageBuilderBase

typealias ReaktMessageBuilder = InteractionMessageBuilderBase<*>.() -> Unit

class ReaktMessage internal constructor(
    val ephemeral: Boolean = false,
    val builder: ReaktMessageBuilder = { }
) {
    companion object {
        @JvmStatic
        fun with(ephemeral: Boolean, builder: ReaktMessageBuilder): ReaktMessage {
            return ReaktMessage(ephemeral, builder)
        }

        @JvmStatic
        fun with(builder: ReaktMessageBuilder): ReaktMessage {
            return with(false, builder)
        }

        @JvmStatic
        @JvmOverloads
        fun from(text: String, ephemeral: Boolean = false, builder: ReaktMessageBuilder = {}): ReaktMessage =
            with(ephemeral) {
                setContent(text)
                builder(this)
            }

        @JvmStatic
        @JvmOverloads
        fun from(ephemeral: Boolean = false, builder: ReaktMessageBuilder = {}, vararg embeds: EmbedBuilder): ReaktMessage =
            with(ephemeral) {
                addEmbeds(*embeds)
                builder(this)
            }
    }
    fun <T: InteractionMessageBuilderBase<*>> into(instance: T): T {
        builder(instance)
        return instance
    }
}

@JvmSynthetic
fun EmbedBuilder.toReakt(ephemeral: Boolean = false, builder: ReaktMessageBuilder = {}) = ReaktMessage.from(ephemeral, builder, this)
@JvmSynthetic
fun String.toReakt(ephemeral: Boolean = false, builder: ReaktMessageBuilder = {}) = ReaktMessage.from(this, ephemeral, builder)