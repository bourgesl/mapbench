#!/bin/bash
# get paths and jvm settings:
source ./env.sh

source ~/test-jdk10-cl.sh

# Marlin tuning options:

# ThreadLocal (TL) or ConcurrentLinkedQueue (CLQ) storage for renderer contexts:
USE_TL=true
# PixelSize (2048)
SIZE=2048
# larger pixelsize best for images ~ 8192x8192
#SIZE=8192
# Reference type among [hard,soft,weak]
REF_TYPE=soft
# Use Line simplifier [true,false]
USE_SIMPLIFIER=false
# Clip curves:
CLIP_CURVES=false
# 32x32 tiles (5)
TILE_H_LOG2=5
TILE_W_LOG2=5 # 7 is better
# enable logging
LOG=false

# Rendering engine:
#RDR="sun.java2d.marlin.MarlinRenderingEngine"
RDR="sun.java2d.marlin.DMarlinRenderingEngine"

# Use Cubic/Quad quality threshold values (<= 0.7.4):
JAVA_OPTS="-Dsun.java2d.renderer.cubic_dec_d2=2.5 -Dsun.java2d.renderer.cubic_inc_d1=1.0 -Dsun.java2d.renderer.quad_dec_d2=1.0 $JAVA_OPTS"

JAVA_OPTS="-Dsun.java2d.renderer.log=$LOG -Dsun.java2d.renderer.clip.curves=$CLIP_CURVES -Dsun.java2d.renderer.useThreadLocal=$USE_TL -Dsun.java2d.renderer.useSimplifier=$USE_SIMPLIFIER -Dsun.java2d.renderer.useRef=$REF_TYPE -Dsun.java2d.renderer.pixelsize=$SIZE -Dsun.java2d.renderer.tileSize_log2=$TILE_H_LOG2 -Dsun.java2d.renderer.tileWidth_log2=$TILE_W_LOG2 -Dsun.java2d.renderer=$RDR $JAVA_OPTS"

echo "CLASSPATH:   $CLASSPATH"
echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $JAVA_OPTS"
echo "Java tuning: $JAVA_TUNING"

echo "JVM path"
which java

echo "Java version"
java -version

taskset -c $CPU_CORE_IDS java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench

