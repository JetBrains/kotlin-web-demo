FROM tomcat:8

ADD conf/ /usr/local/tomcat/conf/

ADD lib/ /usr/local/tomcat/lib/

RUN ["rm", "-r", "/usr/local/tomcat/webapps"]

ADD war/WebDemoWar.war /usr/local/tomcat/webapps/ROOT.war
