#!/bin/bash
# get paths and jvm settings:
source ./env.sh

source ./env_base.sh

echo "CLASSPATH:   $CLASSPATH"
echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $JAVA_OPTS"
echo "Java tuning: $JAVA_TUNING"


echo "JVM path"
which java

echo "Java version"
java -version

# DEMO window:
# DEMO="-Ddemo.width=800 -Ddemo.height=600"

JAVA_OPTS="$JAVA_OPTS -Dsun.java2d.opengl=True -Dsun.java2d.opengl.bufferSize=4194304 -Dsun.java2d.opengl.flushDelay=100"

echo "CMD: java $DEMO $MARLIN_G2D -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapDemo"

taskset -c $CPU_CORE_IDS java $DEMO $MARLIN_G2D -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapDemo

