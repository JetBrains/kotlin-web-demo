namespace demo

class Main() {
    fun aaa() : Int {
        return 10
    }
}

fun main(args : Array<String>) {
    val i = 0;
    for(i in 1..1000000000) {
       Main().aaa()
    }
}