#!/bin/sh

source env_openjdk9.sh

# should use Marlin within open jdk 9:
# Marlin jar prefix:
MARLIN_JAR_PREFIX=

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
# 32x32 tiles (5)
TILE_LOG2=5
# enable logging
LOG=true

# Marlin renderer in lib folder:
BOOTCLASSPATH=

# Rendering engine:
#RDR="sun.java2d.marlin.MarlinRenderingEngine"
#RDR="sun.java2d.marlin.DMarlinRenderingEngine"

# Pixel loops:
#RLE="-Dsun.java2d.renderer.enableRLE=true -Dsun.java2d.renderer.forceRLE=false -Dsun.java2d.renderer.forceNoRLE=false -Dsun.java2d.renderer.useTileFlags=true -Dsun.java2d.renderer.useTileFlags.useHeuristics=true -Dsun.java2d.renderer.rleMinWidth=40 -Dsun.java2d.renderer.blockSize_log2=5"

# Update Java options:
#JAVA_OPTS="$RLE -Dsun.java2d.renderer.subPixel_log2_X=3 -Dsun.java2d.renderer.subPixel_log2_Y=3 -Dsun.java2d.renderer.useThreadLocal=$USE_TL -Dsun.java2d.renderer.useSimplifier=$USE_SIMPLIFIER -Dsun.java2d.renderer.useRef=$REF_TYPE -Dsun.java2d.renderer.pixelsize=$SIZE -Dsun.java2d.renderer.tileSize_log2=$TILE_LOG2 -Dsun.java2d.renderer=$RDR $JAVA_OPTS"

#JAVA_OPTS="-Dsun.java2d.renderer=$RDR"

JAVA_OPTS="-Dsun.java2d.renderer.verbose=false -Dsun.java2d.renderer.log=$LOG -Dsun.java2d.renderer.logCreateContext=false -Dsun.java2d.renderer.logUnsafeMalloc=false -Dsun.java2d.renderer.subPixel_log2_X=3 -Dsun.java2d.renderer.subPixel_log2_Y=3 -Dsun.java2d.renderer.useThreadLocal=$USE_TL -Dsun.java2d.renderer.useSimplifier=$USE_SIMPLIFIER -Dsun.java2d.renderer.useRef=$REF_TYPE -Dsun.java2d.renderer.pixelsize=$SIZE -Dsun.java2d.renderer.tileSize_log2=$TILE_LOG2 $JAVA_OPTS"
