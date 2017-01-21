package ch04.ex4_2_1_CompanionObjectsAPlaceForFactoryMethodsAndStaticMembers

class A {
    companion object {
        fun bar() {
            println("Companion object called")
        }
    }
}

fun main(args: Array<String>) {
    A.bar()
}
