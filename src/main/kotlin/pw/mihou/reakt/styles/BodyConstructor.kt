@file:Suppress("unused")

package pw.mihou.reakt.styles

import java.time.Instant

class BodyConstructor internal constructor(var autoAppendNewLines: Boolean) {
    internal val builder = StringBuilder()
    internal val content get() = builder.toString()
    private val Array<out TextStyles>.isBold get() = this.contains(TextStyles.BOLD)
    private val Array<out TextStyles>.isItalic get() = this.contains(TextStyles.ITALIC)
    private val Array<out TextStyles>.isUnderlined get() = this.contains(TextStyles.UNDERLINE)
    private val Array<out TextStyles>.isStrikedthrough get() = this.contains(TextStyles.STRIKETHROUGH)
    private val Array<out TextStyles>.isSpoilered get() = this.contains(TextStyles.SPOILER)
    private val Array<out TextStyles>.isHighlighted get() = this.contains(TextStyles.HIGHLIGHTED)

    private fun renderTextStyles(vararg styles: TextStyles): Pair<String, String> {
        var prefix = ""
        var suffix = ""

        if (styles.isBold) {
            prefix += "**"
            suffix += "**"
            if (styles.isItalic) {
                prefix += "*"
                suffix += "*"
            }
        }

        if (styles.isUnderlined) {
            prefix += "__"
            suffix = "__$suffix"
        }

        if (styles.isItalic && !styles.isBold) {
            prefix += "*"
            suffix = "*$suffix"
        }

        if (styles.isStrikedthrough) {
            prefix += "~~"
            suffix = "~~$suffix"
        }

        if (styles.isHighlighted) {
            prefix += "`"
            suffix = "`$suffix"
        }

        if (styles.isSpoilered) {
            prefix = "||$prefix"
            suffix = "$suffix||"
        }

        return prefix to suffix
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun append(line: String) {
        builder.append(if (autoAppendNewLines) "\n$line" else line)
    }

    infix fun BodyConstructor.append(text: String) {
        append(text)
    }

    val String.append get() = append(this)

    /**
     * Renders a bold text. We recommend using [p] when you want to stack different
     * styling over one another.
     */
    val String.bold get() = bold(this)

    /**
     * Renders an underlined text. We recommend using [p] when you want to stack different
     * styling over one another.
     */
    val String.underlined get() = underline(this)


    /**
     * Renders an italicized text. We recommend using [p] when you want to stack different
     * styling over one another.
     */
    val String.italicized get() = italic(this)


    /**
     * Renders a marked text. We recommend using [p] when you want to stack different
     * styling over one another.
     */
    val String.marked get() = mark(this)


    /**
     * Renders a spoilered text. We recommend using [p] when you want to stack different
     * styling over one another.
     */
    val String.spoilered get() = spoiler(this)


    /**
     * Renders a strikethrough text. We recommend using [p] when you want to stack different
     * styling over one another.
     */
    val String.deleted get() = del(this)


    /**
     * Renders the text as a primary heading. We recommend using [p] when you want to stack different
     * styling over one another.
     */
    val String.h1 get() = h1(this)

    /**
     * Renders the text as a secondary heading. We recommend using [p] when you want to stack different
     * styling over one another.
     */
    val String.h2 get() = h2(this)

    /**
     * Renders the text as a tertiary heading. We recommend using [p] when you want to stack different
     * styling over one another.
     */
    val String.h3 get() = h3(this)


    /**
     * Renders a regular text content, unlike methods such as [bold], [italic], you can write this as unstyled
     * or stack different stylings over the text through parameters.
     *
     * @param text the text to render.
     * @param styles the different styles to display.
     */
    fun p(text: String, vararg styles: TextStyles): String {
        val (prefix, suffix) = renderTextStyles(*styles)
        return prefix + text + suffix
    }

    /**
     * Renders an underlined text. We recommend using [p] when you want to stack different
     * styling over one another.
     *
     * @param text the text to render.
     */
    fun underline(text: String) = "__${text}__"

    /**
     * Renders a bold text. We recommend using [p] when you want to stack different
     * styling over one another.
     *
     * @param text the text to render.
     */
    fun bold(text: String) = "**$text**"

    /**
     * Renders an italic text. We recommend using [p] when you want to stack different
     * styling over one another.
     *
     * @param text the text to render.
     */
    fun italic(text: String) = "*$text*"

    /**
     * Renders a highlighted text. We recommend using [p] when you want to stack different
     * styling over one another.
     *
     * @param text the text to render.
     */
    fun mark(text: String) = "`$text`"

    /**
     * Renders a spoiler text. We recommend using [p] when you want to stack different
     * styling over one another.
     *
     * @param text the text to render.
     */
    fun spoiler(text: String) = "||$text||"

    /**
     * Renders a text with a strikethrough. We recommend using [p] when you want to stack different
     * styling over one another.
     *
     * @param text the text to render.
     */
    fun del(text: String) = "~~$text~~"

    /**
     * Renders a next line, we recommend using `StyledBody` instead when next text has a next line.
     */
    val br = "\n"

    /**
     * Renders a link text that allows users to click and be redirected to the [href].
     *
     * @param text the text to render.
     * @param href the address the text should point.
     * @param styles the additional styling to add.
     */
    fun link(text: String, href: String, vararg styles: TextStyles): String {
        val (prefix, suffix) = renderTextStyles(*styles)
        return "$prefix[$text]($href)$suffix"
    }

    /**
     * Renders a main title heading text.
     * @param text the text to render.
     */
    fun h1(text: String): String {
        return "# $text"
    }

    /**
     * Renders a subsection heading text.
     * @param text the text to render.
     */
    fun h2(text: String): String {
        return "## $text"
    }

    /**
     * Renders a sub-subsection heading text.
     * @param text the text to render.
     */
    fun h3(text: String): String {
        return "### $text"
    }

    /**
     * Renders an unordered list with the given nodes. This cannot be used to create nested
     * unordered lists.
     *
     * @param nodes the nodes to include in the unordered list.
     */
    fun ul(vararg nodes: String): String {
        return nodes.joinToString("\n") { "* $it" }
    }

    /**
     * Renders an ordered list with the given nodes. This cannot be used to create nested
     * ordered lists.
     *
     * @param nodes the nodes to include in the unordered list.
     */
    fun ol(vararg nodes: String): String {
        var text = ""
        for ((index, node) in nodes.withIndex()) {
            text += "${index + 1}. $node\n"
        }
        return text
    }

    /**
     * Renders the texts inside a codeblock.
     *
     * @param language the language to use to highlight the nodes.
     * @param nodes the nodes to include in the codeblock.
     */
    fun codeblock(language: String, vararg nodes: String): String {
        return "```$language\n${nodes.joinToString("\n")}\n```"
    }

    /**
     * Renders the texts under a blockquote.
     *
     * @param nodes the texts to write inside a blockquote.
     */
    fun blockquote(vararg nodes: String): String {
        return nodes.joinToString("\n") { "> $it"}
    }

    /**
     * Renders the [Instant] as a timestamp in Discord. By default, this uses the relative time format, but you
     * can customize it to show different outputs.
     *
     * **Timestamp Formats**:
     * 1. **Relative** ([TimeFormat.RELATIVE]): shows a relative time (e.g. 30 seconds ago)
     * 2. **Short Time** ([TimeFormat.SHORT_TIME]): shows the time without the seconds (e.g. 10:07 PM)
     * 3. **Long Time** ([TimeFormat.LONG_TIME]): shows the time with the seconds (e.g. 10:07:00 PM)
     * 4. **Short Date** ([TimeFormat.SHORT_DATE]): shows the date with the month as a number and the year shortened (e.g. 10/17/23)
     * 5. **Long Date** ([TimeFormat.LONG_DATE]): shows the date with the full month name and the full year (e.g. October 17, 2023)
     * 6. **Long Date With Short Time** ([TimeFormat.LONG_DATE_WITH_SHORT_TIME]): shows the date with a short time (e.g. October 17, 2023 at 10:07 PM)
     * 7. **Long Date With Day of Week and Short Time** ([TimeFormat.LONG_DATE_WITH_DAY_OF_WEEK_AND_SHORT_TIME]): shows the date with the short time and the day of the week (e.g. Tuesday, October 17, 2023 at 10:07 PM)
     *
     * @param instant the [Instant] to render as a timestamp.
     * @param format the format of the timestamp.
     */
    fun time(instant: Instant, format: TimeFormat = TimeFormat.RELATIVE) = "<t:${instant.epochSecond}:${format.suffix}>"
}