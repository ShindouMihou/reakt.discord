package pw.mihou.reakt.elements

import org.javacord.api.entity.message.component.ButtonBuilder
import org.javacord.api.entity.message.component.ButtonStyle
import pw.mihou.reakt.Reakt

fun Reakt.Document.UrlButton(label: String, url: String, emoji: String? = null) = component {
    render {
        // @native directly injects a button into the stack.
        stack {
            val button = ButtonBuilder()
            button.setStyle(ButtonStyle.LINK)
            button.setLabel(label)
            button.setUrl(url)

            if (emoji != null) {
                button.setEmoji(emoji)
            }

            component = button.build()
        }
    }
}() // @note auto-invoke component upon creation.