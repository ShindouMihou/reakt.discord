package pw.mihou.reakt.writable

import pw.mihou.reakt.Reakt

operator fun Reakt.Writable<ULong>.plus(number: ULong): Reakt.Writable<ULong> {
    this.update { it + number }
    return this
}

operator fun Reakt.Writable<ULong>.plusAssign(number: ULong) {
    this.plus(number)
}

operator fun Reakt.Writable<ULong>.minus(number: ULong): Reakt.Writable<ULong> {
    this.update { it - number }
    return this
}

operator fun Reakt.Writable<ULong>.minusAssign(number: ULong) {
    this.minus(number)
}

operator fun Reakt.Writable<ULong>.times(number: ULong): Reakt.Writable<ULong> {
    this.update { it * number }
    return this
}


operator fun Reakt.Writable<ULong>.timesAssign(number: ULong) {
    this.times(number)
}

operator fun Reakt.Writable<ULong>.div(number: ULong): Reakt.Writable<ULong> {
    this.update { it / number }
    return this
}

operator fun Reakt.Writable<ULong>.divAssign(number: ULong) {
    this.div(number)
}


operator fun Reakt.Writable<ULong>.rem(number: ULong): Reakt.Writable<ULong> {
    this.update { it % number }
    return this
}

operator fun Reakt.Writable<ULong>.remAssign(number: ULong) {
    this.rem(number)
}

operator fun Reakt.Writable<ULong>.compareTo(number: ULong): Int {
    return this.get().compareTo(number)
}

operator fun Reakt.Writable<ULong>.dec(): Reakt.Writable<ULong> {
    return minus(1.toULong())
}

operator fun Reakt.Writable<ULong>.inc(): Reakt.Writable<ULong> {
    return plus(1.toULong())
}