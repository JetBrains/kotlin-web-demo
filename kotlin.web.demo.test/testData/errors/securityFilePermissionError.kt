fun main(args : Array<String>) {
    var file = java.io.File("test.kt")
    if (!file.exists()) {
        file.createNewFile()
    }
}  