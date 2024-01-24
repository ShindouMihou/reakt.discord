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

fun Reakt.Component.SelectMenu(
    componentType: ComponentType,
    customId: String = UuidGenerator.request(),
    minimumValues: Int = 1,
    maximumValues: Int = 1,
    disabled: Boolean = false,
    onSelect: ((event: SelectMenuChooseEvent) -> Unit)? = null,
    selectMenu: SelectMenu.() -> Unit
) {
    val element = SelectMenu(
        SelectMenuBuilder(componentType, customId)
            .setDisabled(disabled)
            .setMaximumValues(maximumValues)
            .setMinimumValues(minimumValues))
    selectMenu(element)

    if (onSelect != null) {
        listeners += SelectMenuChooseListener {
            if (it.selectMenuInteraction.customId != customId) {
                return@SelectMenuChooseListener
            }

            onSelect(it)
        }
    }

    components += element.selectMenu.build()
}

fun Reakt.Component.ChannelSelectMenu(
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

fun Reakt.Component.ChannelSelectMenu(
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

fun Reakt.Component.UserSelectMenu(
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

fun Reakt.Component.MentionableSelectMenu(
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

fun Reakt.Component.SelectMenu(
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