#!/bin/sh

# set profile
PROFILE=default.properties

# use shared image (less gc overhead but less realistic)
#PROFILE=sharedImage.properties

# use shared image but single thread only
#PROFILE=shared_1T.properties

# scaling test (1T but image x4):
#PROFILE=scaleTest.properties

# even_odd winding rule test:
#PROFILE=evenOddWindingRule.properties

# complex affine transform test:
#PROFILE=cpxTransform.properties

# use shared image and only 1 thread for insane maps:
#PROFILE=insane_1T.properties

# set paths
# MapBench jar:
MAP_BENCH_JAR=../lib/mapbench-0.4.0.jar


# server jvm
JAVA_OPTS="-server"
# -XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal"

# headless mode
#JAVA_OPTS="-server -Djava.awt.headless=true -XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal"
#JAVA_OPTS="-XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal"

#JAVA_TUNING=" -Xms256m  -Xmx256m -XX:-TieredCompilation"
#JAVA_TUNING=" -Xms128m  -Xmx128m -XX:+TieredCompilation"
#JAVA_TUNING=" -Xms128m  -Xmx128m -XX:+AggressiveOpts -XX:CompileThreshold=1000"

JAVA_TUNING=" -Xms2048m  -Xmx2048m -XX:+UseConcMarkSweepGC"
# -ea -XX:+AggressiveOpts"
#JAVA_TUNING=" -Xms2048m  -Xmx2048m -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:InitiatingHeapOccupancyPercent=60"
# -verbose:gc"

# "-XX:+UseParNewGC -XX:+CMSParallelRemarkEnabled"
#JAVA_TUNING=" -Xms2048m  -Xmx2048m -ea -XX:+AggressiveOpts -XX:+UseConcMarkSweepGC"


# MapBench jar file:
CLASSPATH=$MAP_BENCH_JAR

# Reset Boot classpath:
BOOTCLASSPATH=""
