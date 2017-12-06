import org.junit.Test
import org.junit.Assert


class TestDataClasses {
    @Test fun testListOfPeople() {
        Assert.assertEquals("[Person(name=Alice, age=29), Person(name=Bob, age=31)]", getPeople().toString())
    }
}