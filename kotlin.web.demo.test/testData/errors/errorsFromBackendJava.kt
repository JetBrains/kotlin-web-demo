import kotlin.properties.Delegates

class Example {
    val p: String by Delegate()

    override fun toString() = "Example Class"
}

class Delegate() {
    fun get(thisRef: Any?, prop: String): String {
        return "$thisRef, thank you for delegating '${prop.name}' to me!"
    }
}