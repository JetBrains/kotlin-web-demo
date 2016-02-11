import org.junit.Assert
import org.junit.Test
import java.util.*
import koans.util.errorMessage

public class TestGenericFunctions {
    @Test fun testPartitionWordsAndLines() {
        partitionWordsAndLines()

        val (words, lines) = listOf("a", "a b", "c", "d e").
                partitionTo(ArrayList<String>(), ArrayList()) { s -> !s.contains(" ") }
        Assert.assertEquals(errorMessage("partitionTo"), listOf("a", "c"), words)
        Assert.assertEquals(errorMessage("partitionTo"), listOf("a b", "d e"), lines)
    }

    @Test fun testPartitionLettersAndOtherSymbols() {
        partitionLettersAndOtherSymbols()

        val (letters, other) = setOf('a', '%', 'r', '}').
                partitionTo(HashSet<Char>(), HashSet()) { c -> c in 'a'..'z' || c in 'A'..'Z'}
        Assert.assertEquals(errorMessage("partitionTo"), setOf('a', 'r'), letters)
        Assert.assertEquals(errorMessage("partitionTo"), setOf('%', '}'), other)
    }
}