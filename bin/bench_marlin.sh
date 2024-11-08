#!/bin/bash
# get paths and jvm settings:
source ./env.sh

# get marlin settings and boot class path:
source ./env_marlin.sh

# Enable stats
CHECK=false
STATS=false
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

# taskset -c $CPU_CORE_IDS 
java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench

