##Nullable types
In Kotlin the type system distinguishes between references that can hold null (nullable references)
and those that can not (non-null references).

Read more about
[null safety](http://kotlinlang.org/docs/reference/null-safety.html#null-safety) and
[safe calls](http://kotlinlang.org/docs/reference/null-safety.html#safe-calls)
in Kotlin and rewrite the following Java code using only one `if`.

```java
public void sendMessageToClient(
    @Nullable Client client, @Nullable String message, @NotNull Mailer mailer
) {
    if (client == null || message == null) return;

    PersonalInfo personalInfo = client.getPersonalInfo();
    if (personalInfo == null) return;

    String email = personalInfo.getEmail();
    if (email == null) return;

    mailer.sendMessage(email, message);
}
```