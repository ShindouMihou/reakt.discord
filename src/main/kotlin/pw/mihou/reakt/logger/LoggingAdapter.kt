package pw.mihou.reakt.logger

interface LoggingAdapter {
    /**
     * Logs a new info message onto the logging framework.
     *
     * @param message   The raw message to log with placeholders equal to that of SLF4J.
     * @param values    The values to append into the placeholders.
     */
    fun info(message: String, vararg values: Any)

    /**
     * Logs a new error message onto the logging framework.
     *
     * @param message   The raw message to log with placeholders equal to that of SLF4J.
     * @param values    The values to append into the placeholders.
     */
    fun error(message: String, vararg values: Any)

    /**
     * Logs a new warn message onto the logging framework.
     *
     * @param message   The raw message to log with placeholders equal to that of SLF4J.
     * @param values    The values to append into the placeholders.
     */
    fun warn(message: String, vararg values: Any)

    /**
     * Logs a new debug message onto the logging framework.
     *
     * @param message   The raw message to log with placeholders equal to that of SLF4J.
     * @param values    The values to append into the placeholders.
     */
    fun debug(message: String, vararg values: Any)
}

