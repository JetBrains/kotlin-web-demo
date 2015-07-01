##Builders how it works
Look at the questions below and give your answers

1. In the Kotlin code

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
    'td' is:

    1. special built-in syntactic construct
    2. function declaration
    3. function invocation
2. In the Kotlin code

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
    'color' is:

    1. new variable declaration
    1. argument name
    1. argument value
3. The block

    ```kotlin
    {
        text("Product")
    }
    ```
    from the previous question is:

    1. block inside built-in syntax construction 'td'
    2. function literal (or "lambda")
    3. something mysterious
4. For the code

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
    which of the following is true:

    1. this code doesn't compile
    2. 'this' refers to an instance of an outer class
    3. 'this' refers to a receiver parameter TR of the function literal:

    ```kotlin
    tr (color = "yellow") { TR.(): Unit ->
          this.td {
              text("Product")
          }
    }
    ```