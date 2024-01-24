package pw.mihou.reakt.elements

import pw.mihou.reakt.Reakt
import pw.mihou.reakt.styles.TextStyles

fun Reakt.Component.Text(text: Text.() -> Unit) {
    val element = Text()
    text(element)

    contents = element.content
}

class Text: TextStyles {
    internal var content: String = ""
    fun Body(vararg nodes: String)  {
        content = nodes.joinToString("")
    }
    fun SpacedBody(vararg nodes: String) {
        content = nodes.joinToString("\n")
    }
    fun Body(spaced: Boolean = false, builder: MutableList<String>.() -> Unit) {
        val backing = mutableListOf<String>()
        builder(backing)
        content = if (spaced) backing.joinToString("\n") else backing.joinToString()
    }
}