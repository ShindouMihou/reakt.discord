package pw.mihou.reakt.writable

import pw.mihou.reakt.Reakt

operator fun Reakt.Writable<Long>.plus(number: Long): Reakt.Writable<Long> {
    this.update { it + number }
    return this
}

operator fun Reakt.Writable<Long>.plusAssign(number: Long) {
    this.plus(number)
}

operator fun Reakt.Writable<Long>.minus(number: Long): Reakt.Writable<Long> {
    this.update { it - number }
    return this
}

operator fun Reakt.Writable<Long>.minusAssign(number: Long) {
    this.minus(number)
}

operator fun Reakt.Writable<Long>.times(number: Long): Reakt.Writable<Long> {
    this.update { it * number }
    return this
}


operator fun Reakt.Writable<Long>.timesAssign(number: Long) {
    this.times(number)
}

operator fun Reakt.Writable<Long>.div(number: Long): Reakt.Writable<Long> {
    this.update { it / number }
    return this
}

operator fun Reakt.Writable<Long>.divAssign(number: Long) {
    this.div(number)
}


operator fun Reakt.Writable<Long>.rem(number: Long): Reakt.Writable<Long> {
    this.update { it % number }
    return this
}

operator fun Reakt.Writable<Long>.remAssign(number: Long) {
    this.rem(number)
}

operator fun Reakt.Writable<Long>.compareTo(number: Long): Int {
    return this.get().compareTo(number)
}

operator fun Reakt.Writable<Long>.dec(): Reakt.Writable<Long> {
    return minus(1)
}

operator fun Reakt.Writable<Long>.inc(): Reakt.Writable<Long> {
    return plus(1)
}

operator fun Reakt.Writable<Long>.unaryPlus(): Reakt.Writable<Long> {
    this.update { +it }
    return this
}

operator fun Reakt.Writable<Long>.unaryMinus(): Reakt.Writable<Long> {
    this.update { -it }
    return this
}