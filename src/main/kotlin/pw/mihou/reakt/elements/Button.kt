package pw.mihou.reakt.elements

import org.javacord.api.entity.message.component.ButtonBuilder
import org.javacord.api.entity.message.component.ButtonStyle
import org.javacord.api.event.interaction.ButtonClickEvent
import org.javacord.api.listener.interaction.ButtonClickListener
import pw.mihou.reakt.Reakt
import pw.mihou.reakt.uuid.UuidGenerator

fun Reakt.Document.PrimaryButton(
    label: String,
    customId: String? = null,
    emoji: String? = null,
    disabled: Boolean = false,
    onClick: ((event: ButtonClickEvent) -> Unit)? = null
) = Button(ButtonStyle.PRIMARY, label, customId, emoji, disabled, onClick)

fun Reakt.Document.SecondaryButton(
    label: String,
    customId: String? = null,
    emoji: String? = null,
    disabled: Boolean = false,
    onClick: ((event: ButtonClickEvent) -> Unit)? = null
) = Button(ButtonStyle.SECONDARY, label, customId, emoji, disabled, onClick)

fun Reakt.Document.SuccessButton(
    label: String,
    customId: String? = null,
    emoji: String? = null,
    disabled: Boolean = false,
    onClick: ((event: ButtonClickEvent) -> Unit)? = null
) = Button(ButtonStyle.SUCCESS, label, customId, emoji, disabled, onClick)

fun Reakt.Document.DangerButton(
    label: String,
    customId: String? = null,
    emoji: String? = null,
    disabled: Boolean = false,
    onClick: ((event: ButtonClickEvent) -> Unit)? = null
) = Button(ButtonStyle.DANGER, label, customId, emoji, disabled, onClick)

fun Reakt.Document.Button(
    style: ButtonStyle = ButtonStyle.PRIMARY,
    label: String,
    customId: String? = null,
    emoji: String? = null,
    disabled: Boolean = false,
    onClick: ((event: ButtonClickEvent) -> Unit)? = null
) = component {
    render {
        // @native directly injects a button element into the stack.
        document.stack {
            val button = ButtonBuilder()
            button.setStyle(style)
            button.setLabel(label)

            if (emoji != null) {
                button.setEmoji(emoji)
            }

            button.setDisabled(disabled)

            val uuid = customId ?: run {
                uuid = UuidGenerator.request()
                return@run uuid
            }
            button.setCustomId(uuid)

            if (onClick != null) {
                listener = ButtonClickListener {
                    if (it.buttonInteraction.customId != uuid) {
                        return@ButtonClickListener
                    }

                    onClick(it)
                }
            }

            component = button.build()
        }
    }
}() // @note auto-invoke component upon creation.