package pw.mihou.reakt.uuid

import java.util.*
import java.util.concurrent.ConcurrentHashMap

object UuidGenerator {
    /**
     * These are the UUIDs that have reached a handshake with the
     * assigner which means these are allocated to a certain object.
     *
     *
     * A UUID is universally unique by session and this ensures that
     * the Nexus instance does not encounter a UUID collision that could
     * lead fatal.
     *
     *
     * Once a UUID is assigned, it will never be removed unless you somehow have
     * reached the limit of the UUID uniqueness in which by then, might as well restart
     * your JVM.
     */
    private val acceptedUUIDs: MutableSet<String> = ConcurrentHashMap.newKeySet()

    /**
     * Attempts to perform a handshake over a single universally unique
     * identifier for this session. This returns true if the handshake was accepted
     * and false if the UUID is rejected because it exists.
     *
     * @param uuid The UUID to attempt.
     * @return Was the UUID accepted?
     */
    private fun handshake(uuid: String): Boolean {
        return acceptedUUIDs.add(uuid)
    }

    /**
     * Requests for a universally unique identifier for this JVM session that a
     * Reakt object can use without worrying about UUID conflicts with another Nexus
     * object.
     *
     * @return A universally, unique identifier for this JVM.
     */
    fun request(): String {
        val uuid = "reakt-" + UUID.randomUUID().toString()
        return if (handshake(uuid)) uuid else request()
    }

    /**
     * Denies the universally unique identifier for this JVM session. This should be used internally only
     * when unexpectedly there is a user-generated variant that has the same identifier in wherever this
     * universally unique identifier is being used.
     *
     * @param uuid The UUID to deny.
     */
    fun deny(uuid: String) {
        acceptedUUIDs.remove(uuid)
    }
}
