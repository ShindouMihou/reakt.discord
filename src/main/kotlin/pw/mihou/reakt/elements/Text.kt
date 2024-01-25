package pw.mihou.reakt.elements

import pw.mihou.reakt.Reakt
import pw.mihou.reakt.styles.BodyConstructor

fun Reakt.Document.Text(text: Text.() -> Unit) = component {
    render {
        // @native directly injects a text element into the stack.
        document.stack {
            val element = Text()
            text(element)

            textContent = element.content
        }
    }
}() // @note auto-invoke component upon creation.

class Text {
    internal var content: String = ""
    fun Body(spaced: Boolean = false, builder: BodyConstructor.() -> Unit) {
        val constructor = BodyConstructor(autoAppendNewLines = spaced)
        builder(constructor)
        content = constructor.content
    }
}