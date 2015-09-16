import kotlin.properties.Delegates

<answer>
class Commodity(data: MutableMap<String, Any?>) {
    val description: String by Delegates.mapVal(data)
    var price: Int by Delegates.mapVar(data)
    var isAvailable: Boolean by Delegates.mapVar(data)
}
</answer>
