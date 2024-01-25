package pw.mihou.reakt.exceptions

class UnknownPropException(prop: String): RuntimeException("Unknown prop '$prop'.")
class PropTypeMismatch(prop: String, expected: Class<*>, found: Class<*>):
    RuntimeException("Expected prop '$prop' to be of ${expected.name} but found ${found.name} instead.")