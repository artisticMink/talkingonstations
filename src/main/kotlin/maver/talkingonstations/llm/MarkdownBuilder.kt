package maver.talkingonstations.llm

/**
 * Simple markdown string builder
 */
class MarkdownBuilder {
    private val sb: StringBuilder = StringBuilder()

    /**
     * Allows composing nested markdowns using +
     *
     * Example:
     *      val foo = markdown {
     *          h1("Foo")
     *          +markdown {...}
     *      }
     */
    operator fun String.unaryPlus() = apply { sb.append(this@unaryPlus).append("\n") }

    fun h1(text: String) = apply { sb.append("# ").append(text).append("\n") }
    fun h2(text: String) = apply { sb.append("## ").append(text).append("\n") }
    fun h3(text: String) = apply { sb.append("### ").append(text).append("\n") }
    fun h4(text: String) = apply { sb.append("#### ").append(text).append("\n") }
    fun h5(text: String) = apply { sb.append("##### ").append(text).append("\n") }
    fun l(text: String) = apply { sb.append(text).append("\n") }
    fun p(text: String)  = apply { sb.append(text).append("\n\n") }
    fun line() = apply { sb.append("\n\n") .append("---").append("\n\n") }
    fun list(items: Iterable<String>) = apply {
        items.forEach { sb.append("- ").append(it).append('\n') }
        sb.append('\n')
    }

    override fun toString(): String = sb.toString().trimEnd() + "\n"
}

fun markdown(block: MarkdownBuilder.() -> Unit): String = MarkdownBuilder().apply(block).toString()