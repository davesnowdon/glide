Planet IDE
==========

An Integrated Development Environment for kids.

The aim of this project is to give children (aged 10 and upwards) a tool to write code.
It is language agnostic, but as a first pass will only support groovy.

To build

    ./gradlew clean build dist

To run 

    ./run.sh
 
To upload jars to nexus you need to have nexusUsername and nexusPassword defined in ~/.gradle/gradle.properties, then

    ./gradlew uploadArchive

This uploads to the nexus repo here

    http://leonandjosh.ddns.net:8081/nexus

To package up a linux version of the app

    Download the linux jdk from oracle and zip into jdk.zip in the root folder of this project
    java -jar packr.jar packr-linux.json

Troubleshooting
---------------
DesktopLauncher not found - Means you did a gradle build without the dist

