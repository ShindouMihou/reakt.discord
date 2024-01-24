package pw.mihou.reakt.writable

import pw.mihou.reakt.Reakt

operator fun Reakt.Writable<String>.plus(text: String): Reakt.Writable<String> {
    this.update { it + text }
    return this
}
operator fun Reakt.Writable<String>.plusAssign(text: String) {
    this.plus(text)
}