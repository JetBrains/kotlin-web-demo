[![official JetBrains project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)

This is the source for [try.kotl.in]( http://try.kotlinlang.org/)

# Filing Bugs
We use [YouTrack](http://youtrack.jetbrains.com/issues/KT#) for bug reports and suggestions on improving the Web Demo. 
[Click here to report an issue.](https://youtrack.jetbrains.com/newIssue?project=KT&clearDraft=true&c=Subsystems+Web+Site&c=subtask+of+KT-2555) If you need help with compiling or running the project locally, please join the #kontributors channel in the [Kotlin Slack](http://slack.kotlinlang.org), and we'll be happy to help you there.

## Installation
try.kotl.in is running on [Tomcat](https://tomcat.apache.org/). You can setup Tomcat locally using install_tomcat.xml.
If you are installing tomcat manually, you need to download [ConnectorJ](http://dev.mysql.com/downloads/connector/j/) lib 
and copy jar file to tomcat libraries.

try.kotl.in frontend part uses bower as a dependency manager. To fetch this dependencies you have to install
 [node](https://nodejs.org/) and run `npm install` in kotlin.web.demo.frontend folder.

You need MySql database to launch try.kotl.in, schema can be found [here](docker/db/files/init_db.sql).

## Building
try.kotl.in is IDEA project. 

try.kotl.in uses Kotlin for frontend, so latest bootstrap version of Kotlin plugin
 should be installed (see "Note for contributors" at [Kotlin repository](https://github.com/JetBrains/Kotlin#pre-built-plugin)).
 
To build this project, first time you try to build you need to run update_dependencies.xml ant script,
that will download Kotlin for user projects.

## Artifacts
try.kotl.in consists of two war artifacts:

 - WebDemoWar - frontend server that communicates with user, forwards kotlin-related requests to backend
 - WebDemoBackend - backend server that processes requests related with Kotlin (run, highlight, etc.)

## Environment
You need to pass some environment variables to application using tomcat context.xml. Use the following templates to 
create context files: 

 - [context for frontend](kotlin.web.demo.server/src/main/webapp/META-INF/context.template.xml)
 - [context for backend](kotlin.web.demo.backend/src/main/webapp/META-INF/context.template.xml)

context.xml files should be placed near the associated templates.


## Run
To run Web Demo you should build both artifacts and deploy them using Tomcat.
If you have installed Tomcat locally with init.xml and using IDEA, WebDemoServer run configuration can be used.

