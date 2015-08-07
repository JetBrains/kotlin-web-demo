fun doSomethingStrangeWithCollection(collection: Collection<String>): Collection<String>? {

    val groupsByLength = collection. groupBy { s -> TODO() }

    val maximumSizeOfGroup = groupsByLength.values().map { group -> TODO() }.max()

    return groupsByLength.values().firstOrNull { group -> TODO() }
}