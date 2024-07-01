package pw.mihou.reakt.components

import pw.mihou.reakt.Props
import pw.mihou.reakt.ReactDocument
import pw.mihou.reakt.Reakt

@Suppress("FunctionName", "Unused")
fun Reakt.Document.Provider(args: List<Pair<String, Any>>, render: ReactDocument) =
    component("pw.mihou.reakt.<native>.components.Provider") {
        var rendering by session.nativeRef("rendering", true)
        render {
            try {
                render()
            } finally {
                rendering = false
            }
        }
    }(*args.toTypedArray())


data class ProviderProperties internal constructor(
    internal val hash: Int,
    internal val props: Props,
    internal val rendering: Reakt.Writable<Boolean>
)