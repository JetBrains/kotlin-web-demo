## Data classes

Convert the following Java code to Kotlin:

```java
public static class Person {
    private final String name;
    private final int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
```

Then add an annotation `data` to the resulting class.
This annotation means the compiler will generate a bunch of useful methods in this class: `equals`/`hashCode`, `toString` and some others.
The `getPeople` function should start to compile.

Then read about [classes](http://kotlinlang.org/docs/reference/classes.html),
[properties](http://kotlinlang.org/docs/reference/properties.html)
and [data classes](https://kotlinlang.org/docs/reference/data-classes.html) in more detail.