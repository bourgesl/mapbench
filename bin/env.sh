#!/bin/sh

# set profile
PROFILE=default.properties

# longer (no shared image 4T) test:
#PROFILE=longer_no_shared.properties

# use shared image (less gc overhead but less realistic)
#PROFILE=sharedImage.properties

# longer (shared 4T) test:
PROFILE=longer_shared.properties

# try gradient paint:
#PROFILE=longer_shared_grad.properties

# shorter (shared 4T) test:
#PROFILE=shorter_shared.properties

#PROFILE=shared_nu-tx.properties

# longer (shared 4T) DEMO test:
#PROFILE=longer_shared_demo.properties

# faster (shared 4T) test:
#PROFILE=faster_shared.properties

# Use translation to add margin (no clipping = all shapes are visible)
#PROFILE=longer_shared_tx.properties

# Use dashed stroke
#PROFILE=longer_shared_dashed.properties

#PROFILE=addMargin.properties

# use shared image but single thread only
#PROFILE=shared_1T.properties

# JAM (shorter warmup):
#PROFILE=shared_1T_jam.properties

# scaling test (1T but image x4):
#PROFILE=scaleTest.properties

# even_odd winding rule test:
#PROFILE=evenOddWindingRule.properties

# complex affine transform test:
#PROFILE=cpxTransform.properties
#PROFILE=cpxTransform2.properties

# clip fill shapes
#PROFILE=clipTransform.properties

# use createStrokedShape() + fill() test:
#PROFILE=strokedShape.properties

# use shared image and only 1 thread for insane maps:
#PROFILE=insane_1T.properties

# set paths
# MapBench jar:
MAP_BENCH_JAR=../lib/mapbench-0.4.0.jar
# Marlin Graphics jar:
MARLIN_GRAPHICS_JAR=../lib/marlin-graphics-0.2.3.jar 


# server jvm
JAVA_OPTS="-server"
#JAVA_OPTS=""
#JAVA_OPTS="-XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal"
#JAVA_OPTS="-server -XX:FreqInlineSize=800" # -XX:+PrintCompilation"

#JAVA_OPTS="-server -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:StartFlightRecording=name=mapbench,filename=/home/marlin/mapbench/bin/mapbench.jfr,dumponexit=true -XX:FlightRecorderOptions=defaultrecording=true"

# copyAARowNoRLE_WithTileFlags
#JAVA_OPTS="-server -XX:+UnlockDiagnosticVMOptions -XX:+PrintCompilation -XX:+LogCompilation -XX:CompileCommand=option,copyAARow*,Vectorize"

# Force vectorization on copyAARow...() methods:
#JAVA_OPTS="-server -XX:CompileCommand=option,copyAARow*,Vectorize"

#JAVA_OPTS="-server -XX:+UnlockDiagnosticVMOptions -XX:+TraceClassLoading -XX:+LogCompilation -XX:+PrintAssembly -XX:PrintAssemblyOptions=intel -XX:-TieredCompilation -XX:-UseCompressedOops"
#  -XX:+PrintGCApplicationStoppedTime"

# headless mode
#JAVA_OPTS="-server -Djava.awt.headless=true"

# Show compilation to diagnose warmup issue:
#JAVA_OPTS="-server -Djava.awt.headless=true -XX:+PrintCompilation"

# Disable tiered compilation to get stable results (no background compiler task):
#JAVA_OPTS="-server -XX:-TieredCompilation -XX:-BackgroundCompilation"
#JAVA_OPTS="-server -Djava.awt.headless=true -XX:-TieredCompilation -XX:+PrintCompilation -XX:-BackgroundCompilation"

#JAVA_TUNING=" -Xms256m  -Xmx256m"
#JAVA_TUNING=" -Xms256m  -Xmx256m -XX:-TieredCompilation"
#JAVA_TUNING=" -Xms128m  -Xmx128m -XX:+AggressiveOpts -XX:CompileThreshold=1000"

#JAVA_TUNING=" -Xms1g  -Xmx1g -XX:+UseConcMarkSweepGC"
#JAVA_TUNING=" -Xms2g  -Xmx2g"

# Normal settings:
JAVA_TUNING=" -Xms2g  -Xmx2g -XX:+UseConcMarkSweepGC"
#JAVA_TUNING=" -Xms512m  -Xmx512m -XX:+UseConcMarkSweepGC"

# Large heap for regression tests:
#JAVA_TUNING=" -Xms4g  -Xmx4g -XX:+UseConcMarkSweepGC"
#JAVA_TUNING=" -Xms2048m  -Xmx2048m -XX:-TieredCompilation"

# -ea -XX:+AggressiveOpts"
#JAVA_TUNING=" -Xms2048m  -Xmx2048m -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:InitiatingHeapOccupancyPercent=60"
# -verbose:gc"

#http://blog.mgm-tp.com/2014/04/controlling-gc-pauses-with-g1-collector/
#JAVA_TUNING=" -Xms2g -Xmx2g -XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=60 -XX:MaxGCPauseMillis=1000"

# "-XX:+UseParNewGC -XX:+CMSParallelRemarkEnabled"
#JAVA_TUNING=" -Xms2048m  -Xmx2048m -ea -XX:+AggressiveOpts -XX:+UseConcMarkSweepGC"


# From https://engineering.linkedin.com/garbage-collection/garbage-collection-optimization-high-throughput-and-low-latency-java-applications
# // JVM sizing options
# -server -Xms40g -Xmx40g -XX:MaxDirectMemorySize=4096m -XX:PermSize=256m -XX:MaxPermSize=256m   
# // Young generation options
# -XX:NewSize=6g -XX:MaxNewSize=6g -XX:+UseParNewGC -XX:MaxTenuringThreshold=2 -XX:SurvivorRatio=8 -XX:+UnlockDiagnosticVMOptions -XX:ParGCCardsPerStrideChunk=32768
# // Old generation  options
# -XX:+UseConcMarkSweepGC -XX:CMSParallelRemarkEnabled -XX:+ParallelRefProcEnabled -XX:+CMSClassUnloadingEnabled  -XX:CMSInitiatingOccupancyFraction=80 -XX:+UseCMSInitiatingOccupancyOnly   
# // Other options
# -XX:+AlwaysPreTouch -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintTenuringDistribution -XX:+PrintGCApplicationStoppedTime -XX:-OmitStackTraceInFastThrow

#JAVA_TUNING="-Xms2g -Xmx2g -XX:MaxTenuringThreshold=2 -XX:SurvivorRatio=8 -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+ParallelRefProcEnabled -XX:CMSInitiatingOccupancyFraction=80 -XX:+UseCMSInitiatingOccupancyOnly"


# JDK9 Shenandoah build:
# -XX:+UseShenandoahGC -client -XX:-UseFastLocking -XX:-UseCRC32Intrinsics -XX:ParallelGCThreads=2 
#JAVA_TUNING="-Xms2g -Xmx2g -XX:+UseShenandoahGC"


# MapBench jar file:
CLASSPATH=$MAP_BENCH_JAR
#CLASSPATH=$MAP_BENCH_JAR:$MARLIN_GRAPHICS_JAR

# MapBench Quality mode:
QUALITY=false
CLIP=false
SKIP_DRAW=false

PRE=true
ACCEL=false
VOLATILE=false
FILTER=false

# TRY AlphaPaint ie GradientPaint !!

#JAVA_OPTS="-DMapBench.clip.small=false -DMapBench.qualityMode=$QUALITY -DMapBench.filter.size=$FILTER -DMapBench.filter.minWidth=64 $JAVA_OPTS"
JAVA_OPTS="-DMapBench.skipDraw=$SKIP_DRAW -DMapBench.clip.small=$CLIP -DMapBench.qualityMode=$QUALITY -DMapBench.premultiplied=$PRE -DMapBench.acceleration=$ACCEL -DMapBench.volatile=$VOLATILE $JAVA_OPTS"

# Reset Boot classpath:
BOOTCLASSPATH=""

# log date:
echo "date:"
date
