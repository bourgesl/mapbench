#!/bin/bash
# get paths and jvm settings:
source ./env.sh

source /home/bourgesl/libs/graphics-rasterizer/openjfx/jfx10.sh

PATCH="--patch-module javafx.graphics=../lib/marlinfx-0.8.2-Unsafe-OpenJDK9.jar"

VERBOSE=false

MARLIN_OPTS="-Dprism.marlinrasterizer=true -Dprism.marlin.double=true -Dprism.rasterizerorder=marlin -Dprism.marlin.log=true -Dprism.marlin.doChecks=false -Dprism.marlin.doStats=false -Dprism.marlin.clip=true"

PRISM_OPTS="-Dprism.order=es2 -Dprism.verbose=$VERBOSE -Djavafx.animation.fullspeed=true"

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
$JIGSAW_HOME/bin/java -version

# FxDEMO window:
FXDEMO="-Dfxdemo.width=600 -Dfxdemo.height=400"

#$JIGSAW_HOME/bin/java @$JFX_XPATCH $PATCH $FXDEMO -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING -cp ../lib/mapbench-fx-0.1.0.jar:$CLASSPATH it.geosolutions.java2d.MapDemoFX
$JIGSAW_HOME/bin/java $PATCH $FXDEMO -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING -cp ../lib/mapbench-fx-0.1.0.jar:$CLASSPATH it.geosolutions.java2d.MapDemoFX

