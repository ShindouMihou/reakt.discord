package pw.mihou.reakt.channels

import pw.mihou.reakt.Reakt

typealias Endpoint<T> = (T) -> Unit

typealias KeyedChannel<T> = Pair<Any, Reakt.Channel<T>>

/**
 * [awaits] subscribes a [Endpoint] to a [KeyedChannel]. This is recommended to use to build
 * more clean and readable code flow. It is recommended to use [routine] to create the [Endpoint]
 * itself with [from] being used to create the [KeyedChannel].
 *
 * @param keyedChannel the key and the channel to subscribe into.
 */
infix fun <T> Endpoint<T>.awaits(keyedChannel: KeyedChannel<T>) {
    val (key, channel) = keyedChannel
    channel.listen(key, this)
}

/**
 * [awaits once] subscribes a [Endpoint] once and only once to a [KeyedChannel]. This is recommended to use to build
 * more clean and readable code flow. It is recommended to use [routine] to create the [Endpoint]
 * itself with [from] being used to create the [KeyedChannel].
 *
 * @param keyedChannel the key and the channel to subscribe into.
 */
infix fun <T> Endpoint<T>.`awaits once`(keyedChannel: KeyedChannel<T>) {
    val (key, channel) = keyedChannel
    channel.once(key, this)
}

/**
 * Sends [T] into the given [keyedChannel] to be enqueued and tasked on all listening
 * endpoints.
 * @param keyedChannel the key and channel to send into.
 */
infix fun <T> T.into(keyedChannel: KeyedChannel<T>) {
    val (key, channel) = keyedChannel
    channel.send(key, this)
}

/**
 * Creates a new [Endpoint] based on the given [endpoint].
 * @param [endpoint] the endpoint to create.
 * @return the same [Endpoint]
 */
fun <T> routine(endpoint: Endpoint<T>): Endpoint<T> = endpoint

/**
 * Creates a new [Reakt.Channel] with the given [T] type.
 * @param T the type to use.
 */
fun <T> channel(): Reakt.Channel<T> = Reakt.Channel()

/**
 * [awaits] subscribes a [Reakt.Writable] state update once and only once to a [KeyedChannel].
 * This is recommended to use to build more clean and readable code flow with [from] being used to create
 * the [KeyedChannel].
 *
 * @param keyedChannel the key and the channel to subscribe into.
 */
infix fun <T> Reakt.Writable<T>.awaits(keyedChannel: KeyedChannel<T>): Reakt.Writable<T> {
    routine<T> { this.set(it) } awaits keyedChannel
    return this
}

/**
 * [awaits once] subscribes a [Reakt.Writable] state update once and only once to a [KeyedChannel].
 * This is recommended to use to build more clean and readable code flow with [from] being used to create
 * the [KeyedChannel].
 *
 * @param keyedChannel the key and the channel to subscribe into.
 */
infix fun <T> Reakt.Writable<T>.`awaits once`(keyedChannel: KeyedChannel<T>): Reakt.Writable<T> {
    routine<T> { this.set(it) } `awaits once` keyedChannel
    return this
}

/**
 * Creates a new [KeyedChannel] with the element being the key.
 * @param channel the channel to use.
 */
infix fun <T> Any.from(channel: Reakt.Channel<T>): KeyedChannel<T> = (this to channel)