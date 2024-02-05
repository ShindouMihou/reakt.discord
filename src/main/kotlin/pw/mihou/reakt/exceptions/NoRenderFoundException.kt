package pw.mihou.reakt.exceptions

class NoRenderFoundException(qualifiedName: String): RuntimeException("No render method was identified in the Reakt component '$qualifiedName'.")