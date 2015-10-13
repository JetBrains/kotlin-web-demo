## Nullable types

Read about
[null safety and safe calls](http://kotlinlang.org/docs/reference/null-safety.html)
in Kotlin and rewrite the following Java code using only one `if` expression:

```java
public void sendMessageToClient(
    @Nullable Client client,
    @Nullable String message,
    @NotNull Mailer mailer
) {
    if (client == null || message == null) return;

    PersonalInfo personalInfo = client.getPersonalInfo();
    if (personalInfo == null) return;

    String email = personalInfo.getEmail();
    if (email == null) return;

    mailer.sendMessage(email, message);
}
```