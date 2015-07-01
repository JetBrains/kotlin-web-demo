package iii_properties

class LazyProperty(val initializer: () -> Int) {
    val lazy: Int
    get(){
        throw Exception("Not implemented")
    }
}
