## Builders: how it works

Look at the questions below and give your answers

**1. In the Kotlin code**

```kotlin
tr {
    td {
        text("Product")
    }
    td {
        text("Popularity")
    }
}
```

**'td' is:**

a. special built-in syntactic construct

b. function declaration

c. function invocation

***

**2. In the Kotlin code**

```kotlin
tr (color = "yellow") {
    td {
        text("Product")
    }
    td {
        text("Popularity")
    }
}
```

**'color' is:**

a. new variable declaration

b. argument name

c. argument value

***

**3. The block**

```kotlin
{
    text("Product")
}
```

**from the previous question is:**

a. block inside built-in syntax construction `td`

b. function literal (or "lambda")

c. something mysterious

***

**4. For the code**

```kotlin
tr (color = "yellow") {
    this.td {
        text("Product")
    }
    td {
        text("Popularity")
    }
}
```

**which of the following is true:**

a. this code doesn't compile

b. `this` refers to an instance of an outer class

c. `this` refers to a receiver parameter TR of the function literal:

```kotlin
tr (color = "yellow") { TR.(): Unit ->
      this.td {
          text("Product")
      }
}
```