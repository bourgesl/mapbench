#!/bin/bash
# get paths and jvm settings:
source ./env.sh

# Pisces renderer in lib folder:
BOOTCLASSPATH="-Xbootclasspath/a:../lib/openjdk8-pisces.jar"

# Update Java options:
JAVA_OPTS="-Dsun.java2d.renderer=sun.java2d.pisces.PiscesRenderingEngine $JAVA_OPTS"

echo "CLASSPATH:   $CLASSPATH"
echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $JAVA_OPTS"
echo "Java tuning: $JAVA_TUNING"

echo "JVM path"
which java

echo "Java version"
java -version

java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench
