package pw.mihou.reakt.writable

import pw.mihou.reakt.Reakt

operator fun Reakt.Writable<Float>.plus(number: Float): Reakt.Writable<Float> {
    this.update { it + number }
    return this
}

operator fun Reakt.Writable<Float>.plusAssign(number: Float) {
    this.plus(number)
}

operator fun Reakt.Writable<Float>.minus(number: Float): Reakt.Writable<Float> {
    this.update { it - number }
    return this
}

operator fun Reakt.Writable<Float>.minusAssign(number: Float) {
    this.minus(number)
}

operator fun Reakt.Writable<Float>.times(number: Float): Reakt.Writable<Float> {
    this.update { it * number }
    return this
}


operator fun Reakt.Writable<Float>.timesAssign(number: Float) {
    this.times(number)
}

operator fun Reakt.Writable<Float>.div(number: Float): Reakt.Writable<Float> {
    this.update { it / number }
    return this
}

operator fun Reakt.Writable<Float>.divAssign(number: Float) {
    this.div(number)
}


operator fun Reakt.Writable<Float>.rem(number: Float): Reakt.Writable<Float> {
    this.update { it % number }
    return this
}

operator fun Reakt.Writable<Float>.remAssign(number: Float) {
    this.rem(number)
}

operator fun Reakt.Writable<Float>.compareTo(number: Float): Int {
    return this.get().compareTo(number)
}

operator fun Reakt.Writable<Float>.dec(): Reakt.Writable<Float> {
    return minus(1.0f)
}

operator fun Reakt.Writable<Float>.inc(): Reakt.Writable<Float> {
    return plus(1.0f)
}

operator fun Reakt.Writable<Float>.unaryPlus(): Reakt.Writable<Float> {
    this.update { +it }
    return this
}

operator fun Reakt.Writable<Float>.unaryMinus(): Reakt.Writable<Float> {
    this.update { -it }
    return this
}