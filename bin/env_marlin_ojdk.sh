#!/bin/sh

# Marlin jar prefix:
#MARLIN_JAR_PREFIX=../lib/marlin-0.7.3.3-Unsafe-OpenJDK
MARLIN_JAR_PREFIX=../lib/marlin-0.8.1-Unsafe-OpenJDK

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
TILE_H_LOG2=5 # 6 ?
TILE_W_LOG2=5 # 7 is better
# enable logging
LOG=true
# enable path clipping
CLIP=true

# Marlin renderer in lib folder:
BOOTCLASSPATH="-Xbootclasspath/a:$MARLIN_JAR_PREFIX.jar"

# Optional Marlin java2d patch in lib folder:
BOOTCLASSPATH="-Xbootclasspath/p:$MARLIN_JAR_PREFIX-sun-java2d.jar $BOOTCLASSPATH"

# Rendering engine:
RDR="sun.java2d.marlin.MarlinRenderingEngine"
RDR="sun.java2d.marlin.DMarlinRenderingEngine"

# Use Cubic/Quad quality threshold values (<= 0.7.4):
JAVA_OPTS="-Dsun.java2d.renderer.cubic_dec_d2=2.5 -Dsun.java2d.renderer.cubic_inc_d1=1.0 -Dsun.java2d.renderer.quad_dec_d2=1.0 $JAVA_OPTS"

# Intermediate quality:
#JAVA_OPTS="-Dsun.java2d.renderer.cubic_dec_d2=1.5 -Dsun.java2d.renderer.cubic_inc_d1=0.75 -Dsun.java2d.renderer.quad_dec_d2=0.75 $JAVA_OPTS"

# 0.7.5 defaults: 1.0/0.4/0.5

# Pixel loops:
RLE="-Dsun.java2d.renderer.enableRLE=true -Dsun.java2d.renderer.forceRLE=false -Dsun.java2d.renderer.forceNoRLE=false -Dsun.java2d.renderer.useTileFlags=true -Dsun.java2d.renderer.useTileFlags.useHeuristics=true -Dsun.java2d.renderer.blockSize_log2=5 -Dsun.java2d.renderer.rleMinWidth=64"

# Update Java options:
# trace counts: -Dsun.java2d.trace=count

JAVA_OPTS="$RLE -Dsun.java2d.renderer.clip=$CLIP -Dsun.java2d.renderer.log=$LOG -Dsun.java2d.renderer.logCreateContext=false -Dsun.java2d.renderer.logUnsafeMalloc=false -Dsun.java2d.renderer.clip.curves=$CLIP_CURVES -Dsun.java2d.renderer.subPixel_log2_X=3 -Dsun.java2d.renderer.subPixel_log2_Y=3 -Dsun.java2d.renderer.useThreadLocal=$USE_TL -Dsun.java2d.renderer.useSimplifier=$USE_SIMPLIFIER -Dsun.java2d.renderer.useRef=$REF_TYPE -Dsun.java2d.renderer.pixelsize=$SIZE -Dsun.java2d.renderer.tileSize_log2=$TILE_H_LOG2 -Dsun.java2d.renderer.tileWidth_log2=$TILE_W_LOG2 -Dsun.java2d.renderer=$RDR $JAVA_OPTS"

