class PropertyExample() {
    var counter = 0
    var propertyWithCounter: Int? = null
        set(v: Int?) {
            $propertyWithCounter = v
            counter++
        }
}