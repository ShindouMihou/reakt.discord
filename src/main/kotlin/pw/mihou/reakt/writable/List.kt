package pw.mihou.reakt.writable

import pw.mihou.reakt.Reakt

operator fun <List, T> Reakt.Writable<List>.plus(element: T): Reakt.Writable<List> where List : MutableCollection<T> {
    this.update {
        it += element
        it
    }
    return this
}

operator fun <List, T> Reakt.Writable<List>.plusAssign(element: T) where List : MutableCollection<T> {
    this.plus(element)
}

operator fun <List, T> Reakt.Writable<List>.minus(element: T): Reakt.Writable<List> where List : MutableCollection<T> {
    this.update {
        it.remove(element)
        it
    }
    return this
}

operator fun <List, T> Reakt.Writable<List>.minusAssign(element: T) where List : MutableCollection<T> {
    this.plus(element)
}

operator fun <List, T> Reakt.Writable<List>.contains(element: T): Boolean where List : Collection<T> {
    return this.get().contains(element)
}

@Suppress("UNCHECKED_CAST")
operator fun <List, T> Reakt.Writable<List>.get(index: Int): T? where List : Collection<T> {
    return when(val collection = this.get()) {
        is kotlin.collections.List<*> -> collection[index] as? T
        is MutableList<*> -> collection[index] as? T
        is Array<*> -> collection[index] as? T
        else -> collection.withIndex().find { (i, _) -> i == index } as? T
    }
}