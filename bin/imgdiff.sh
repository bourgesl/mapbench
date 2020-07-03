#!/bin/bash
# get paths and jvm settings:
source ./env.sh

JAVA_OPTS="$JAVA_OPTS"

echo "CLASSPATH:   $CLASSPATH"
echo "Java opts:   $JAVA_OPTS"
echo "Java tuning: $JAVA_TUNING"

echo "JVM path"
which java

echo "Java version"
java -version

java $JAVA_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.ImageDiffDisplay $1 $2 $3

