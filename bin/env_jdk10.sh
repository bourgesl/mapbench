#!/bin/sh

# This file defines the JAVA_HOME and PATH pointing to the proper JDK10:

# Oracle JDK10 EA b42
export JAVA_HOME=~/apps/jdk-10/

echo "JAVA_HOME: $JAVA_HOME"

PATH=$JAVA_HOME/bin/:$PATH
export PATH
