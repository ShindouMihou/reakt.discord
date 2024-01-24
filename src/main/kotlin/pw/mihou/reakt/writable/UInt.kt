package pw.mihou.reakt.writable

import pw.mihou.reakt.Reakt

operator fun Reakt.Writable<UInt>.plus(number: UInt): Reakt.Writable<UInt> {
    this.update { it + number }
    return this
}

operator fun Reakt.Writable<UInt>.plusAssign(number: UInt) {
    this.plus(number)
}

operator fun Reakt.Writable<UInt>.minus(number: UInt): Reakt.Writable<UInt> {
    this.update { it - number }
    return this
}

operator fun Reakt.Writable<UInt>.minusAssign(number: UInt) {
    this.minus(number)
}

operator fun Reakt.Writable<UInt>.times(number: UInt): Reakt.Writable<UInt> {
    this.update { it * number }
    return this
}


operator fun Reakt.Writable<UInt>.timesAssign(number: UInt) {
    this.times(number)
}

operator fun Reakt.Writable<UInt>.div(number: UInt): Reakt.Writable<UInt> {
    this.update { it / number }
    return this
}

operator fun Reakt.Writable<UInt>.divAssign(number: UInt) {
    this.div(number)
}


operator fun Reakt.Writable<UInt>.rem(number: UInt): Reakt.Writable<UInt> {
    this.update { it % number }
    return this
}

operator fun Reakt.Writable<UInt>.remAssign(number: UInt) {
    this.rem(number)
}

operator fun Reakt.Writable<UInt>.compareTo(number: UInt): Int {
    return this.get().compareTo(number)
}

operator fun Reakt.Writable<UInt>.dec(): Reakt.Writable<UInt> {
    return minus(1.toUInt())
}

operator fun Reakt.Writable<UInt>.inc(): Reakt.Writable<UInt> {
    return plus(1.toUInt())
}