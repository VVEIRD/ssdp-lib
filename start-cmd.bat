pushd %~dp0
set path=C:\tools\apache-maven-3.6.0\bin;%PATH%
set JAVA_HOME=C:\tools\jdk-10.0.2
@echo off
cls
echo Run: mvn exec:java
echo Package: mvn package
cmd