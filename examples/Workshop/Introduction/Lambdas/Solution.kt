package i_introduction._3_Lambdas

fun containsEven(collection: Collection<Int>): Boolean = collection.any { it % 2 == 0 }
