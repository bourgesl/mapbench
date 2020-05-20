#!/bin/bash
# get paths and jvm settings:
source ./env.sh

# Oracle JDK10
source ~/test-jdk10.sh

#PATCH="--patch-module javafx.graphics=../lib/marlinfx-0.8.2-Unsafe-OpenJDK9.jar"
PATCH="--patch-module javafx.graphics=../lib/marlinfx-0.9.3.1-Unsafe-OpenJDK9.jar --add-modules=jdk.unsupported,java.logging --add-reads=javafx.graphics=jdk.unsupported,java.logging"

VERBOSE=true

MARLIN_OPTS="-Dprism.marlinrasterizer=true -Dprism.marlin.double=true -Dprism.rasterizerorder=marlin -Dprism.marlin.log=true -Dprism.marlin.doChecks=false -Dprism.marlin.doStats=false -Dprism.marlin.profile=quality"
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
java -version

# FxDEMO window:
FXDEMO="-Dfxdemo.width=1000 -Dfxdemo.height=800"

java $PATCH $FXDEMO -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING -cp ../lib/mapbench-fx-0.1.0.jar:$CLASSPATH it.geosolutions.java2d.MapDemoFX

