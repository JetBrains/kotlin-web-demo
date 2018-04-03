[![official JetBrains project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)

This repository contains sources for [try.kotl.in]( http://try.kotlinlang.org/)

## Manual installation :whale:
Before starting the Kotlin-Web-Demo execute two gradle tasks: `copyKotlinLibs` for downloading kotlin libraries for compiler and 
`war` for building war-archive from IDE or terminal: 
```bash
$ gradlew ::copyKotlinLibs
$ gradlew war
```

By default `try.kotl.in` uses port 8080, and if that's OK, just make it available at `http://localhost:8080` via this command:

```bash
$ docker-compose up
```

To change the port number, tweak 'docker-compose.yml':

```bash
    ports:
      - "your_port:8080"
```

If you'd like to log in to JetBrains account, Google, Facebook, GitHub, or Twitter, add corresponding keys to
this [configuration file](https://github.com/JetBrains/kotlin-web-demo/blob/master/docker/frontend/conf/Catalina/localhost/ROOT.xml):

```xml
    <Environment name="github_key" value="YOUR-KEY" type="java.lang.String" override="false"/>
    <Environment name="github_secret" value="YOUR-SECRET-KEY" type="java.lang.String" override="false"/>
```

## How to add your own courses :memo:

  - Add a course name to [manifest.json](https://github.com/JetBrains/kotlin-web-demo/tree/master/kotlin.web.demo.server/examples).
  - Use that name to create a folder next to the [Examples folder](https://github.com/JetBrains/kotlin-web-demo/tree/master/kotlin.web.demo.server/examples)
  and put your course content under it.
  - Make a folder for each of the course topics (and don't forget adding them to `manifest.json`).
  - After that, create:
     1. Test.kt — for test questions
     2. Task.kt — for preview
     3. Solution.kt — for answers to the test questions
     4. task.md - tasks descriptions
     5. manifest.json - to store 'junit' [configuration](https://github.com/JetBrains/kotlin-web-demo/blob/master/kotlin.web.demo.server/examples/Kotlin%20Koans/Introduction/Hello%2C%20world!/manifest.json)

   See [Kotlin-Koans](https://github.com/JetBrains/kotlin-web-demo/tree/master/kotlin.web.demo.server/examples/Kotlin%20Koans) for examples.

## How to add your dependencies to kotlin compiler :books:

Just put whatever you need as dependencies to [gradle.build](https://github.com/JetBrains/kotlin-web-demo/blob/master/versions/1.1.60/build.gradle) via gradle task called `library`:

```gradle
 library "your dependency"
```

NOTE: If the library you're adding uses reflection, accesses the file system, or performs any other type of security-sensitive operations, don't forget to
configure the [executors.policy.template](https://github.com/JetBrains/kotlin-web-demo/blob/master/kotlin.web.demo.backend/src/main/resources/executors.policy.template)
in `web-demo-backend`. [Click here](https://docs.oracle.com/javase/7/docs/technotes/guides/security/PolicyFiles.html) for more information about *Java Security Police*

**How to set Java Security Police in `executors.policy.template`**

If you want to a customm dependency, use the marker `@WRAPPERS_LIB@`:

```
grant codeBase "file:@WRAPPERS_LIB@/junit-4.12.jar" {
  permission java.lang.reflect.ReflectPermission "suppressAccessChecks";
  permission java.lang.RuntimePermission "setIO";
  permission java.io.FilePermission "<<ALL FILES>>", "read";
  permission java.lang.RuntimePermission "accessDeclaredMembers";
};
```

## Feedback and Issue reporting :construction_worker:

We're constantly working on making it easier to add your own courses to `try.kotl.in` and would appreciate ideas, suggestions,
and other feedback, so if you have any, please [use our issue tracker](https://youtrack.jetbrains.com/issues/KT#newissue=25-1925867) to share it with us.

And, of course, if you have any bug reports, you can [file them as well](https://youtrack.jetbrains.com/issues/KT#newissue=25-1925867)
If you need any help with compiling or running the project locally, join the `#kontributors` channel in the [Kotlin Slack](http://slack.kotlinlang.org), and we'll be happy to help you out.
