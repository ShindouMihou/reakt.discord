package pw.mihou.reakt.elements

import org.javacord.api.entity.message.component.ButtonBuilder
import org.javacord.api.entity.message.component.ButtonStyle
import pw.mihou.reakt.Reakt

@Suppress("NAME_SHADOWING")
fun Reakt.Document.UrlButton(label: String, url: String, emoji: String? = null) = component("pw.mihou.reakt.elements.UrlButton") {
    val label = ensureProp<String>("label")
    val url = ensureProp<String>("url")
    val emoji = prop<String>("emoji")
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
}("label" to label, "url" to url, "emoji" to emoji) // @note auto-invoke component upon creation.