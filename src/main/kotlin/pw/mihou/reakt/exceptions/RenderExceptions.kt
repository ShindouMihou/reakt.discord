package pw.mihou.reakt.exceptions

class ReaktComponentDuplicateException(message: String): RuntimeException("RKT-939 This issue can happen when two or more components " +
        "(with Discord components, such as buttons) have no differentiating factors (such as a prop), " +
        "leading to Reakt reusing the same render output for all of them. " +
        "To resolve this issue, you may add the '%key' prop with a unique, identifying value that " +
        "identifies the component. (Read more at https://github.com/ShindouMihou/reakt.discord/wiki/Troubleshooting)." +
        "\n" +
        message
)

object ReaktStateInsideRenderMethodException: RuntimeException("RKT-748 You cannot create a subscribed state inside the `render` method. " +
        "Creating a state inside the `render` method causes it to recreate itself for every time the `render` method is called, creating " +
        "waste. To resolve this issue, move the state declaration outside of the `render` method. " +
        "(Read more at https://github.com/ShindouMihou/reakt.discord/wiki/Troubleshooting)."
) {
    private fun readResolve(): Any = ReaktStateInsideRenderMethodException
}