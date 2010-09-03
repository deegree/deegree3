#!/bin/sh

# TODO check for JAVA

bin/catalina.sh run &

# TODO proper browser startup after Tomcat started
sleep 5
firefox http://127.0.0.1:8080
