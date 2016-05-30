fun doSomethingStrangeWithCollection(collection: Collection<String>): Collection<String>? {

    val groupsByLength = collection. groupBy { s -> <taskWindow>TODO()</taskWindow> }

    val maximumSizeOfGroup = groupsByLength.values.map { group -> <taskWindow>TODO()</taskWindow> }.max()

    return groupsByLength.values.firstOrNull { group -> <taskWindow>TODO()</taskWindow> }
}