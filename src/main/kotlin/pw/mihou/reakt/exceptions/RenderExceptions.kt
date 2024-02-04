package pw.mihou.reakt.exceptions

object ReaktComponentDuplicateException: RuntimeException("RKT-939 This issue can happen when two or more components " +
        "(with Discord components, such as buttons) have no differentiating factors (such as a prop), " +
        "leading to Reakt reusing the same render output for all of them. " +
        "To resolve this issue, you may add the '%key' prop with a unique, identifying value that " +
        "identifies the component. (Read more at https://github.com/ShindouMihou/reakt.discord/wiki/Troubleshooting)") {
    private fun readResolve(): Any = ReaktComponentDuplicateException
}