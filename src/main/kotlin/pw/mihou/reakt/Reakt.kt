package pw.mihou.reakt

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.javacord.api.DiscordApi
import org.javacord.api.entity.intent.Intent
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.MessageBuilder
import org.javacord.api.entity.message.MessageUpdater
import org.javacord.api.entity.message.component.ActionRow
import org.javacord.api.entity.message.component.LowLevelComponent
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater
import org.javacord.api.listener.GloballyAttachableListener
import org.javacord.api.listener.message.MessageDeleteListener
import org.javacord.api.util.event.ListenerManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.helpers.NOPLogger
import pw.mihou.reakt.channels.Endpoint
import pw.mihou.reakt.deferrable.ReaktMessage
import pw.mihou.reakt.exceptions.NoRenderFoundException
import pw.mihou.reakt.exceptions.PropTypeMismatch
import pw.mihou.reakt.exceptions.UnknownPropException
import pw.mihou.reakt.logger.LoggingAdapter
import pw.mihou.reakt.logger.adapters.DefaultLoggingAdapter
import pw.mihou.reakt.logger.adapters.FastLoggingAdapter
import pw.mihou.reakt.utils.coroutine
import pw.mihou.reakt.uuid.UuidGenerator
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import kotlin.reflect.KProperty
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

typealias Subscription<T> = (oldValue: T, newValue: T) -> Unit
typealias PlainSubscription = () -> Unit
typealias Unsubscribe = () -> Unit

typealias RenderSubscription = () -> Unit
typealias DestroySubscription = () -> Unit
typealias UpdateSubscription = (message: Message) -> Unit

typealias ComponentBeforeMountSubscription = () -> Unit
typealias ComponentAfterMountSubscription = () -> Unit
typealias ReactView = Reakt.View.() -> Unit
typealias ReactDocument = Reakt.Document.() -> Unit

typealias Derive<T, K> = (T) -> K

typealias Props = Map<String, Any>
typealias ComponentConstructor = Reakt.Component.() -> Unit

/**
 * [Reakt] is the React-Svelte inspired method of rendering (or sending) messages as response to various scenarios such
 * as message commands, slash commands, context menus and different kind of magic. We recommend using the available
 * `event.R` method instead as it is mostly designed to enable this to work for your situation, or instead use the
 * available `interaction.R` method for interactions.
 */
class Reakt internal constructor(private val api: DiscordApi, private val renderMode: RenderMode, private val lifetime: Duration = 1.days) {
    private var rendered: Boolean = false

    internal var message: ReaktMessage? = null
    internal var messageBuilder: MessageBuilder? = null
    internal var messageUpdater: MessageUpdater? = null
    internal var interactionUpdater: InteractionOriginalResponseUpdater? = null

    private var unsubscribe: Unsubscribe = {}

    private var render: ReactDocument? = null
    private var document: Document? = Document()

    private var debounceTask: Job? = null
    private var mutex = ReentrantLock()

    internal var resultingMessage: Message? = null

    private var firstRenderSubscribers = mutableListOf<RenderSubscription>()
    private var renderSubscribers = mutableListOf<RenderSubscription>()

    private var updateSubscribers = mutableListOf<UpdateSubscription>()
    private var expansions = mutableListOf<Unsubscribe>()

    private var destroyJob = if (lifetime.isInfinite()) null else coroutine {
        delay(lifetime.inWholeMilliseconds)
        destroy()
    }

    private var messageDeleteListenerManager: ListenerManager<MessageDeleteListener>? = null
    private var destroySubscribers = mutableListOf<DestroySubscription>()

    companion object {
        /**
         * Defines how long we should wait before proceeding to re-render the view, this is intended to ensure
         * that all other states being changed in that period is applied as well, preventing multiple unnecessary re-renders
         * which can be costly as we send HTTP requests to Discord.
         *
         * As a default, we recommend within 25ms to 250ms depending on how long the betweens of your state changes are,
         * you can go even lower as long as the states are being changed immediately, otherwise, keeping it as default
         * is recommended.
         */
        var debounceMillis = 25L

        /**
         * An adapter to help Reakt adopt the same way of logging that your application does.
         *
         * You can leave this as default if you are using an SLF4J-based logger such as Logback or
         * if you are using a Log4j logger but with a SLF4J bridge as the default logging adapter uses SLF4J.
         */
        @Volatile var logger: LoggingAdapter =
            if (LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) == null || LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) is NOPLogger) FastLoggingAdapter
            else DefaultLoggingAdapter()

        /**
         * When in an automatic defer situation, the framework will automatically defer the response when the time has
         * surpassed the specified amount. You can specify this to any value less than 3,000 but the default value should
         * be more than enough even when considering network latencies.
         */
        @JvmField @Volatile var autoDeferAfterMilliseconds: Long = 2350
    }

    internal enum class RenderMode {
        Interaction,
        Message,
        UpdateMessage
    }

    /**
     * Subscribes a task to be ran whenever the view is being rendered, this happens on the initial render
     * and re-renders but takes a lower priority than the ones in [onInitialRender].
     * @param subscription the subscription to execute on render.
     */
    fun onRender(subscription: RenderSubscription) {
        renderSubscribers.add(subscription)
    }

    /**
     * Subscribes a task to be ran whenever the view is being rendered. This happens first before the actual
     * view being rendered, therefore, you can use this to load data before the view is actually rendered.
     * @param subscription the subscription to execute on first render.
     */
    fun onInitialRender(subscription: RenderSubscription) {
        firstRenderSubscribers.add(subscription)
    }

    /**
     * Subscribes a task to be ran whenever the message itself updates, this can be when a re-render
     * is successfully acknowledged by Discord, or when the message was sent for the first time.
     *
     * Not to be confused with [onRender] which executes before the message is updated and even before
     * the message is rendered.
     *
     * @param subscription the subscription to execute on update.
     */
    fun onUpdate(subscription: UpdateSubscription) {
        updateSubscribers.add(subscription)
    }

    /**
     * Subscribes a task to be ran when the [Reakt] instance is destroyed. This can be when the lifetime
     * ends or the message is deleted. This is ran all synchronous and will block the destruction thread
     * until it completes.
     *
     * @param subscription the subscription to execute.
     */
    fun onDestroy(subscription: DestroySubscription) {
        destroySubscribers.add(subscription)
    }

    /**
     * An internal function to update the [resultingMessage] and run all the [updateSubscribers].
     * @param message the message resulting from a render.
     */
    internal fun acknowledgeUpdate(message: Message) {
        this.resultingMessage = message
        if (api.intents.contains(Intent.GUILD_MESSAGES) || api.intents.contains(Intent.DIRECT_MESSAGES)) {
            messageDeleteListenerManager?.remove()
            messageDeleteListenerManager = this.resultingMessage?.run {
                api.addMessageDeleteListener {
                    destroy()
                }
            }
        }
        updateSubscribers.forEach {
            try {
                it(message)
            } catch (err: Exception) {
                logger.error("An uncaught exception was received by Reakt's update subscription dispatcher with the following stacktrace.", err)
            }
        }
    }

    /**
     * Destroys any references to this [Reakt] instance. It is recommended to do this when
     * you no longer need the interactivity as this will free up a ton of unused memory that should've
     * been free.
     */
    fun destroy() {
        synchronized(destroySubscribers) {
            destroySubscribers.forEach(DestroySubscription::invoke)

            unsubscribe()
            document = null
            render = null

            this.unsubscribe = {}
            this.destroySubscribers = mutableListOf()
            this.resultingMessage = null
            this.interactionUpdater = null
            this.updateSubscribers = mutableListOf()
            this.messageUpdater = null
            this.messageBuilder = null
            this.renderSubscribers = mutableListOf()
            this.message = null
            this.debounceTask = null
            this.destroyJob = null
            this.expansions.forEach(Unsubscribe::invoke)
            this.expansions = mutableListOf()
            this.messageDeleteListenerManager?.remove()
            this.messageDeleteListenerManager = null
        }
    }

    /**
     * Renders the given view, this will also be used to re-render the view onwards. Note that using two
     * renders will result in the last executed render being used.
     * @param renderer the renderer to use.
     */
    fun render(renderer: ReactDocument) {
        try {
            val element = apply(renderer)
            when (renderMode) {
                RenderMode.Interaction -> {
                    val (unsubscribe, message) = element.render(api)
                    this.message = message
                    this.unsubscribe = unsubscribe
                }

                RenderMode.Message -> {
                    val builder = MessageBuilder()
                    val unsubscribe = element.render(builder, api)

                    this.messageBuilder = builder
                    this.unsubscribe = unsubscribe
                }
                RenderMode.UpdateMessage -> {
                    if (resultingMessage == null) {
                        throw IllegalStateException("Updating a message with React needs `resultingMessage` to not be null.")
                    }
                    val updater = MessageUpdater(resultingMessage)
                    val unsubscribe = element.render(updater, api)

                    this.messageUpdater = updater
                    this.unsubscribe = unsubscribe
                }
            }
            this.rendered = true
        } catch (err: Exception) {
            logger.error("An uncaught exception was received by Reakt's renderer with the following stacktrace.", err)
        }
    }

    private fun flatten(document: Document, into: Document) {
        for (component in document.components) {
            if (!component.hasRenderedOnce) {
                component.rerender()
            }

            into.stack += component.document.stack
            if (component.document.components.isNotEmpty()) {
                flatten(component.document, into)
            }
        }
    }

    private fun apply(renderer: ReactDocument): View {
        this.render = renderer

        if (!rendered) {
            firstRenderSubscribers.forEach {
                try {
                    it()
                } catch (err: Exception) {
                    logger.error("An uncaught exception was received by Reakt's initial render subscription dispatcher with the following stacktrace.", err)
                }
            }
        }

        renderSubscribers.forEach {
            try {
                it()
            } catch (err: Exception) {
                logger.error("An uncaught exception was received by Reakt's render subscription dispatcher with the following stacktrace.", err)
            }
        }

        val oldDocument = this.document ?: Document()
        val oldComponents = oldDocument.copyComponents()

        val newDocument = Document()
        renderer(newDocument)

        val composition = Document()
        val newComponents = newDocument.copyComponents()

        // @note list of components that were re-rendered, so we can reference it when there are duplicates.
        val renderedComponents = mutableListOf<Component>()

        // @question how do we differentiate two components that are same type, but actually different.
        // @question how do we know that the two components are the same?
        // @question we could iterate by index, but that means if the component's index changes, there are no equivalents.
        for (component in newComponents) {
            var document: Document? = null
            for (component1 in renderedComponents) {
                val equivalent = component.compare(component1)
                if (equivalent) {
                    document = component1.document
                    break
                }
            }

            // @note only iterate older components when Reakt re-rendered.
            if (rendered) {
                for (component1 in oldComponents) {
                    val equivalent = component.compare(component1)
                    if (equivalent) {
                        document = component1.document
                        break
                    }
                }
            }

            if (document == null) {
                if (!component.hasRenderedOnce) {
                    component.rerender()
                }

                document = component.document
                renderedComponents += component
            } else {
                // ensure that we swap the document inside the component
                // so that the next rerender will utilize this new document.
                component.document = document
            }

            composition.components += component
            flatten(document, composition)
        }

        this.document = composition

        val element = View()
        for (stackElement in composition.stack) {
            stackElement.render(element)
        }
        return element
    }

    /**
     * Creates a [Writable] that can react to changes of the value, allowing you to re-render the message
     * with the new states. Internally, this simply creates a [Writable] then adds a subscriber to re-render
     * the message whenever the state changes (debounced by [Reakt.debounceMillis] milliseconds).
     *
     * We recommend using [Writable]'s constructor and then using [Reakt.expand] to add the subscriber whenever you need
     * to create a [Writable] outside of the [Reakt] scope. (View the source code of this method to see how it looks).
     *
     * To pass a [Writable], we recommend creating another variable that has the [Writable] itself as the value and another
     * one that uses `by` and then passing the [Writable] instead. (Refer to the wiki for this, as function parameters are not mutable
     * and delegated variables pass their [Writable.getValue] result, so changes cannot be listened).
     *
     * @param value the initial value.
     * @return a [Writable] with a [Subscription] that will re-render the [ReactView] when the state changes.
     */
    fun <T> writable(value: T): Writable<T> {
        val element = Writable(value)
        return expand(element)
    }

    /**
     * Adds a [Subscription] that enables the [ReactView] to be re-rendered whenever the value of the [Writable]
     * changes, this is what [writable] uses internally to react to changes.
     * @param writable the writable to subscribe.
     * @return the [Writable] with the re-render subscription attached.
     */
    fun <T> expand(writable: Writable<T>): Writable<T> {
        val stateUnsubscribe = writable.subscribe { _, _ ->
            try {
                if (!mutex.tryLock()) return@subscribe
                val render = this.render ?: return@subscribe
                debounceTask?.cancel()
                debounceTask = coroutine {
                    delay(debounceMillis)
                    this.unsubscribe()

                    debounceTask = null
                    if (this.document == null) return@coroutine
                    val interactionUpdater = interactionUpdater
                    if (interactionUpdater != null) {
                        val view = apply(render)
                        this.unsubscribe = view.render(interactionUpdater, api)
                        interactionUpdater.update().exceptionally {
                            logger.error("Failed to re-render message using Reakt with the following stacktrace.", it)
                            return@exceptionally null
                        }.thenAccept(::acknowledgeUpdate)
                    } else {
                        val message = resultingMessage
                        if (message != null) {
                            val updater = message.createUpdater()
                            val view = apply(render)
                            this.unsubscribe = view.render(updater, api)
                            updater.replaceMessage().exceptionally {
                                logger.error("Failed to re-render message using Reakt with the following stacktrace.", it)
                                return@exceptionally null
                            }.thenAccept(::acknowledgeUpdate)
                        }
                    }

                    destroyJob?.cancel()
                    destroyJob = if (lifetime.isInfinite()) null else coroutine {
                        delay(lifetime.inWholeMilliseconds)
                        destroy()
                    }
                }
                mutex.unlock()
            } catch (err: Exception) {
                logger.error("Failed to re-render message using Reakt with the following stacktrace.", err)
            }
        }
        expansions.add(stateUnsubscribe)
        return writable
    }

    /**
     * Creates a new [ReadOnly] state that has a value derived of this [Writable], which means that the value
     * of the new [ReadOnly] state changes whenever the value of the current [Writable] changes.
     *
     * Note: This does not inherit the subscriptions of the [Writable].
     *
     * Different from [Writable.derive] itself, this has the re-render subscription which will make changes to the
     * origin [Writable] also signal the system to re-render. This is intended to be used for cases where the origin
     * [Writable] is not subscribed to the re-render subscription, for some reason.
     *
     * @param modifier the action to do to mutate the value into the desired value.
     * @return a new [ReadOnly] state that is derived from the current [Writable].
     */
    fun <T, K> derive(writable: Writable<T>, modifier: Derive<T, K>) = writable.derive(modifier).apply {
        expand(this.writable)
    }

    /**
     * React [Channel] is a special FIFO (First In, First Out) linked queue-based tool that is best
     * applied with natives such as [Writable]. It enables developers to write better reactive code
     * outside the [Reakt] scope.
     */
    class Channel<T> {
        private val levers: MutableMap<Any, Lever<T>> = mutableMapOf()

        internal class Lever<T> internal constructor() {
            private val queue: ConcurrentLinkedQueue<T> = ConcurrentLinkedQueue()
            internal val endpoints: CopyOnWriteArrayList<Endpoint<T>> = CopyOnWriteArrayList()
            private var lock = ReentrantLock()

            fun send(element: T) {
                queue.add(element)
                process()
            }

            internal fun process() = coroutine {
                val locked = lock.tryLock()
                if (!locked) return@coroutine
                while(queue.isNotEmpty()) {
                    val element = queue.poll()
                    if (element != null) {
                        // synchronous wait
                        for (endpoint in endpoints) {
                            endpoint(element)
                        }
                    }
                }

                lock.unlock()
            }

            internal fun close() = coroutine {
                endpoints.clear()
            }
        }

        private fun lever(key: Any) =  levers.computeIfAbsent(key) { Lever() }

        /**
         * [send] enqueues an element update to a specific [key] queue. Each [key] queue
         * has their own thread and queue, allowing [Channel] to remain thread-safe and be
         * a single instance for each big task.
         *
         * @param key the name of the queue task.
         * @param element the element to enqueue.
         */
        fun send(key: Any, element: T) {
            lever(key).send(element)
        }

        /**
         * [close] closes the [key] queue, disallowing any further processing from happening
         * within that queue instance, it will process all remaining events and then detaches
         * all endpoints.
         *
         * @param key the queue to close.
         */
        fun close(key: Any) {
            levers[key]?.let {
                it.process() // locks first to process remaining entities (if not already running)
                it.close()   // locks second to remove all endpoints
            }
            levers.remove(key)
        }

        /**
         * [listen] subscribes an [Endpoint] into a [key] endpoints that will be triggered synchronous
         * with each update. Each [Endpoint] is executed synchronously one-by-one for each [key], therefore,
         * to prevent slowdowns, each [Endpoint] shouldn't be blocking to prevent the [key] queue from being
         * blocked.
         *
         * @param key the name of the queue task.
         * @param endpoint the endpoint to subscribe.
         */
        fun listen(key: Any, endpoint: Endpoint<T>): Unsubscribe {
            lever(key).endpoints.add(endpoint)
            return {
                levers[key]?.endpoints?.remove(endpoint)
                levers[key]?.let {
                    if (it.endpoints.isEmpty()) {
                        levers.remove(key)
                    }
                }
            }
        }

        /**
         * [once] subscribes a once-only task that will dequeue as soon after the first element update executes.
         * Recommended for one-time state updates, but better to use the auxiliary methods such as `awaits once`
         * instead.
         *
         * @param key the name of the queue task.
         * @param endpoint the endpoint to subscribe.
         */
        fun once(key: Any, endpoint: Endpoint<T>) {
            var unsubscribe: Unsubscribe? = null
            unsubscribe = listen(key) {
                endpoint(it)
                unsubscribe?.invoke()
            }
        }
    }

    /**
     * [ReadOnly] is a state similar to [Writable], but instead, only the getters are exposed to the public. It's a
     * simple wrapper around [Writable] and is used by [Writable.derive] to enable read-only states. As it is of
     * no-use to external code, [ReadOnly] can only be created internally by Nexus.
     */
    class ReadOnly<T> internal constructor(value: T) {
        internal val writable = Writable(value)
        internal fun set(value: T) = writable.set(value)

        /**
         * Gets the value of this [Writable]. This is intended to be used for delegation. You may be looking for
         * [get] instead which allows you to directly get the value.
         */
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return writable.get()
        }

        /**
         * Gets the current value of the [Writable]. If you need to listen to changes to the value,
         * use the [subscribe] method instead to subscribe to changes.
         *
         * @return the value of the [Writable].
         */
        fun get(): T = writable.get()

        /**
         * Subscribes to changes to the value of the [Writable]. This is ran asynchronously after the value has
         * been changed.
         *
         * @param subscription the task to execute upon a change to the value is detected.
         * @return an [Unsubscribe] method to unsubscribe the [Subscription].
         */
        infix fun subscribe(subscription: Subscription<T>): Unsubscribe = writable.subscribe(subscription)
    }

    /**
     * Writable are the equivalent to state in React.js, or [writable] in Svelte (otherwise known as `$state` in Svelte Runes),
     * these are simply properties that will execute subscribed tasks whenever the property changes, enabling reactivity.
     * [Reakt] uses this to support re-rendering the [ReactView] whenever a state changes, allowing developers to write
     * incredibly reactive yet beautifully simple code that are similar to Svelte.
     *
     * We recommend using the constructor method to create a [Writable] for use cases outside of [Reakt] scope, otherwise
     * use the [writable] method to create a [Writable] inside of [Reakt] scope which will have a re-render subscription.
     * (Read the wiki for more information).
     *
     */
    class Writable<T>(value: T) {
        private val subscribers: MutableList<Subscription<T>> = mutableListOf()
        private val _value: AtomicReference<T> = AtomicReference(value)

        /**
         * Gets the value of this [Writable]. This is intended to be used for delegation. You may be looking for
         * [get] instead which allows you to directly get the value.
         */
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return _value.get()
        }

        /**
         * Sets the value of this [Writable]. This is intended to be used for delegation. You may be looking for
         * [set] or [update] instead which allows you to manipulate the [Writable]'s value.
         */
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            set(value)
        }

        /**
         * Manipulates the value of the [Writable].
         * This will run all the subscriptions asynchronously after the value has been changed, ensuring that
         * all subscriptions are executed without interfering or delaying one another.
         *
         * When performing things such as increment, decrements, or anything that requires the current value, we
         * recommend using [update] instead which will allow you to atomically update the value.
         *
         * @param value the new value of the [Writable].
         */
        infix fun set(value: T) {
            val oldValue = _value.get()
            _value.set(value)

            this.react(oldValue, value)
        }

        /**
         * Atomically updates the value of the [Writable]. This is recommended to use when manipulating the value of, say
         * a numerical value, for instance, incrementing, decrementing, multiplying, etc. as this is performed atomically
         * which stops a lot of thread-unsafety.
         *
         * Similar to [set], this executes all the subscriptions asynchronously.
         * @param updater the updater to update the value of the [Writable].
         */
        infix fun update(updater: (T) -> T) {
            val oldValue = _value.get()
            _value.getAndUpdate(updater)

            val value = _value.get()
            this.react(oldValue, value)
        }

        /**
         * Gets the current value of the [Writable]. If you need to listen to changes to the value,
         * use the [subscribe] method instead to subscribe to changes.
         *
         * @return the value of the [Writable].
         */
        fun get(): T = _value.get()

        /**
         * Subscribes to changes to the value of the [Writable]. This is ran asynchronously after the value has
         * been changed.
         *
         * @param subscription the task to execute upon a change to the value is detected.
         * @return an [Unsubscribe] method to unsubscribe the [Subscription].
         */
        infix fun subscribe(subscription: Subscription<T>): Unsubscribe {
            subscribers.add(subscription)
            return { subscribers.remove(subscription) }
        }

        /**
         * Subscribes to changes to the value of the [Writable]. This is ran asynchronously after the value has
         * been changed.
         *
         * @param subscription the task to execute upon a change to the value is detected.
         * @return an [Unsubscribe] method to unsubscribe the [Subscription].
         */
        infix fun subscribe(subscription: PlainSubscription): Unsubscribe {
            return subscribe { _, _ -> subscription() }
        }

        /**
         * Reacts to the change and executes all the subscriptions that were subscribed at the
         * time of execution.
         *
         * @param oldValue the old value.
         * @param value the current value.
         */
        internal fun react(oldValue: T, value: T) {
            subscribers.forEach {
                coroutine {
                    try {
                        it(oldValue, value)
                    } catch (err: Exception) {
                        logger.error("An uncaught exception was received by Reakt's writable subscriptions with the following stacktrace.", err)
                    }
                }
            }
        }

        /**
         * Creates a new [ReadOnly] state that has a value derived of this [Writable], which means that the value
         * of the new [ReadOnly] state changes whenever the value of the current [Writable] changes.
         *
         * Note: This does not inherit the subscriptions of the [Writable], which means that subscriptions such as
         * re-rendering is not inherited, but it's not as needed as the value
         *
         * @param modifier the action to do to mutate the value into the desired value.
         * @return a new [ReadOnly] state that is derived from the current [Writable].
         */
        infix fun <K> derive(modifier: Derive<T, K>): ReadOnly<K> {
            val currentValue = get()
            val state = ReadOnly(modifier(currentValue))

            this.subscribe { _, newValue ->
                state.set(modifier(newValue))
            }
            return state
        }

        override fun toString(): String {
            return _value.get().toString()
        }

        override fun hashCode(): Int {
            return _value.get().hashCode()
        }

        override fun equals(other: Any?): Boolean {
            val value = _value.get()
            if (other == null && value == null) return true
            if (other == null) return false
            return value == other
        }
    }

    class Component internal constructor(private val constructor: ComponentConstructor) {
        /**
         * [shouldRerender] declares whether the component should re-render when [invoke] is invoked
         * one more time. By default, it checks whether the component's props have changed or not, and
         * bases it on that.
         */
        @Volatile var shouldRerender: (old: Props, new: Props) -> Boolean = shouldRerender@{ old, new ->
            for ((key, value) in old) {
                val currentValue = new[key]
                if (currentValue == null || currentValue != value) {
                    return@shouldRerender true
                }
            }
            return@shouldRerender false
        }
        private var beforeMountListeners = mutableListOf<ComponentBeforeMountSubscription>()
        private var afterMountListeners = mutableListOf<ComponentAfterMountSubscription>()

        internal var render: (Document.() -> Unit)? = null
            private set

        internal var props: Map<String, Any?> = mapOf()
            private set

        private var unsubscribes = mutableListOf<Unsubscribe>()

        internal var document = Document()

        internal var hasRenderedOnce = false
            private set
        private var constructed = false

        /**
         * [render] defines how the component should be rendered.
         */
        fun render(document: Document.() -> Unit) {
            this.render = document
        }

        /**
         * [onBeforeMount] adds a [ComponentBeforeMountSubscription] to this component that will execute the
         * subscription right before mounting. This is similar to [onRender] but for a specific component only,
         * this happens before the component is rendered, which means you can use this to manipulate the different
         * values of the component.
         */
        fun onBeforeMount(subscription: ComponentBeforeMountSubscription) {
            beforeMountListeners.add(subscription)
        }

        /**
         * [onAfterMount] adds a [ComponentAfterMountSubscription] to this component that will execute the
         * subscription after mounting. This is similar to [onUpdate], but rather than a full update on the message,
         * it refers to after the component is rendered (no guarantee that the message was updated yet).
         */
        fun onAfterMount(subscription: ComponentAfterMountSubscription) {
            afterMountListeners.add(subscription)
        }

        /**
         * [prop] gets the [prop] with the [name]. If there are no props with the name, it will return [null] instead.
         *
         * @param name the name of the prop to retrieve.
         * @return the prop's value.
         */
        fun anyProp(name: String): Any? = props[name.lowercase()]

        /**
         * [prop] gets the [prop] with the [name] as [T] type. If there are no props with the name, and with the
         * right type, it will return [null] instead.
         *
         * @param name the name of the prop to retrieve.
         * @return the prop as a [T] type.
         * @throws PropTypeMismatch when the type of the prop received does not match the expected type.
         */
        inline fun <reified T> prop(name: String): T? {
            val value = anyProp(name) ?: return null
            return (value as? T) ?: throw PropTypeMismatch(name, T::class.java, value::class.java)
        }

        /**
         * [ensureProp] ensures that the [prop] with the [name] exists and returns it. This is
         * recommended for components as it tells the developer what props you are expecting and
         * what it's type should be.
         *
         * @param name the name of the prop to retrieve.
         * @return the prop as a [T] type.
         * @throws PropTypeMismatch when the type of the prop received does not match the expected type.
         * @throws UnknownPropException when there is no prop received with the name.
         */
        inline fun <reified T> ensureProp(name: String): T {
            return prop(name) ?: throw UnknownPropException(name)
        }

        /**
         * [writableProp] gets a [prop] of a [Writable] type, this is intended to be able to use
         * delegation much simpler without having to type out the entire [Writable] type.
         *
         * @param name the name of the prop to retrieve.
         * @return the prop as a [Writable] type.
         * @throws PropTypeMismatch when the type of the prop received does not match the expected type.
         * @throws UnknownPropException when there is no prop received with the name.
         */
        inline fun <reified T> writableProp(name: String): Writable<T> {
            return ensureProp(name)
        }

        /**
         * [readonlyProp] gets a [prop] of a [ReadOnly] type, this is intended to be able to use
         * delegation much simpler without having to type out the entire [ReadOnly] type.
         *
         * @param name the name of the prop to retrieve.
         * @return the prop as a [ReadOnly] type.
         * @throws PropTypeMismatch when the type of the prop received does not match the expected type.
         * @throws UnknownPropException when there is no prop received with the name.
         */
        inline fun <reified T> readonlyProp(name: String): ReadOnly<T> {
            return ensureProp(name)
        }

        internal fun rerender() {
            synchronized(this) {
                document = Document()
                val render = render ?: throw NoRenderFoundException

                for (beforeMountListener in beforeMountListeners) {
                    beforeMountListener()
                }
                render(document)
                hasRenderedOnce = true
                for (afterMountListener in afterMountListeners) {
                    coroutine {
                        afterMountListener()
                    }
                }
            }
        }

        operator fun invoke(vararg props: Pair<String, Any?>) {
            synchronized(this) {
                for (unsubscribe in unsubscribes) {
                    unsubscribe()
                }
                this.props = props.associate {
                    val secondParameter = it.second
                    if (secondParameter is Writable<*>) {
                        unsubscribes += secondParameter.subscribe(::rerender)
                    }
                    return@associate it.first.lowercase() to secondParameter
                }
                if (!constructed) {
                    constructor(this)
                }
            }
        }

        internal fun compare(component: Component): Boolean {
            if (this::class.java != component::class.java) return false
            if (props.containsKey("%key") && component.props.containsKey("%key")) {
                val key = component.props["%key"]
                val key2 = component.props["%key"]
                if (key != key2) {
                    return false
                }
            } else {
                for ((key, value) in component.props) {
                    if (!component.props.containsKey(key)) {
                        return false
                    }
                    val newValue = component.props[key]
                    if (newValue != value) {
                        return false
                    }
                }
            }

            return true
        }
    }

    class StackElement internal constructor() {
        internal var embed: EmbedBuilder? = null
        internal var component: LowLevelComponent? = null
        internal var listener: GloballyAttachableListener? = null
        internal var uuid: String? = null
        internal var textContent: String? = null

        fun render(view: View) {
            val embed = embed
            val component =  component
            val listener = listener
            val uuid = uuid
            val textContent = textContent

            if (embed != null) {
                view.embeds += embed
            }

            if (component != null) {
                view.components += component
            }

            if (listener != null) {
                view.listeners += listener
            }

            if (uuid != null) {
                view.uuids += uuid
            }

            if (textContent != null) {
                view.contents = textContent
            }
        }
    }

    class Document internal constructor() {
        internal var components = mutableListOf<Component>()
        internal var stack: MutableStack = mutableListOf()
            private set

        internal fun stack(constructor: StackElement.() -> Unit) {
            val element = StackElement()
            constructor(element)
            this.stack += element
        }

        internal fun copyComponents(): List<Component> {
            return components.toList()
        }

        fun component(constructor: Component.() -> Unit): Component {
            val component = Component(constructor)
            components += component
            return component
        }
    }

    class View internal constructor() {
        internal var embeds: MutableList<EmbedBuilder> = mutableListOf()
        internal var contents: String? = null
        internal var components: MutableList<LowLevelComponent> = mutableListOf()
        internal var listeners: MutableList<GloballyAttachableListener> = mutableListOf()
        internal var uuids: MutableList<String> = mutableListOf()

        private fun attachListeners(api: DiscordApi): Unsubscribe {
            val listenerManagers = listeners.map { api.addListener(it) }
            return {
                listenerManagers.forEach { managers ->
                    managers.forEach { it.remove() }
                }
                listeners = mutableListOf()

                components = mutableListOf()
                contents = null
                embeds = mutableListOf()

                uuids.forEach { UuidGenerator.deny(it) }
                uuids = mutableListOf()
            }
        }

        private fun chunkComponents(): List<ActionRow> {
            val actionRows = mutableListOf<ActionRow>()
            var lowLevelComponents = mutableListOf<LowLevelComponent>()

            for ((index, component) in components.withIndex()) {
                if (component.isSelectMenu) {
                    actionRows += ActionRow.of(component)
                    continue
                } else {
                    if (lowLevelComponents.size >= 3) {
                        actionRows += ActionRow.of(lowLevelComponents)
                        lowLevelComponents = mutableListOf()
                    }

                    lowLevelComponents += component
                }

                if (index == (components.size - 1) && lowLevelComponents.size <= 3) {
                    actionRows += ActionRow.of(lowLevelComponents)
                    lowLevelComponents = mutableListOf()
                }
            }

            if (lowLevelComponents.isNotEmpty() && lowLevelComponents.size <= 3) {
                actionRows += ActionRow.of(lowLevelComponents)
            }

            return actionRows
        }

        fun render(api: DiscordApi): Pair<Unsubscribe, ReaktMessage> {
            return attachListeners(api) to ReaktMessage.with {
                this.removeAllEmbeds()
                this.addEmbeds(embeds)

                if (contents != null) {
                    this.setContent(contents)
                }

                chunkComponents().forEach { this.addComponents(it) }
            }
        }

        // TODO: Reduce code duplication by using MessageBuilderBase (currently package-private)
        //       https://discord.com/channels/151037561152733184/151326093482262528/1163425854186065951
        fun render(updater: MessageUpdater, api: DiscordApi): Unsubscribe {
            updater.apply {
                this.removeAllEmbeds()
                this.addEmbeds(embeds)

                if (contents != null) {
                    this.setContent(contents)
                }
                chunkComponents().forEach { this.addComponents(it) }
            }
            return attachListeners(api)
        }

        fun render(builder: MessageBuilder, api: DiscordApi): Unsubscribe {
            builder.apply {
                this.removeAllEmbeds()
                this.addEmbeds(embeds)

                if (contents != null) {
                    this.setContent(contents)
                }
                chunkComponents().forEach { this.addComponents(it) }
            }
            return attachListeners(api)
        }


        fun render(updater: InteractionOriginalResponseUpdater, api: DiscordApi): Unsubscribe{
            updater.apply {
                this.removeAllEmbeds()
                this.removeAllComponents()

                this.addEmbeds(embeds)

                if (contents != null) {
                    this.setContent(contents)
                }
                chunkComponents().forEach { this.addComponents(it) }
            }
            return attachListeners(api)
        }
    }
}

typealias MutableStack = MutableList<Reakt.StackElement>