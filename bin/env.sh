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
PROFILE=shared_1T.properties

# with gamma
#PROFILE=shared_1T_gamma.properties

#PROFILE=shared_1T_NZ.properties
#PROFILE=shared_1T_dashed.properties
#PROFILE=shared_1T_4K.properties
#PROFILE=shared_1T_4K_dashed.properties

# zoom tests
#PROFILE=shared_1T_5x_dashed.properties
#PROFILE=shared_1T_zoom_out.properties

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

# Check profile / maps (very quick test)
#PROFILE=check_shared_1T.properties


# set paths
# MapBench jar:
MAP_BENCH_JAR=../lib/mapbench-0.5.0.jar:../lib/org.jfree.svg-4.1.jar
# Marlin Graphics jar:
MARLIN_GRAPHICS_JAR=../lib/marlin-graphics-0.3.0.jar 


# server jvm
JAVA_OPTS="-server"
#JAVA_OPTS="-server -Xbatch -XX:ObjectAlignmentInBytes=32"

#JAVA_OPTS=""
#JAVA_OPTS="-XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal"
#JAVA_OPTS="-server -XX:FreqInlineSize=800" # -XX:+PrintCompilation"

#JAVA_OPTS="-server -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:StartFlightRecording=name=mapbench,filename=/home/marlin/mapbench/bin/mapbench.jfr,dumponexit=true -XX:FlightRecorderOptions=defaultrecording=true"

# MaxInlineLevel: 
#JAVA_OPTS="-server -XX:MaxInlineLevel=15"

# GRAAL:
#JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -XX:+UseJVMCICompiler"
# -Dgraal.AlwaysInlineIntrinsics=true"
# -XX:+PrintCompilation -XX:+JVMCIPrintProperties
# -XX:+BootstrapJVMCI -XX:-TieredCompilation
# -XX:+UnlockDiagnosticVMOptions -XX:+LogCompilation"


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

#JAVA_TUNING=" -Xms1g  -Xmx1g -XX:+UseConcMarkSweepGC"
#JAVA_TUNING=" -Xms2g  -Xmx2g"


# Normal settings:
# previous default:
#JAVA_TUNING=" -Xms2g  -Xmx2g -XX:+UseConcMarkSweepGC"
# better 2020:
JAVA_TUNING=" -Xms2g  -Xmx2g -XX:+UseConcMarkSweepGC -XX:+AlwaysPreTouch -XX:+UseTransparentHugePages"
#JAVA_TUNING=" -Xms512m  -Xmx512m -XX:+UseConcMarkSweepGC -XX:+AlwaysPreTouch -XX:+UseTransparentHugePages"
# -XX:+UseLargePages

# TEST GC:
#JAVA_TUNING=" -Xms2g  -Xmx2g -XX:+UseParallelGC"

# JDK14+ removed CMS GC so switch to ParallelGC ?
#JAVA_TUNING=" -Xms2g  -Xmx2g -XX:+UseParallelGC -XX:+AlwaysPreTouch -XX:+UseTransparentHugePages"
#JAVA_TUNING=" -Xms2g  -Xmx2g -XX:+UseParallelGC -XX:+AlwaysPreTouch -XX:+UseTransparentHugePages -XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -XX:+UseJVMCICompiler -Dgraal.AlwaysInlineIntrinsics=true -XX:+BootstrapJVMCI"

#JAVA_TUNING="$JAVA_TUNING -XX:+UnlockExperimentalVMOptions -XX:+TrustFinalNonStaticFields"


# Large heap for regression tests:
#JAVA_TUNING=" -Xms4g  -Xmx16g -XX:+UseConcMarkSweepGC"
#JAVA_TUNING=" -Xms2048m  -Xmx2048m -XX:-TieredCompilation"


#http://blog.mgm-tp.com/2014/04/controlling-gc-pauses-with-g1-collector/
JAVA_TUNING=" -Xms2g -Xmx2g -XX:+UseG1GC -XX:+AlwaysPreTouch -XX:+UseTransparentHugePages"
# -XX:InitiatingHeapOccupancyPercent=60 -XX:MaxGCPauseMillis=100

# Shenandoah:
# -XX:+UseShenandoahGC -client -XX:-UseFastLocking -XX:-UseCRC32Intrinsics -XX:ParallelGCThreads=2 
#JAVA_TUNING="-Xms2g -Xmx2g -XX:+UnlockExperimentalVMOptions -XX:+UseShenandoahGC -XX:+AlwaysPreTouch -XX:+UseTransparentHugePages"
#  -XX:+ShenandoahOptimizeStableFinals -XX:-ExplicitGCInvokesConcurrent"
# -Xlog:gc+stats
# -XX:ShenandoahGCHeuristics=passive

# EpsilonGC (no GC):
#JAVA_TUNING="-Xms2g -Xmx2g -XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC -XX:+AlwaysPreTouch -XX:+UseTransparentHugePages"


# Try tuning max inline:
# see http://cr.openjdk.java.net/~redestad/8234863/open.00/
# Defaults in OpenJDK8:
# JAVA_OPTS="-XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal"
#     intx MaxInlineLevel                            = 9                                   {product}
#     intx MaxNodeLimit                              = 75000                               {C2 product}
#JAVA_TUNING="$JAVA_TUNING -XX:MaxInlineLevel=15 -XX:MaxNodeLimit=250000 -XX:NodeLimitFudgeFactor=99000"
#JAVA_TUNING="$JAVA_TUNING -XX:MaxInlineLevel=15"
#JAVA_TUNING="$JAVA_TUNING -XX:MaxInlineLevel=9"
#JAVA_TUNING="$JAVA_TUNING -XX:FreqInlineSize=325" # 325 is default


# MapBench jar file:
CLASSPATH=$MAP_BENCH_JAR

# MapBench Quality mode:
QUALITY=true
CLIP=false
SKIP_DRAW=false
SKIP_FILL=false

PRE=false
ACCEL=false
VOLATILE=false
USE_4BYTES=false
FILTER=false
USE_GAMMA=false

# TRY AlphaPaint ie GradientPaint !!

#JAVA_OPTS="-DMapBench.clip.small=false -DMapBench.qualityMode=$QUALITY -DMapBench.filter.size=$FILTER -DMapBench.filter.minWidth=64 $JAVA_OPTS"
JAVA_OPTS="-DMapBench.skipDraw=$SKIP_DRAW -DMapBench.skipFill=$SKIP_FILL -DMapBench.clip.small=$CLIP -DMapBench.qualityMode=$QUALITY -DMapBench.premultiplied=$PRE -DMapBench.acceleration=$ACCEL -DMapBench.volatile=$VOLATILE $JAVA_OPTS -DMapBench.4bytes=$USE_4BYTES"

if [ "$USE_GAMMA" == "true" ]
then
    JAVA_OPTS="$JAVA_OPTS -DMapBench.useMarlinGraphics2D=true -DMarlinGraphics.blendComposite=true"
    CLASSPATH=$MAP_BENCH_JAR:$MARLIN_GRAPHICS_JAR
fi


# Reset Boot classpath:
BOOTCLASSPATH=""

# define CPU core to use
# Note: use linux kernel GRUB_CMDLINE_LINUX="isolcpus=3" in /etc/default/grub
export CPU_CORE_IDS=3

echo "CPU_CORE_IDS: $CPU_CORE_IDS"

# log date:
echo "date:"
date
