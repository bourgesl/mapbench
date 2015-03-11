#!/bin/sh

# Marlin jar prefix:
MARLIN_JAR_PREFIX=../lib/marlin-0.4.5
MARLIN_JAR_PREFIX=../lib/marlin-0.5.6-Unsafe
#MARLIN_JAR_PREFIX=../lib/marlin-0.5.6-Unsafe-OpenJDK

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
BOOTCLASSPATH="-Xbootclasspath/a:$MARLIN_JAR_PREFIX.jar"
#BOOTCLASSPATH="-Xbootclasspath/p:$MARLIN_JAR_PREFIX.jar"

# Optional Marlin java2d patch in lib folder:
BOOTCLASSPATH="-Xbootclasspath/p:$MARLIN_JAR_PREFIX-sun-java2d.jar $BOOTCLASSPATH"

# Rendering engine:
RDR="org.marlin.pisces.PiscesRenderingEngine"
#RDR="sun.java2d.marlin.MarlinRenderingEngine"

# Update Java options:
JAVA_OPTS="-Dsun.java2d.renderer.useThreadLocal=$USE_TL -Dsun.java2d.renderer.useSimplifier=$USE_SIMPLIFIER -Dsun.java2d.renderer.useRef=$REF_TYPE -Dsun.java2d.renderer.pixelsize=$SIZE -Dsun.java2d.renderer.tileSize_log2=$TILE_LOG2 -Dsun.java2d.renderer=$RDR $JAVA_OPTS"
