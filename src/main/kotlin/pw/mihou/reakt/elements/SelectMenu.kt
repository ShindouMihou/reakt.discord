@file:Suppress("FunctionName")
package pw.mihou.reakt.elements

import org.javacord.api.entity.channel.ChannelType
import org.javacord.api.entity.message.component.ComponentType
import org.javacord.api.entity.message.component.SelectMenuBuilder
import org.javacord.api.entity.message.component.SelectMenuOption
import org.javacord.api.entity.message.component.SelectMenuOptionBuilder
import org.javacord.api.event.interaction.SelectMenuChooseEvent
import org.javacord.api.listener.interaction.SelectMenuChooseListener
import pw.mihou.reakt.Reakt
import pw.mihou.reakt.uuid.UuidGenerator

typealias ReaktSelectMenuSelectListener = ((event: SelectMenuChooseEvent) -> Unit)
typealias ReaktSelectMenuConstructor = SelectMenu.() -> Unit
@Suppress("NAME_SHADOWING")
fun Reakt.Document.SelectMenu(
    componentType: ComponentType,
    customId: String = UuidGenerator.request(),
    minimumValues: Int = 1,
    maximumValues: Int = 1,
    disabled: Boolean = false,
    onSelect: ReaktSelectMenuSelectListener? = null,
    selectMenu: ReaktSelectMenuConstructor
) =  component("pw.mihou.reakt.elements.SelectMenu") {

    val componentType = ensureProp<ComponentType>("componentType")
    val customId = ensureProp<String>("customId")
    val minimumValues = ensureProp<Int>("minimumValues")
    val maximumValues = ensureProp<Int>("maximumValues")
    val disabled = ensureProp<Boolean>("disabled")
    val onSelect = prop<ReaktSelectMenuSelectListener>("onSelect")
    val selectMenu = ensureProp<ReaktSelectMenuConstructor>("constructor")

    render {
        // @native directly injects a select menu into the stack.
        document.stack {
            val element = SelectMenu(
                SelectMenuBuilder(componentType, customId)
                    .setDisabled(disabled)
                    .setMaximumValues(maximumValues)
                    .setMinimumValues(minimumValues))
            selectMenu(element)

            if (onSelect != null) {
                listener = SelectMenuChooseListener {
                    if (it.selectMenuInteraction.customId != customId) {
                        return@SelectMenuChooseListener
                    }

                    onSelect(it)
                }

                uuid = customId
            }

            component = element.selectMenu.build()
        }
    }
}(
    "componentType" to componentType, "customId" to customId, "minimumValues" to minimumValues,
    "maximumValues"  to maximumValues, "disabled" to disabled, "onSelect" to onSelect,
    "constructor" to selectMenu) // @note auto-invoke component upon creation.

fun Reakt.Document.ChannelSelectMenu(
    types: Set<ChannelType>,
    placeholder: String? = null,
    customId: String = UuidGenerator.request(),
    minimumValues: Int = 1,
    maximumValues: Int = 1,
    disabled: Boolean = false,
    onSelect: ((event: SelectMenuChooseEvent) -> Unit)? = null
) = SelectMenu(
    componentType = ComponentType.SELECT_MENU_CHANNEL,
    customId = customId,
    minimumValues = minimumValues,
    maximumValues = maximumValues,
    disabled = disabled,
    onSelect = onSelect
) {
    types.forEach(::ChannelType)
    placeholder?.let { Placeholder(it) }
}

fun Reakt.Document.ChannelSelectMenu(
    placeholder: String? = null,
    customId: String = UuidGenerator.request(),
    minimumValues: Int = 1,
    maximumValues: Int = 1,
    disabled: Boolean = false,
    onSelect: ((event: SelectMenuChooseEvent) -> Unit)? = null
) = SelectMenu(
    componentType = ComponentType.SELECT_MENU_CHANNEL,
    customId = customId,
    minimumValues = minimumValues,
    maximumValues = maximumValues,
    disabled = disabled,
    onSelect = onSelect
) {
    placeholder?.let { Placeholder(it) }
}

fun Reakt.Document.UserSelectMenu(
    placeholder: String? = null,
    customId: String = UuidGenerator.request(),
    minimumValues: Int = 1,
    maximumValues: Int = 1,
    disabled: Boolean = false,
    onSelect: ((event: SelectMenuChooseEvent) -> Unit)? = null
) = SelectMenu(
    componentType = ComponentType.SELECT_MENU_USER,
    customId = customId,
    minimumValues = minimumValues,
    maximumValues = maximumValues,
    disabled = disabled,
    onSelect = onSelect
) {
    placeholder?.let { Placeholder(it) }
}

fun Reakt.Document.MentionableSelectMenu(
    placeholder: String? = null,
    customId: String = UuidGenerator.request(),
    minimumValues: Int = 1,
    maximumValues: Int = 1,
    disabled: Boolean = false,
    onSelect: ((event: SelectMenuChooseEvent) -> Unit)? = null
) = SelectMenu(
    componentType = ComponentType.SELECT_MENU_MENTIONABLE,
    customId = customId,
    minimumValues = minimumValues,
    maximumValues = maximumValues,
    disabled = disabled,
    onSelect = onSelect
) {
    placeholder?.let { Placeholder(it) }
}

fun Reakt.Document.SelectMenu(
    options: List<SelectMenuOption>,
    placeholder: String? = null,
    customId: String = UuidGenerator.request(),
    minimumValues: Int = 1,
    maximumValues: Int = 1,
    disabled: Boolean = false,
    onSelect: ((event: SelectMenuChooseEvent) -> Unit)? = null
) = SelectMenu(
    componentType = ComponentType.SELECT_MENU_STRING,
    customId = customId,
    minimumValues = minimumValues,
    maximumValues = maximumValues,
    disabled = disabled,
    onSelect = onSelect
) {
    options.forEach(::Option)
    placeholder?.let { Placeholder(it) }
}

class SelectMenu(internal val selectMenu: SelectMenuBuilder) {
    fun ChannelType(type: ChannelType) {
        selectMenu.addChannelType(type)
    }

    fun Option(option: SelectMenuOption) {
        selectMenu.addOption(option)
    }

    fun Option(builder: SelectMenuOptionBuilder.() -> Unit) {
        val original = SelectMenuOptionBuilder()
        builder(original)
        selectMenu.addOption(original.build())
    }

    fun Options(vararg options: SelectMenuOption) {
        selectMenu.addOptions(options.toList())
    }

    fun Placeholder(text: String) {
        selectMenu.setPlaceholder(text)
    }
}