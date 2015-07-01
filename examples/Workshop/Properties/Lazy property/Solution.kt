package iii_properties

class LazyProperty(val initializer: () -> Int) {
    private val lazyValue: Int? = null
        get() {
            if ($lazyValue == null) $lazyValue = initializer()
            return $lazyValue
        }
    val lazy: Int
        get() = lazyValue!!
}