#!/bin/sh

# This file defines the JAVA_HOME and PATH pointing to the proper JDK9:

# Oracle JDK9 EA b110
export JAVA_HOME=~/apps/jdk-9/

# Oracle JDK9 EA jigsaw b99+
#export JAVA_HOME=~/apps/jdk-9/


echo "JAVA_HOME: $JAVA_HOME"

PATH=$JAVA_HOME/bin/:$PATH
export PATH
