package pw.mihou.reakt.exceptions

object NoRenderFoundException: RuntimeException("No render method was identified in a Reakt component.") {
    private fun readResolve(): Any = NoRenderFoundException
}