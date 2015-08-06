import kotlin.test.*
import org.junit.Test as test
import org.junit.Assert


public class TestDataClasses {
    test fun testListOfPeople() {
        Assert.assertEquals("[Person(name=Alice, age=29), Person(name=Bob, age=31)]", getPeople().toString())
    }
}