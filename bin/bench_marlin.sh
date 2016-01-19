#!/bin/bash
# get paths and jvm settings:
source ./env.sh

# get marlin settings and boot class path:
source ./env_marlin.sh

echo "CLASSPATH:   $CLASSPATH"
echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $JAVA_OPTS"
echo "Java tuning: $JAVA_TUNING"

echo "JVM path"
which java

echo "Java version"
java -version

USE_MARLIN_G2D=false
java -DMapBench.useMarlinGraphics2D=$USE_MARLIN_G2D -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING $MARLIN_CUSTOM -cp $CLASSPATH it.geosolutions.java2d.MapBench
