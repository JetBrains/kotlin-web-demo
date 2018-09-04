FROM tomcat:8

ADD conf/ /usr/local/tomcat/conf/

RUN ["rm", "-r", "/usr/local/tomcat/webapps"]

RUN apt-get update && apt-get install --assume-yes openjdk-8-jdk

ADD war/WebDemoBackend.war /usr/local/tomcat/webapps/ROOT.war