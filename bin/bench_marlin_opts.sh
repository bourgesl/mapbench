#!/bin/bash
# get paths and jvm settings:
source ./env.sh

# get marlin settings and boot class path:
source ./env_marlin.sh

echo "CLASSPATH:   $CLASSPATH"
echo "Java tuning: $JAVA_TUNING"

echo "JVM path"
which java

echo "Java version"
java -version

# Major Options:
# sun.java2d.renderer.useThreadLocal   = true
# sun.java2d.renderer.useRef           = soft
# sun.java2d.renderer.pixelsize        = 2048
# sun.java2d.renderer.subPixel_log2_X  = 3
# sun.java2d.renderer.subPixel_log2_Y  = 3
# sun.java2d.renderer.tileSize_log2    = 5
# sun.java2d.renderer.useFastMath      = true
# sun.java2d.renderer.useSimplifier    = false

# Reset Java options:
JAVA_OPTS="-server"

# Marlin tuning options:

# ThreadLocal (TL) or ConcurrentLinkedQueue (CLQ) storage for renderer contexts:
USE_TL=true

# Marlin renderer in lib folder:
BOOTCLASSPATH="-Xbootclasspath/a:$MARLIN_JAR_PREFIX.jar"

# Update Java options:
MARLIN_OPTS="-Dsun.java2d.renderer.useThreadLocal=$USE_TL -Dsun.java2d.renderer.useSimplifier=$USE_SIMPLIFIER -Dsun.java2d.renderer.pixelsize=$SIZE -Dsun.java2d.renderer.tileSize_log2=$TILE_LOG2 -Dsun.java2d.renderer=$RDR $JAVA_OPTS"

echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $MARLIN_OPTS"

java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_TL_1.log
java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_TL_2.log


# Optional Marlin java2d patch in lib folder:
BOOTCLASSPATH="-Xbootclasspath/p:$MARLIN_JAR_PREFIX-sun-java2d.jar -Xbootclasspath/a:$MARLIN_JAR_PREFIX.jar"

echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $MARLIN_OPTS"

java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_TL_Tile_1.log
java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_TL_Tile_2.log


TILE_LOG2=6

# Update Java options:
MARLIN_OPTS="-Dsun.java2d.renderer.useFastMath=false -Dsun.java2d.renderer.useThreadLocal=$USE_TL -Dsun.java2d.renderer.useSimplifier=$USE_SIMPLIFIER -Dsun.java2d.renderer.pixelsize=$SIZE -Dsun.java2d.renderer.tileSize_log2=$TILE_LOG2 -Dsun.java2d.renderer=$RDR $JAVA_OPTS"

echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $MARLIN_OPTS"

#java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_TL_Tile6_1.log
#java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_TL_Tile6_2.log


TILE_LOG2=5

# Update Java options:
MARLIN_OPTS="-Dsun.java2d.renderer.useFastMath=false -Dsun.java2d.renderer.useThreadLocal=$USE_TL -Dsun.java2d.renderer.useSimplifier=$USE_SIMPLIFIER -Dsun.java2d.renderer.pixelsize=$SIZE -Dsun.java2d.renderer.tileSize_log2=$TILE_LOG2 -Dsun.java2d.renderer=$RDR $JAVA_OPTS"

echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $MARLIN_OPTS"

java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_TL_Tile_noFM_1.log
java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_TL_Tile_noFM_2.log


# ThreadLocal (TL) or ConcurrentLinkedQueue (CLQ) storage for renderer contexts:
USE_TL=false

# Marlin renderer in lib folder:
BOOTCLASSPATH="-Xbootclasspath/a:$MARLIN_JAR_PREFIX.jar"

# Update Java options:
MARLIN_OPTS="-Dsun.java2d.renderer.useThreadLocal=$USE_TL -Dsun.java2d.renderer.useSimplifier=$USE_SIMPLIFIER -Dsun.java2d.renderer.pixelsize=$SIZE -Dsun.java2d.renderer.tileSize_log2=$TILE_LOG2 -Dsun.java2d.renderer=$RDR $JAVA_OPTS"

echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $MARLIN_OPTS"

echo "java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench"


java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_CLQ_1.log
java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_CLQ_2.log

# Optional Marlin java2d patch in lib folder:
BOOTCLASSPATH="-Xbootclasspath/p:$MARLIN_JAR_PREFIX-sun-java2d.jar -Xbootclasspath/a:$MARLIN_JAR_PREFIX.jar"

echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $MARLIN_OPTS"

java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_CLQ_Tile_1.log
java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_CLQ_Tile_2.log


