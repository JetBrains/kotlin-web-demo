class Invokable {
    public var numberOfInvocations: Int = 0
        private set
    public fun invoke(): Invokable = TODO()
}

fun invokeTwice(invokable: Invokable) = invokable()()