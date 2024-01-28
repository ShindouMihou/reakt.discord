package pw.mihou.reakt.elements

import pw.mihou.reakt.Reakt
import pw.mihou.reakt.styles.BodyConstructor

typealias ReaktTextConstructor = Text.() -> Unit
fun Reakt.Document.Text(text: ReaktTextConstructor) = component {
    val constructor =  ensureProp<ReaktTextConstructor>("constructor")
    render {
        // @native directly injects a text element into the stack.
        document.stack {
            val element = Text()
            constructor(element)

            textContent = element.content
        }
    }
}("constructor" to text) // @note auto-invoke component upon creation.

class Text {
    internal var content: String = ""
    fun Body(spaced: Boolean = false, builder: BodyConstructor.() -> Unit) {
        val constructor = BodyConstructor(autoAppendNewLines = spaced)
        builder(constructor)
        content = constructor.content
    }
}