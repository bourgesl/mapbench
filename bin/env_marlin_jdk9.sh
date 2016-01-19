#!/bin/sh

source env_jdk9.sh

# should use Marlin within open jdk 9:
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

# Marlin renderer in lib folder:
#BOOTCLASSPATH="-Xbootclasspath/p:$MARLIN_JAR_PREFIX.jar"
#BOOTCLASSPATH="-Xbootclasspath/p:../lib/marlin_jdk9.jar"

# Optional Marlin java2d patch in lib folder:
#BOOTCLASSPATH="-Xbootclasspath/p:$MARLIN_JAR_PREFIX-sun-java2d.jar $BOOTCLASSPATH"

# Rendering engine:
RDR="sun.java2d.marlin.MarlinRenderingEngine"


# Pixel loops:
#RLE="-Dsun.java2d.renderer.enableRLE=true -Dsun.java2d.renderer.forceRLE=false -Dsun.java2d.renderer.forceNoRLE=false -Dsun.java2d.renderer.useTileFlags=true -Dsun.java2d.renderer.useTileFlags.onlyRLE=false -Dsun.java2d.renderer.useTileFlags.useHeuristics=true"

# Update Java options:
JAVA_OPTS="$RLE -Dsun.java2d.renderer.subPixel_log2_X=3 -Dsun.java2d.renderer.subPixel_log2_Y=3 -Dsun.java2d.renderer.useThreadLocal=$USE_TL -Dsun.java2d.renderer.useSimplifier=$USE_SIMPLIFIER -Dsun.java2d.renderer.useRef=$REF_TYPE -Dsun.java2d.renderer.pixelsize=$SIZE -Dsun.java2d.renderer.tileSize_log2=$TILE_LOG2 -Dsun.java2d.renderer=$RDR $JAVA_OPTS"
