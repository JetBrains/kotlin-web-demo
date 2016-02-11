package koans.util

fun String.toMessage() = "The function '$this' is implemented incorrectly"

fun String.toMessageInEquals() = toMessage().inEquals()

fun String.inEquals() = this + ":" + if (mode == Mode.WEB_DEMO) " " else "<br><br>"

private enum class Mode { WEB_DEMO, EDUCATIONAL_PLUGIN }
private val mode = Mode.WEB_DEMO
