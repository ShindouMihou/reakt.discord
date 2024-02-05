package pw.mihou.reakt.elements

import pw.mihou.reakt.Reakt
import pw.mihou.reakt.styles.BodyConstructor

typealias ReaktTextConstructor = BodyConstructor.() -> Unit
fun Reakt.Document.Text(spaced: Boolean = true, constructor: ReaktTextConstructor) = component("pw.mihou.reakt.elements.Text") {
    @Suppress("NAME_SHADOWING") val constructor =  ensureProp<ReaktTextConstructor>("constructor")
    render {
        // @native directly injects a text element into the stack.
        document.stack {
            val bodyConstructor = BodyConstructor(autoAppendNewLines = spaced)
            constructor(bodyConstructor)
            textContent = bodyConstructor.content
        }
    }
}("spaced" to spaced, "constructor" to constructor) // @note auto-invoke component upon creation.