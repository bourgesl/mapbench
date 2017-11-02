#!/bin/bash
# get paths and jvm settings:
source ./env.sh

VERBOSE=false

MARLIN_OPTS="-Dprism.marlin=true -Dprism.marlin.double=true -Dprism.marlin.log=true -Dprism.marlin.doChecks=false -Dprism.marlin.doStats=false"
PRISM_OPTS="-Dprism.order=es2,sw -Dprism.nativepisces=false -Dprism.verbose=$VERBOSE -Djavafx.animation.fullspeed=true"

BOOTCLASSPATH=-Xbootclasspath/p:../lib/marlinfx-0.8.0-Unsafe.jar

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

java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING -cp ../lib/mapbench-fx-0.1.0.jar:$CLASSPATH it.geosolutions.java2d.MapDemoFX

