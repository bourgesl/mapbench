#!/bin/bash
# get paths and jvm settings:
source ./env.sh

#source ~/test-jdk9-sh.sh 
source ~/test-jdk9-cl.sh
#source ~/test-jdk9-shed.sh

# -XX:-UseBiasedLocking -XX:+PrintSafepointStatistics -XX:ShenandoahGCHeuristics=passive
#JAVA_TUNING=" -Xms4g -Xmx4g -XX:+AlwaysPreTouch -XX:+UseTransparentHugePages -XX:+UseNUMA -XX:+UseShenandoahGC -Xlog:gc -Xlog:gc+ergo -Xlog:gc+stats"

# bad option: -XX:+PrintReferenceGC
#JAVA_TUNING=" -Xms1g -Xmx1g -XX:+AlwaysPreTouch -XX:+UseShenandoahGC -XX:ShenandoahGCHeuristics=passive -XX:+UnlockDiagnosticVMOptions -XX:-ShenandoahSATBBarrier -XX:-ShenandoahReadBarrier -XX:-ShenandoahWriteBarrier -XX:-ShenandoahAcmpBarrier -XX:-ShenandoahCASBarrier -XX:-ShenandoahCloneBarrier"


#JAVA_TUNING=" -Xms1g -Xmx1g -XX:+UseConcMarkSweepGC"

#JAVA_TUNING=" -Xms1g -Xmx1g -XX:+AlwaysPreTouch -XX:+UseTransparentHugePages -XX:+UseNUMA -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xlog:gc -Xlog:gc+ergo -Xlog:gc+stats"


#JAVA_TUNING=" -Xms1g -Xmx1g -XX:+UseSerialGC"
JAVA_TUNING=" -Xms1g -Xmx1g -XX:+UseParallelGC"


#JAVA_TUNING="$JAVA_TUNING -Xlog:gc -Xlog:gc+ergo -Xlog:gc+stats"


JAVA_OPTS="-Dsun.java2d.renderer.log=false -Dsun.java2d.renderer.doStats=false -Dsun.java2d.renderer.pixelsize=2160 $JAVA_OPTS"

echo "CLASSPATH:   $CLASSPATH"
echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $JAVA_OPTS"
echo "Java tuning: $JAVA_TUNING"

echo "JVM path"
which java

echo "Java version"
java -version

java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench
