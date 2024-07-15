package pw.mihou.reakt

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ComponentHashCodeTest {

    @Test
    fun `test hashcode consistency`() {
        // Setup
        val qualifiedName = "MockComponent"
        val acknowledgementMessage = "You are about to reset the honey for **all users in the server**! "
        val confirmationMessage =
            "Are you sure you would like to reset honey for all Honey for users in {server.name}? " +
                    "This will set every user with the roles' honey from to **0**!"
        val confirmedMessage = "Honey in {server.name} will be reset for every member."
        val props = mapOf(
            "acknowledgementMessage" to acknowledgementMessage,
            "confirmationMessage" to confirmationMessage,
            "confirmedMessage" to confirmedMessage,
            "confirmationAction" to {},
        )
        val component = Component(qualifiedName, props)

        // Expected hashcode calculation
        var expectedHashCode = component.hashCode

        // Execution and Verification
        repeat(200) { iteration ->
            val actualHashCode = component.hashCode
            assertEquals(expectedHashCode, actualHashCode, "The hashcode does not match the expected value on iteration ${iteration + 1}.")
        }
    }

    class Component(val qualifiedName: String, val props: Map<String, Any?>) {
        val hashCode: Int
            get() {
                var result = 1
                props.entries
                    .filterNot { it.key.endsWith(Reakt.RESERVED_VALUE_KEY) }
                    .forEach { (key, value) ->
                        result = 31 * result + (key.hashCode() + (value?.hashCode() ?: 0))
                    }
                result = 31 * result + qualifiedName.hashCode()
                return result
            }
    }
}