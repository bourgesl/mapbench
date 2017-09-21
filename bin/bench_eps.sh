#!/bin/bash
# get paths and jvm settings:
source ./env.sh

source ~/test-jdk10-eps.sh

# -XX:-UseBiasedLocking -XX:+PrintSafepointStatistics -XX:ShenandoahGCHeuristics=passive
JAVA_TUNING=" -Xms4g -Xmx4g -XX:+AlwaysPreTouch -XX:+UseTransparentHugePages -XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC -Xlog:gc -Xlog:gc+ergo -Xlog:gc+stats"
#JAVA_TUNING=" -Xms4g -Xmx4g -XX:+UseConcMarkSweepGC -XX:+AlwaysPreTouch -XX:+UseTransparentHugePages -XX:+PrintSafepointStatistics -Xlog:gc"

#JAVA_TUNING=" -Xms1g -Xmx1g -XX:+AlwaysPreTouch -XX:+UseTransparentHugePages -XX:+UseNUMA -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xlog:gc -Xlog:gc+ergo -Xlog:gc+stats"

JAVA_OPTS="-Dsun.java2d.renderer=sun.java2d.marlin.MarlinRenderingEngine -Dsun.java2d.renderer.log=true $JAVA_OPTS"

echo "CLASSPATH:   $CLASSPATH"
echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $JAVA_OPTS"
echo "Java tuning: $JAVA_TUNING"

echo "JVM path"
which java

echo "Java version"
java -version

java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench
