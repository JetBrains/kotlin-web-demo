[![official JetBrains project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)

This is the source for [try.kotl.in]( http://try.kotlinlang.org/)

## Manual installation :whale:

Start with command:

```bash
docker-compose up
```

The application will be available on `http://localhost:8080`.

By default try.kotl.in is working on 8080 port.
You can expose it to another port changing the config file 'docker-compose.yml'.
```bash
    ports:
      - "your_port:8080"
```

So, if you need Google/Facebook/GitHub/Twitter of JetBrains account authorization
you have to set key and secret_key in [configuration file](https://github.com/JetBrains/kotlin-web-demo/blob/master/docker/frontend/conf/Catalina/localhost/ROOT.xml).

For example: 
```xml
    <Environment name="github_key" value="YOUR-KEY" type="java.lang.String" override="false"/>
    <Environment name="github_secret" value="YOUR-SECRET-KEY" type="java.lang.String" override="false"/>
```

## How to add your own courses :memo:

  - Add name of your course to [manifest.json](https://github.com/JetBrains/kotlin-web-demo/tree/master/kotlin.web.demo.server/examples).
  - Add the folder with content near the [Examples folder](https://github.com/JetBrains/kotlin-web-demo/tree/master/kotlin.web.demo.server/examples)
  with the same name as in manifest.json.
  - Create folders with the topics of your course and don't forget about `manifest.json` with names of folders.
  - Create 
     1. Test.kt — file with test-methods
     2. Task.kt — file with preview 
     3. Solution.kt — file with solution
     4. task.md - file with description of the task 
     5. manifest.json - with 'junit' [configuration](https://github.com/JetBrains/kotlin-web-demo/blob/master/kotlin.web.demo.server/examples/Kotlin%20Koans/Introduction/Hello%2C%20world!/manifest.json)
     
   See example [Kotlin-Koans](https://github.com/JetBrains/kotlin-web-demo/tree/master/kotlin.web.demo.server/examples/Kotlin%20Koans) course.
  
## How to add your dependencies to kotlin compiler :books:
 
In order to add your libraries you should append dependencies to gradle.build. 
For example, you may add your dependency with the help of gradle-task called `kotlinLibs ` 
to [gradle.build](https://github.com/JetBrains/kotlin-web-demo/blob/master/versions/1.1.60/build.gradle) 
in compiler 1.1.60 like this:
```gradle
 kotlinLibs "your dependency"
```

FYI: Pay attention if yor library has got reflections, work with files and etc.
Please configure the [executors.policy.template](https://github.com/JetBrains/kotlin-web-demo/blob/master/kotlin.web.demo.backend/src/main/resources/executors.policy.template) 
in web-demo-backend.
[Follow the link](https://docs.oracle.com/javase/7/docs/technotes/guides/security/PolicyFiles.html)
for getting more information about *Java Security Police*.

**How to set Java Security Police in `executors.policy.template`**

If you want to add own dependency please use marker  `@WRAPPERS_LIB@`

For example:
```     
grant codeBase "file:@WRAPPERS_LIB@/junit-4.12.jar" {
  permission java.lang.reflect.ReflectPermission "suppressAccessChecks";
  permission java.lang.RuntimePermission "setIO";
  permission java.io.FilePermission "<<ALL FILES>>", "read";
  permission java.lang.RuntimePermission "accessDeclaredMembers";
};
```    

## Current development status :construction_worker:
We try to make the try.kotl.in more easier for adding your own courses. 
If you have some ideas and suggestions for improving the Kotlin-Web-Demo please create the 
[issues](https://youtrack.jetbrains.com/newIssue?project=KT&clearDraft=true&c=Subsystems+Web+Site&c=subtask+of+KT-2555).
        
## Filing Bugs :beetle:
We also use [YouTrack](http://youtrack.jetbrains.com/issues/KT#) for bug reports. 
[Click here to report an issue.](https://youtrack.jetbrains.com/newIssue?project=KT&clearDraft=true&c=Subsystems+Web+Site&c=subtask+of+KT-2555) If you need help with compiling or running the project locally, please join the #kontributors channel in the [Kotlin Slack](http://slack.kotlinlang.org), and we'll be happy to help you there.

    
   
     
     
  







