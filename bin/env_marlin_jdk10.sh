#!/bin/sh

source env_jdk10.sh

# should use Marlin within open jdk 10:
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
TILE_W_LOG2=7 # 7
TILE_H_LOG2=6 # 6
# block size:
BLOCK_LOG2=5
RLE_MIN=64
# enable logging
LOG=true
# enable path clipping
CLIP=true

SP_X=3
SP_Y=3

# Rendering engine:
RDR="sun.java2d.marlin.MarlinRenderingEngine"
RDR="sun.java2d.marlin.DMarlinRenderingEngine"

# marlin renderer before 0.7.x:
#RDR="org.marlin.pisces.PiscesRenderingEngine"

# Use Cubic/Quad quality threshold values (<= 0.7.4):
JAVA_OPTS="-Dsun.java2d.renderer.cubic_dec_d2=2.5 -Dsun.java2d.renderer.cubic_inc_d1=1.0 -Dsun.java2d.renderer.quad_dec_d2=1.0 $JAVA_OPTS"

# Intermediate quality:
#JAVA_OPTS="-Dsun.java2d.renderer.cubic_dec_d2=1.5 -Dsun.java2d.renderer.cubic_inc_d1=0.75 -Dsun.java2d.renderer.quad_dec_d2=0.75 $JAVA_OPTS"

# 0.7.5 defaults: 1.0/0.4/0.5

# Pixel loops:
RLE="-Dsun.java2d.renderer.enableRLE=true -Dsun.java2d.renderer.forceRLE=false -Dsun.java2d.renderer.forceNoRLE=false -Dsun.java2d.renderer.useTileFlags=true -Dsun.java2d.renderer.useTileFlags.useHeuristics=true -Dsun.java2d.renderer.blockSize_log2=$BLOCK_LOG2 -Dsun.java2d.renderer.rleMinWidth=$RLE_MIN"

# Update Java options:
# trace counts: -Dsun.java2d.trace=count

JAVA_OPTS="$RLE -Dsun.java2d.renderer.clip=$CLIP -Dsun.java2d.renderer.log=$LOG -Dsun.java2d.renderer.logCreateContext=false -Dsun.java2d.renderer.logUnsafeMalloc=false -Dsun.java2d.renderer.clip.curves=$CLIP_CURVES -Dsun.java2d.renderer.subPixel_log2_X=$SP_X -Dsun.java2d.renderer.subPixel_log2_Y=$SP_Y -Dsun.java2d.renderer.useThreadLocal=$USE_TL -Dsun.java2d.renderer.useSimplifier=$USE_SIMPLIFIER -Dsun.java2d.renderer.useRef=$REF_TYPE -Dsun.java2d.renderer.pixelSize=$PIX_W -Dsun.java2d.renderer.tileSize_log2=$TILE_H_LOG2 -Dsun.java2d.renderer.tileWidth_log2=$TILE_W_LOG2 -Dsun.java2d.renderer=$RDR $JAVA_OPTS"

