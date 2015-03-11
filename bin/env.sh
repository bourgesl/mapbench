#!/bin/sh

# set profile
PROFILE=default.properties

# use shared image (less gc overhead but less realistic)
PROFILE=sharedImage.properties

# longer (shared 4T) test:
PROFILE=longer_shared.properties

# faster (shared 4T) test:
#PROFILE=faster_shared.properties

# Use translation to add margin (no clipping = all shapes are visible)
#PROFILE=longer_shared_tx.properties

# Use dashed stroke
#PROFILE=longer_shared_dashed.properties

#PROFILE=addMargin.properties

# use shared image but single thread only
#PROFILE=shared_1T.properties

# scaling test (1T but image x4):
#PROFILE=scaleTest.properties

# even_odd winding rule test:
#PROFILE=evenOddWindingRule.properties

# complex affine transform test:
#PROFILE=cpxTransform.properties

# use createStrokedShape() + fill() test:
#PROFILE=strokedShape.properties

# use shared image and only 1 thread for insane maps:
#PROFILE=insane_1T.properties

# set paths
# MapBench jar:
MAP_BENCH_JAR=../lib/mapbench-0.4.0.jar


# server jvm
#JAVA_OPTS="-XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal"
JAVA_OPTS="-server"

# headless mode
#JAVA_OPTS="-server -Djava.awt.headless=true"

# Show compilation to diagnose warmup issue:
#JAVA_OPTS="-server -Djava.awt.headless=true -XX:+PrintCompilation"

# Disable tiered compilation to get stable results (no background compiler task):
#JAVA_OPTS="-server -Djava.awt.headless=true -XX:-TieredCompilation -XX:-BackgroundCompilation"
#JAVA_OPTS="-server -Djava.awt.headless=true -XX:-TieredCompilation -XX:+PrintCompilation -XX:-BackgroundCompilation"

#JAVA_TUNING=" -Xms256m  -Xmx256m"
#JAVA_TUNING=" -Xms256m  -Xmx256m -XX:-TieredCompilation"
#JAVA_TUNING=" -Xms128m  -Xmx128m -XX:+AggressiveOpts -XX:CompileThreshold=1000"

#JAVA_TUNING=" -Xms2048m  -Xmx2048m"
JAVA_TUNING=" -Xms2048m  -Xmx2048m -XX:+UseConcMarkSweepGC"
#JAVA_TUNING=" -Xms2048m  -Xmx2048m -XX:-TieredCompilation"

# -ea -XX:+AggressiveOpts"
#JAVA_TUNING=" -Xms2048m  -Xmx2048m -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:InitiatingHeapOccupancyPercent=60"
# -verbose:gc"

#JAVA_TUNING=" -Xms2048m  -Xmx2048m -XX:+UseG1GC"

# "-XX:+UseParNewGC -XX:+CMSParallelRemarkEnabled"
#JAVA_TUNING=" -Xms2048m  -Xmx2048m -ea -XX:+AggressiveOpts -XX:+UseConcMarkSweepGC"


# MapBench jar file:
CLASSPATH=$MAP_BENCH_JAR

# Reset Boot classpath:
BOOTCLASSPATH=""
