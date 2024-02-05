package pw.mihou.reakt.exceptions


open class ReaktRuleEnforcementException(code: Int, message: String, additionalData: String? = null):
    RuntimeException(
        "RKT-$code $message (Read more at https://github.com/ShindouMihou/reakt.discord/wiki/Troubleshooting)." +
                if (additionalData != null) "\n$additionalData"
                else ""
    )

class ReaktComponentDuplicateException(message: String):
    ReaktRuleEnforcementException(
        code = 939,
        message = "This issue can happen when two or more components (with Discord components, such as buttons) " +
                "have no differentiating factors (such as a prop), leading to Reakt reusing the same render " +
                "output for all of them. To resolve this issue, you may add the '%key' prop with a unique, " +
                "identifying value that identifies the component.",
        additionalData = message
    )

object ReaktStateInsideRenderMethodException:  ReaktRuleEnforcementException(
    code = 748,
    message = "You cannot create a subscribed state inside the `render` method. " +
            "Creating a state inside the `render` method causes it to recreate itself for every time the `render` method is called, creating " +
            "waste. To resolve this issue, move the state declaration outside of the `render` method."
) {
    private fun readResolve(): Any = ReaktStateInsideRenderMethodException
}