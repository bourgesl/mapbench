#!/bin/bash
# get paths and jvm settings:
source ./env.sh

# get marlin settings and boot class path:
source ./env_marlin.sh

# Enable stats
STATS=false
JAVA_OPTS="-Dsun.java2d.renderer.doChecks=true -Dsun.java2d.renderer.doStats=$STATS $JAVA_OPTS"

echo "CLASSPATH:   $CLASSPATH"
echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $JAVA_OPTS"
echo "Java tuning: $JAVA_TUNING"

echo "JVM path"
which java

echo "Java version"
java -version

#FILTER="-Dfilter.type=BELL -Dfilter.blur=1.0"

#MARLIN_G2D="-DMarlinGraphics.blendComposite=true -DMapBench.useMarlinGraphics2D=true"
# $FILTER $MARLIN_G2D 
java $MARLIN_G2D -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapDisplay
