package pw.mihou.reakt.writable

import pw.mihou.reakt.Reakt

operator fun Reakt.Writable<UByte>.plus(number: UByte): Reakt.Writable<UByte> {
    this.update { (it + number).toUByte() }
    return this
}

operator fun Reakt.Writable<UByte>.plusAssign(number: UByte) {
    this.plus(number)
}

operator fun Reakt.Writable<UByte>.minus(number: UByte): Reakt.Writable<UByte> {
    this.update { (it - number).toUByte() }
    return this
}

operator fun Reakt.Writable<UByte>.minusAssign(number: UByte) {
    this.minus(number)
}

operator fun Reakt.Writable<UByte>.times(number: UByte): Reakt.Writable<UByte> {
    this.update { (it * number).toUByte() }
    return this
}


operator fun Reakt.Writable<UByte>.timesAssign(number: UByte) {
    this.times(number)
}

operator fun Reakt.Writable<UByte>.div(number: UByte): Reakt.Writable<UByte> {
    this.update { (it / number).toUByte() }
    return this
}

operator fun Reakt.Writable<UByte>.divAssign(number: UByte) {
    this.div(number)
}


operator fun Reakt.Writable<UByte>.rem(number: UByte): Reakt.Writable<UByte> {
    this.update { (it % number).toUByte() }
    return this
}

operator fun Reakt.Writable<UByte>.remAssign(number: UByte) {
    this.rem(number)
}

operator fun Reakt.Writable<UByte>.compareTo(number: UByte): Int {
    return this.get().compareTo(number)
}

operator fun Reakt.Writable<UByte>.dec(): Reakt.Writable<UByte> {
    return minus(1.toUByte())
}

operator fun Reakt.Writable<UByte>.inc(): Reakt.Writable<UByte> {
    return plus(1.toUByte())
}