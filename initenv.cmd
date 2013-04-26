@echo off

rem author: Sven Wiegand
rem
rem Sets up all required environment variables for android development.
rem
rem To work correctly, this script requires two environment variables to be
rem set before called:
rem ANT_HOME: Path to the ANT installation directory
rem JDK_HOME: Path to the JDK installation
rem ANDROID_HOME: Path to the Android SDK

set SRC_HOME=%CD%
set PATH=%SRC_HOME%\GoodNews\scripts;%ANT_HOME%\bin;%ANDROID_HOME%\tools;%ANDROID_HOME%\platform-tools;%JDK_HOME%\bin;%PATH%
set JAVA_HOME=%JDK_HOME%
set ANT_OPTS="-Dos.name=Windows_NT"
set CLASSPATH=

echo android development environment has been set up for %SRC_HOME%
