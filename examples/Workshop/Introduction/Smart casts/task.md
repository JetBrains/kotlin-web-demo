##Smart casts
Rewrite the following Java code using
[smart casts](http://kotlinlang.org/docs/reference/typecasts.html#smart-casts)
and
[when](http://kotlinlang.org/docs/reference/control-flow.html#when-expression).

```java
public String print(Expr expr) {
    if (expr instanceof Num) {
        return "" + ((Num) expr).getValue();
    }
    if (expr instanceof Sum) {
        Sum sum = (Sum) expr;
        return print(sum.getLeft()) + " + " + print(sum.getRight());
    }
    throw new IllegalArgumentException("Unknown expression");
}
```