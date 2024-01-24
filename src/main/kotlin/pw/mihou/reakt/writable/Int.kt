package pw.mihou.reakt.writable

import pw.mihou.reakt.Reakt

operator fun Reakt.Writable<Int>.plus(number: Int): Reakt.Writable<Int> {
    this.update { it + number }
    return this
}

operator fun Reakt.Writable<Int>.plusAssign(number: Int) {
    this.plus(number)
}

operator fun Reakt.Writable<Int>.minus(number: Int): Reakt.Writable<Int> {
    this.update { it - number }
    return this
}

operator fun Reakt.Writable<Int>.minusAssign(number: Int) {
    this.minus(number)
}

operator fun Reakt.Writable<Int>.times(number: Int): Reakt.Writable<Int> {
    this.update { it * number }
    return this
}


operator fun Reakt.Writable<Int>.timesAssign(number: Int) {
    this.times(number)
}

operator fun Reakt.Writable<Int>.div(number: Int): Reakt.Writable<Int> {
    this.update { it / number }
    return this
}

operator fun Reakt.Writable<Int>.divAssign(number: Int) {
    this.div(number)
}


operator fun Reakt.Writable<Int>.rem(number: Int): Reakt.Writable<Int> {
    this.update { it % number }
    return this
}

operator fun Reakt.Writable<Int>.remAssign(number: Int) {
    this.rem(number)
}

operator fun Reakt.Writable<Int>.compareTo(number: Int): Int {
    return this.get().compareTo(number)
}

operator fun Reakt.Writable<Int>.dec(): Reakt.Writable<Int> {
    return minus(1)
}

operator fun Reakt.Writable<Int>.inc(): Reakt.Writable<Int> {
    return plus(1)
}

operator fun Reakt.Writable<Int>.unaryPlus(): Reakt.Writable<Int> {
    this.update { +it }
    return this
}

operator fun Reakt.Writable<Int>.unaryMinus(): Reakt.Writable<Int> {
    this.update { -it }
    return this
}