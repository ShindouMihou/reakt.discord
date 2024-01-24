package pw.mihou.reakt.writable

import pw.mihou.reakt.Reakt

operator fun Reakt.Writable<Byte>.plus(number: Byte): Reakt.Writable<Byte> {
    this.update { (it + number).toByte() }
    return this
}

operator fun Reakt.Writable<Byte>.plusAssign(number: Byte) {
    this.plus(number)
}

operator fun Reakt.Writable<Byte>.minus(number: Byte): Reakt.Writable<Byte> {
    this.update { (it - number).toByte() }
    return this
}

operator fun Reakt.Writable<Byte>.minusAssign(number: Byte) {
    this.minus(number)
}

operator fun Reakt.Writable<Byte>.times(number: Byte): Reakt.Writable<Byte> {
    this.update { (it * number).toByte() }
    return this
}


operator fun Reakt.Writable<Byte>.timesAssign(number: Byte) {
    this.times(number)
}

operator fun Reakt.Writable<Byte>.div(number: Byte): Reakt.Writable<Byte> {
    this.update { (it / number).toByte() }
    return this
}

operator fun Reakt.Writable<Byte>.divAssign(number: Byte) {
    this.div(number)
}


operator fun Reakt.Writable<Byte>.rem(number: Byte): Reakt.Writable<Byte> {
    this.update { (it % number).toByte() }
    return this
}

operator fun Reakt.Writable<Byte>.remAssign(number: Byte) {
    this.rem(number)
}

operator fun Reakt.Writable<Byte>.compareTo(number: Byte): Int {
    return this.get().compareTo(number)
}

operator fun Reakt.Writable<Byte>.dec(): Reakt.Writable<Byte> {
    return minus(1.toByte())
}

operator fun Reakt.Writable<Byte>.inc(): Reakt.Writable<Byte> {
    return plus(1.toByte())
}

operator fun Reakt.Writable<Byte>.unaryPlus(): Reakt.Writable<Byte> {
    this.update { (+it).toByte() }
    return this
}

operator fun Reakt.Writable<Byte>.unaryMinus(): Reakt.Writable<Byte> {
    this.update { (-it).toByte() }
    return this
}