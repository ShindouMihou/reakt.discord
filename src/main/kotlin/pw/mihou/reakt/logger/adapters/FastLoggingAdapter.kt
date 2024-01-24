package pw.mihou.reakt.logger.adapters

import pw.mihou.reakt.logger.LoggingAdapter
import pw.mihou.reakt.logger.adapters.enums.LogLevel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

typealias FastLoggingFormatter = (time: Instant, level: LogLevel, message: String) -> String

// Nexus' logging adapter transported down to reakt.
// CC: https://github.com/ShindouMihou/reakt
object FastLoggingAdapter: LoggingAdapter {

    @Volatile @JvmField var datetimeFormatter: DateTimeFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss.SSSZ")
        .withZone(ZoneId.systemDefault())

    @Volatile @JvmField var formatter: FastLoggingFormatter = {
            time: Instant,
            level: LogLevel,
            message: String
        -> "${datetimeFormatter.format(time)} $level reakt $message"
    }

    @Volatile @JvmField var allowed: MutableSet<LogLevel> = mutableSetOf(
        LogLevel.ERROR,
        LogLevel.INFO,
        LogLevel.WARN
    )

    /**
     * It is recommended to set this as false when using for reakt only as the framework no longer
     * uses placeholders, but instead interpolate the data directly using Kotlin's string templates.
     *
     * (If for some reason that you are also using this for logger, you can set this as true, but this will
     * significantly slow down the performance as it will scan the message for any placeholders).
     */
    @Volatile @JvmField var usePlaceholders = false

    /**
     * Formats the reakt log messages which are based out of
     * SLF4J-standard into log messages that are complete and readable.
     *
     * @param message   The message to format.
     * @param values    The values to append into the message.
     * @return          The formatted log message.
     */
    private fun exchange(message: String, vararg values: Any): String {
        @Suppress("NAME_SHADOWING") var message = message
        for (value in values) {
            message = message.replaceFirst("\\{}".toRegex(), value.toString())
        }
        return message
    }

    /**
     * Creates a message, performs legacy interpolation when [usePlaceholders] is enabled.
     * @param message the message to create
     * @param values the values to interpolate, if [usePlaceholders] is present.
     * @return the original message if [usePlaceholders] is disabled, otherwise the interpolated message.
     */
    private fun createMessage(message: String, values: List<Any>): String {
        if (usePlaceholders && message.contains("{}")) {
            return exchange(message, values)
        }
        return message
    }

    /**
     * Logs the message if the configuration supports for the logging level specified.
     *
     * @param message   The message to format and log into the console.
     * @param level     The logging level to log this message as.
     * @param values    The values to append into the message.
     */
    private fun log(message: String, level: LogLevel, vararg values: Any) {
        if (allowed.contains(level)) {
            var printStream = System.out
            if (level == LogLevel.ERROR) {
                printStream = System.err
            }
            @Suppress("NAME_SHADOWING") val message =  createMessage(message, values.toList())
            printStream.println(formatter(Instant.now(), level, message))
            if (!usePlaceholders || !message.contains("{}")) {
                Arrays.stream(values).forEach { `object`: Any? ->
                    if (`object` is Exception) {
                        `object`.printStackTrace()
                    } else if (`object` is Throwable) {
                        `object`.printStackTrace()
                    }
                }
            }
        }
    }

    override fun info(message: String, vararg values: Any) {
        log(message, LogLevel.INFO, *values)
    }

    override fun error(message: String, vararg values: Any) {
        log(message, LogLevel.ERROR, *values)
    }

    override fun warn(message: String, vararg values: Any) {
        log(message, LogLevel.WARN, *values)
    }

    override fun debug(message: String, vararg values: Any) {
        log(message, LogLevel.DEBUG, *values)
    }

}