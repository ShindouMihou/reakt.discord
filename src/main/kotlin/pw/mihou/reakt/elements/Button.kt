package pw.mihou.reakt.elements

import org.javacord.api.entity.message.component.ButtonBuilder
import org.javacord.api.entity.message.component.ButtonStyle
import org.javacord.api.event.interaction.ButtonClickEvent
import org.javacord.api.listener.interaction.ButtonClickListener
import pw.mihou.reakt.Reakt
import pw.mihou.reakt.uuid.UuidGenerator

fun Reakt.View.PrimaryButton(
    label: String,
    customId: String? = null,
    emoji: String? = null,
    disabled: Boolean = false,
    onClick: ((event: ButtonClickEvent) -> Unit)? = null
) = Button(ButtonStyle.PRIMARY, label, customId, emoji, disabled, onClick)

fun Reakt.View.SecondaryButton(
    label: String,
    customId: String? = null,
    emoji: String? = null,
    disabled: Boolean = false,
    onClick: ((event: ButtonClickEvent) -> Unit)? = null
) = Button(ButtonStyle.SECONDARY, label, customId, emoji, disabled, onClick)

fun Reakt.View.SuccessButton(
    label: String,
    customId: String? = null,
    emoji: String? = null,
    disabled: Boolean = false,
    onClick: ((event: ButtonClickEvent) -> Unit)? = null
) = Button(ButtonStyle.SUCCESS, label, customId, emoji, disabled, onClick)

fun Reakt.View.DangerButton(
    label: String,
    customId: String? = null,
    emoji: String? = null,
    disabled: Boolean = false,
    onClick: ((event: ButtonClickEvent) -> Unit)? = null
) = Button(ButtonStyle.DANGER, label, customId, emoji, disabled, onClick)

fun Reakt.View.Button(
    style: ButtonStyle = ButtonStyle.PRIMARY,
    label: String,
    customId: String? = null,
    emoji: String? = null,
    disabled: Boolean = false,
    onClick: ((event: ButtonClickEvent) -> Unit)? = null
) {
    val button = ButtonBuilder()
    button.setStyle(style)
    button.setLabel(label)

    if (emoji != null) {
        button.setEmoji(emoji)
    }

    button.setDisabled(disabled)

    val uuid = customId ?: run {
        val id = UuidGenerator.request()
        uuids.add(id)
        return@run id
    }
    button.setCustomId(uuid)

    if (onClick != null) {
        reference.listeners += ButtonClickListener {
            if (it.buttonInteraction.customId != uuid) {
                return@ButtonClickListener
            }

            onClick(it)
        }
    }

    reference.components += button.build()
}