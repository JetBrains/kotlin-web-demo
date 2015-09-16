<answer>class Invokable {
    public var numberOfInvocations: Int = 0
        private set
    public fun invoke(): Invokable {
        numberOfInvocations++
        return this
    }
}</answer>

fun invokeTwice(invokable: Invokable) = invokable()()