package vi_generics.generics

import java.util.ArrayList
import java.util.HashSet

fun partitionTo() = throw Exception("Not implemented")

fun List<String>.partitionWordsAndLines(): Pair<kotlin.List<kotlin.String>, kotlin.List<kotlin.String>> {
    return partitionTo(ArrayList<String>(), ArrayList()) { s -> !s.contains(" ") }
}

fun Set<Char>.partitionLettersAndOtherSymbols(): Pair<kotlin.Set<kotlin.Char>, kotlin.Set<kotlin.Char>> {
    return partitionTo(HashSet<Char>(), HashSet()) { c -> c in 'a'..'z' || c in 'A'..'Z'}
}