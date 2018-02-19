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
   
## Current development status :construction_worker:
We try to make the try.kotl.in more easier for adding your own courses. 
If you have some ideas and suggestions for improving the Kotlin-Web-Demo please create the 
[issues](https://youtrack.jetbrains.com/newIssue?project=KT&clearDraft=true&c=Subsystems+Web+Site&c=subtask+of+KT-2555).
        
## Filing Bugs :beetle:
We also use [YouTrack](http://youtrack.jetbrains.com/issues/KT#) for bug reports. 
[Click here to report an issue.](https://youtrack.jetbrains.com/newIssue?project=KT&clearDraft=true&c=Subsystems+Web+Site&c=subtask+of+KT-2555) If you need help with compiling or running the project locally, please join the #kontributors channel in the [Kotlin Slack](http://slack.kotlinlang.org), and we'll be happy to help you there.

    
   
     
     
  







