class Invokable {
    var numberOfInvocations: Int = 0
        private set
    operator fun invoke(): Invokable {
        <taskWindow>TODO()</taskWindow>
    }
}

fun invokeTwice(invokable: Invokable) = invokable()()