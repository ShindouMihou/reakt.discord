package pw.mihou.reakt.writable

import pw.mihou.reakt.Reakt

operator fun Reakt.Writable<Short>.plus(number: Short): Reakt.Writable<Short> {
    this.update { (it + number).toShort() }
    return this
}

operator fun Reakt.Writable<Short>.plusAssign(number: Short) {
    this.plus(number)
}

operator fun Reakt.Writable<Short>.minus(number: Short): Reakt.Writable<Short> {
    this.update { (it - number).toShort() }
    return this
}

operator fun Reakt.Writable<Short>.minusAssign(number: Short) {
    this.minus(number)
}

operator fun Reakt.Writable<Short>.times(number: Short): Reakt.Writable<Short> {
    this.update { (it * number).toShort() }
    return this
}


operator fun Reakt.Writable<Short>.timesAssign(number: Short) {
    this.times(number)
}

operator fun Reakt.Writable<Short>.div(number: Short): Reakt.Writable<Short> {
    this.update { (it / number).toShort() }
    return this
}

operator fun Reakt.Writable<Short>.divAssign(number: Short) {
    this.div(number)
}


operator fun Reakt.Writable<Short>.rem(number: Short): Reakt.Writable<Short> {
    this.update { (it % number).toShort() }
    return this
}

operator fun Reakt.Writable<Short>.remAssign(number: Short) {
    this.rem(number)
}

operator fun Reakt.Writable<Short>.compareTo(number: Short): Int {
    return this.get().compareTo(number)
}

operator fun Reakt.Writable<Short>.dec(): Reakt.Writable<Short> {
    return minus(1)
}

operator fun Reakt.Writable<Short>.inc(): Reakt.Writable<Short> {
    return plus(1)
}

operator fun Reakt.Writable<Short>.unaryPlus(): Reakt.Writable<Short> {
    this.update { (+it).toShort() }
    return this
}

operator fun Reakt.Writable<Short>.unaryMinus(): Reakt.Writable<Short> {
    this.update { (-it).toShort() }
    return this
}