@echo off
REM Adjust the Java path if needed:
set JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.7.7-hotspot"

REM Path to the .jar file (relative to this .bat file)
set JAR_FILE="target\PriceDataVisualizer-1.0-jar-with-dependencies.jar"

REM Run the application
%JAVA_HOME%\bin\java.exe -jar %JAR_FILE%
