#!/bin/bash
# get paths and jvm settings:
source ./env.sh

# get marlin settings and boot class path:
source ./env_marlin_ojdk.sh

# Enable stats
CHECK=false
STATS=true
MONITOR=false
JAVA_OPTS="-Dsun.java2d.renderer.doChecks=$CHECK -Dsun.java2d.renderer.doMonitors=$MONITOR -Dsun.java2d.renderer.doStats=$STATS $JAVA_OPTS"

echo "CLASSPATH:   $CLASSPATH"
echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $JAVA_OPTS"
echo "Java tuning: $JAVA_TUNING"

echo "JVM path"
which java

echo "Java version"
java -version

#MARLIN_G2D="-DMarlinGraphics.blendComposite=true -DMapBench.useMarlinGraphics2D=true"

java $MARLIN_G2D -DPNGImageWriter.level=4 -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapDisplay
