#!/bin/bash

sed -i "s|SERVER_DOMAIN_NAME|${SERVER_DOMAIN_NAME}|g" /home/tomcat/apache-tomcat-current/conf/Catalina/localhost/ROOT.xml
sed -i "s|MYSQL_USERNAME|${MYSQL_USERNAME}|g" /home/tomcat/apache-tomcat-current/conf/Catalina/localhost/ROOT.xml
sed -i "s|MYSQL_PASSWORD|${MYSQL_PASSWORD}|g" /home/tomcat/apache-tomcat-current/conf/Catalina/localhost/ROOT.xml
sed -i "s|MYSQL_URL|${MYSQL_URL}|g" /home/tomcat/apache-tomcat-current/conf/Catalina/localhost/ROOT.xml
sed -i "s|BACKEND_URL|${BACKEND_URL}|g" /home/tomcat/apache-tomcat-current/conf/Catalina/localhost/ROOT.xml
sed -i "s|APP_DB_NAME|${APP_DB_NAME}|g" /home/tomcat/apache-tomcat-current/conf/Catalina/localhost/ROOT.xml

