namespace demo
import java.util.List
import java.util.LinkedList
fun main(args : Array<String>) {
    System.out?.println(User().main())
}
open class Member() {

}
open class User() {
    open fun main() : Unit {
        var members : List<Member?>? = LinkedList<Member?>()
        members?.add(Member())
        System.out?.println(members)
    }
}