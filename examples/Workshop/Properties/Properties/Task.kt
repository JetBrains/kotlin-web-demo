package iii_properties

class PropertyExample() {
    var counter = 0
    var propertyWithCounter: Int? = null
        set(value: Int?) {
            throw Exception("Not implemented")
        }
}