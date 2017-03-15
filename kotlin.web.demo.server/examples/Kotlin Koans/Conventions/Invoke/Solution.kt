<answer>class Invokable {
    var numberOfInvocations: Int = 0
        private set
    operator fun invoke(): Invokable {
        numberOfInvocations++
        return this
    }
}</answer>

fun invokeTwice(invokable: Invokable) = invokable()()