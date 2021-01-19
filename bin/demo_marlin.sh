#!/bin/bash
# get paths and jvm settings:
source ./env.sh

# get marlin settings and boot class path:
source ./env_marlin.sh

# Enable stats
CHECK=false
STATS=false
MONITOR=false
JAVA_OPTS="$JAVA_OPTS -Dsun.java2d.renderer.doChecks=$CHECK -Dsun.java2d.renderer.doMonitors=$MONITOR -Dsun.java2d.renderer.doStats=$STATS -Dsun.java2d.renderer.compositor=false"

echo "CLASSPATH:   $CLASSPATH"
echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $JAVA_OPTS"
echo "Java tuning: $JAVA_TUNING"

echo "JVM path"
which java

echo "Java version"
java -version

#MARLIN_G2D="-DMarlinGraphics.blendComposite=true -DMapBench.useMarlinGraphics2D=true"

# DEMO window:
# DEMO="-Ddemo.width=800 -Ddemo.height=600"

# -Dsun.java2d.opengl.bufferSize=262144
JAVA_OPTS="$JAVA_OPTS -Dsun.java2d.opengl=true -Dsun.java2d.opengl.bufferSize=262144 -Dsun.java2d.opengl.flushDelay=1"

java $DEMO $MARLIN_G2D -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapDemo

