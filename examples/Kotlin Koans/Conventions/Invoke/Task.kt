class Invokable {
    public var numberOfInvocations: Int = 0
        private set
    public fun invoke(): Invokable = <taskWindow>TODO()</taskWindow>
}

fun invokeTwice(invokable: Invokable) = invokable()()