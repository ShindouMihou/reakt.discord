package pw.mihou.reakt.writable

import pw.mihou.reakt.Reakt

operator fun Reakt.Writable<Double>.plus(number: Double): Reakt.Writable<Double> {
    this.update { it + number }
    return this
}

operator fun Reakt.Writable<Double>.plusAssign(number: Double) {
    this.plus(number)
}

operator fun Reakt.Writable<Double>.minus(number: Double): Reakt.Writable<Double> {
    this.update { it - number }
    return this
}

operator fun Reakt.Writable<Double>.minusAssign(number: Double) {
    this.minus(number)
}

operator fun Reakt.Writable<Double>.times(number: Double): Reakt.Writable<Double> {
    this.update { it * number }
    return this
}


operator fun Reakt.Writable<Double>.timesAssign(number: Double) {
    this.times(number)
}

operator fun Reakt.Writable<Double>.div(number: Double): Reakt.Writable<Double> {
    this.update { it / number }
    return this
}

operator fun Reakt.Writable<Double>.divAssign(number: Double) {
    this.div(number)
}


operator fun Reakt.Writable<Double>.rem(number: Double): Reakt.Writable<Double> {
    this.update { it % number }
    return this
}

operator fun Reakt.Writable<Double>.remAssign(number: Double) {
    this.rem(number)
}

operator fun Reakt.Writable<Double>.compareTo(number: Double): Int {
    return this.get().compareTo(number)
}

operator fun Reakt.Writable<Double>.dec(): Reakt.Writable<Double> {
    return minus(1.0)
}

operator fun Reakt.Writable<Double>.inc(): Reakt.Writable<Double> {
    return plus(1.0)
}

operator fun Reakt.Writable<Double>.unaryPlus(): Reakt.Writable<Double> {
    this.update { +it }
    return this
}

operator fun Reakt.Writable<Double>.unaryMinus(): Reakt.Writable<Double> {
    this.update { -it }
    return this
}