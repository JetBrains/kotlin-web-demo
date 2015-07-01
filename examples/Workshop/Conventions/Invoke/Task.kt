package ii_conventions

class Invokable{
    public var numberOfInvocations: Int = 0
        private set
    public fun invoke(): Invokable = throw Exception("Not implemented")
}

fun invokeTwice(invokable: Invokable) = invokable()()