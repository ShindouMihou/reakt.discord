package pw.mihou.reakt.elements

import org.javacord.api.entity.message.component.ButtonBuilder
import org.javacord.api.entity.message.component.ButtonStyle
import pw.mihou.reakt.Reakt

fun Reakt.Component.UrlButton(label: String, url: String, emoji: String? = null) {
    val button = ButtonBuilder()
    button.setStyle(ButtonStyle.LINK)
    button.setLabel(label)
    button.setUrl(url)

    if (emoji != null) {
        button.setEmoji(emoji)
    }

    components += button.build()
}