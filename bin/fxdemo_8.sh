#!/bin/bash
# get paths and jvm settings:
source ./env.sh

VERBOSE=true

MARLIN_OPTS="-Dprism.marlin=true -Dprism.marlin.double=true -Dprism.marlin.log=true -Dprism.marlin.doChecks=false -Dprism.marlin.doStats=false"
PRISM_OPTS="-Dprism.order=es2,sw -Dprism.nativepisces=false -Dprism.verbose=$VERBOSE -Djavafx.animation.fullspeed=true"

BOOTCLASSPATH=-Xbootclasspath/p:../lib/marlinfx-0.9.3-Unsafe.jar

#MARLIN_OPTS="-Dprism.marlin.clip.subdivider.minLength=0.01 $MARLIN_OPTS"
MARLIN_OPTS="-Dprism.marlin.profile=speed $MARLIN_OPTS"

# Enable stats
CHECK=false
STATS=false
MONITOR=false
JAVA_OPTS="$MARLIN_OPTS $PRISM_OPTS $JAVA_OPTS"

echo "CLASSPATH:   $CLASSPATH"
echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $JAVA_OPTS"
echo "Java tuning: $JAVA_TUNING"

echo "Java version"
java -version

# FxDEMO window:
FXDEMO="-Dfxdemo.width=800 -Dfxdemo.height=600"

java $FXDEMO -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING -cp ../lib/mapbench-fx-0.1.0.jar:$CLASSPATH it.geosolutions.java2d.MapDemoFX

