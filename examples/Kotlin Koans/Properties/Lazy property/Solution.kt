<answer>class LazyProperty(val initializer: () -> Int) {
    private val lazyValue: Int? = null
        get() {
            if (field == null) field = initializer()
            return field
        }

    val lazy: Int
        get() = lazyValue!!
}</answer>