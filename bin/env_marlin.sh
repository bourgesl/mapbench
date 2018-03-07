#!/bin/sh

# Marlin jar prefix:
#MARLIN_JAR_PREFIX=../lib/marlin-0.6.0-Unsafe
#MARLIN_JAR_PREFIX=../lib/marlin-0.9.0-ref-Unsafe
MARLIN_JAR_PREFIX=../lib/marlin-0.9.2-Unsafe
#MARLIN_JAR_PREFIX=../lib/marlin-0.8.2-Unsafe
#MARLIN_JAR_PREFIX=../lib/marlin-0.3

# Marlin tuning options:

# ThreadLocal (TL) or ConcurrentLinkedQueue (CLQ) storage for renderer contexts:
USE_TL=true
# PixelSize (4096 x 2176 = 4K)
#PIX_W=4096
#PIX_H=2176
PIX_W=4096
PIX_H=2176
# larger pixelsize best for images ~ 8192x8192
#SIZE=8192
# Reference type among [hard,soft,weak]
REF_TYPE=soft
# Use Line simplifier [true,false]
USE_SIMPLIFIER=false
# 32x32 tiles (5)
TILE_W_LOG2=7 # 7
TILE_H_LOG2=7 # 6
# block size:
BLOCK_LOG2=5
RLE_MIN=64
# enable logging
LOG=true
# enable path clipping
CLIP=true
# enable curve subdivider for clipping
SUBDIVIDE=true
SUBDIVIDER_MIN_LEN=100.0

SP_X=8
SP_Y=3

# Marlin renderer in lib folder:
BOOTCLASSPATH="-Xbootclasspath/a:$MARLIN_JAR_PREFIX.jar"

# Optional Marlin java2d patch in lib folder:
BOOTCLASSPATH="-Xbootclasspath/p:$MARLIN_JAR_PREFIX-sun-java2d.jar $BOOTCLASSPATH"

# Rendering engine:
#RDR="org.marlin.pisces.MarlinRenderingEngine"
RDR="org.marlin.pisces.DMarlinRenderingEngine"

# marlin renderer before 0.7.x:
#RDR="org.marlin.pisces.PiscesRenderingEngine"


# Use Cubic/Quad quality threshold values (<= 0.7.4):
#JAVA_OPTS="-Dsun.java2d.renderer.cubic_dec_d2=2.5 -Dsun.java2d.renderer.cubic_inc_d1=1.0 -Dsun.java2d.renderer.quad_dec_d2=1.0 $JAVA_OPTS"

# Intermediate quality:
#JAVA_OPTS="-Dsun.java2d.renderer.cubic_dec_d2=1.5 -Dsun.java2d.renderer.cubic_inc_d1=0.75 -Dsun.java2d.renderer.quad_dec_d2=0.75 $JAVA_OPTS"

#higher quality:
#JAVA_OPTS="-Dsun.java2d.renderer.cubic_dec_d2=0.5 -Dsun.java2d.renderer.cubic_inc_d1=0.1 -Dsun.java2d.renderer.quad_dec_d2=0.2 $JAVA_OPTS"

# 0.7.5 defaults: 1.0/0.4/0.5

# New settings after 0.9.0:
#JAVA_OPTS="-Dsun.java2d.renderer.cubic_dec_d2=2.5 -Dsun.java2d.renderer.cubic_inc_d1=0.5 -Dsun.java2d.renderer.quad_dec_d2=1.0 $JAVA_OPTS"
#JAVA_OPTS="-Dsun.java2d.renderer.cubic_dec_d2=1.0 -Dsun.java2d.renderer.cubic_inc_d1=0.2 -Dsun.java2d.renderer.quad_dec_d2=0.5 $JAVA_OPTS"

# highest quality (1px error):
#JAVA_OPTS="-Dsun.java2d.renderer.quad_dec_d2=0.01 -Dsun.java2d.renderer.cubic_dec_d2=0.01 -Dsun.java2d.renderer.cubic_inc_d1=0.002 $JAVA_OPTS"


# clip subdivider:
JAVA_OPTS="-Dsun.java2d.renderer.clip.subdivider=$SUBDIVIDE -Dsun.java2d.renderer.clip.subdivider.minLength=$SUBDIVIDER_MIN_LEN $JAVA_OPTS"

# path simplifier:
JAVA_OPTS="-Dsun.java2d.renderer.usePathSimplifier=false -Dsun.java2d.renderer.pathSimplifier.pixTol=0.125 $JAVA_OPTS"

# Pixel loops:
RLE="-Dsun.java2d.renderer.enableRLE=true -Dsun.java2d.renderer.forceRLE=false -Dsun.java2d.renderer.forceNoRLE=false -Dsun.java2d.renderer.useTileFlags=true -Dsun.java2d.renderer.useTileFlags.useHeuristics=true -Dsun.java2d.renderer.blockSize_log2=$BLOCK_LOG2 -Dsun.java2d.renderer.rleMinWidth=$RLE_MIN"

# Update Java options:
# trace counts: -Dsun.java2d.trace=count

JAVA_PIPE="-Dsun.java2d.opengl=true -Dsun.java2d.opengl.bufferSize=4194304 -Dsun.java2d.opengl.flushDelay=20" 
# 1M: 1048576 
# 4M: 4194304

JAVA_OPTS="$JAVA_PIPE $RLE -Dsun.java2d.renderer.clip=$CLIP -Dsun.java2d.renderer.log=$LOG -Dsun.java2d.renderer.logCreateContext=false -Dsun.java2d.renderer.logUnsafeMalloc=false -Dsun.java2d.renderer.clip.curves=$CLIP_CURVES -Dsun.java2d.renderer.subPixel_log2_X=$SP_X -Dsun.java2d.renderer.subPixel_log2_Y=$SP_Y -Dsun.java2d.renderer.useThreadLocal=$USE_TL -Dsun.java2d.renderer.useSimplifier=$USE_SIMPLIFIER -Dsun.java2d.renderer.useRef=$REF_TYPE -Dsun.java2d.renderer.pixelWidth=$PIX_W -Dsun.java2d.renderer.pixelHeight=$PIX_H -Dsun.java2d.renderer.tileSize_log2=$TILE_H_LOG2 -Dsun.java2d.renderer.tileWidth_log2=$TILE_W_LOG2 -Dsun.java2d.renderer=$RDR $JAVA_OPTS"

