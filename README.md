# Reakt

An innovative framework laid on top of Javacord for building highly interactive (or reactive) Discord bots based 
on the same concept of JavaScript Front-End frameworks such as React and Svelte.

Reakt is a rendering framework abstraction on top of Javacord that was originally built as part of the [Nexus](https://nexus.mihou.pw) 
framework. What sets Reakt apart from other frameworks is that it doesn't necessarily interfere with other frameworks' mechanisms 
such as how their command systems works, and supports both message and all interactions.

## Write-once, Use-Anywhere

As Reakt abstracts many parts of how one would interact with messages and interactions on Javacord, we are able to make it 
so that developers only ever need to write code once for all kinds of interactions. Whether it'd be auto-deferring, re-rendering 
messages and everything. Reakt handles it by itself.

## Reactively Simple

Reakt was built with Svelte as a primary inspiration, albeit limited by the capabilities of Kotlin. We still managed to achieve 
simple reactivity by the use of proxy-subscribers with Kotlin's delegation technique. In fact, here's a complete example of 
the framework paired with Nexus:
```kotlin
fun onEvent(event: NexusCommandEvent) {
    event.R {
        var clicks by writable(0)
        render {
            Embed {
                Title("Rendered with Reakt")
                SpacedBody(
                    p("This message was rendered with Reakt."),
                    p("The button has been clicked ") + bold("$clicks times.")
                )
                Color(java.awt.Color.YELLOW)
                Timestamp(Instant.now())
            }
            Button(label = "Click me!") {
                it.buttonInteraction.acknowledge()
                clicks += 1
            }
        }
    }
}
```