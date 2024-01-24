package pw.mihou.reakt.elements

import pw.mihou.reakt.Reakt
import pw.mihou.reakt.styles.BodyConstructor

fun Reakt.Component.Text(text: Text.() -> Unit) {
    val element = Text()
    text(element)

    contents = element.content
}

class Text {
    internal var content: String = ""
    fun Body(spaced: Boolean = false, builder: BodyConstructor.() -> Unit) {
        val constructor = BodyConstructor(autoAppendNewLines = spaced)
        builder(constructor)
        content = constructor.content
    }
}