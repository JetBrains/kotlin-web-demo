package ch04.ex1_4_3_InnerAndNestedClassesNestedByDefault2

class Outer {
    inner class Inner {
        fun getOuterReference(): Outer = this@Outer
    }
}
