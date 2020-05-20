#!/bin/bash
# get paths and jvm settings:
source ./env.sh

# get marlin settings and boot class path:
source ./env_marlin_ojdk9.sh

echo "CLASSPATH:   $CLASSPATH"
echo "Java tuning: $JAVA_TUNING"

echo "JVM path"
which java

echo "Java version"
java -version

# Major Options:
# sun.java2d.renderer.useThreadLocal   = true
# sun.java2d.renderer.useFastMath      = true

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

#taskset -c $CPU_CORE_IDS java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_TL_1.log
#taskset -c $CPU_CORE_IDS java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_TL_2.log
#taskset -c $CPU_CORE_IDS java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_TL_3.log


# Optional Marlin java2d patch in lib folder:
BOOTCLASSPATH="-Xbootclasspath/p:$MARLIN_JAR_PREFIX-sun-java2d.jar -Xbootclasspath/a:$MARLIN_JAR_PREFIX.jar"

echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $MARLIN_OPTS"

taskset -c $CPU_CORE_IDS java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_TL_Tile_1.log
taskset -c $CPU_CORE_IDS java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_TL_Tile_2.log
taskset -c $CPU_CORE_IDS java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_TL_Tile_3.log


USE_SIMPLIFIER=true

# Update Java options:
MARLIN_OPTS="-Dsun.java2d.renderer.useThreadLocal=$USE_TL -Dsun.java2d.renderer.useSimplifier=$USE_SIMPLIFIER -Dsun.java2d.renderer.pixelsize=$SIZE -Dsun.java2d.renderer.tileSize_log2=$TILE_LOG2 -Dsun.java2d.renderer=$RDR $JAVA_OPTS"

echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $MARLIN_OPTS"

#taskset -c $CPU_CORE_IDS java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_TL_Tile_Simp_1.log
#taskset -c $CPU_CORE_IDS java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_TL_Tile_Simp_2.log
#taskset -c $CPU_CORE_IDS java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_TL_Tile_Simp_3.log

USE_SIMPLIFIER=false


TILE_LOG2=6

# Update Java options:
MARLIN_OPTS="-Dsun.java2d.renderer.useThreadLocal=$USE_TL -Dsun.java2d.renderer.useSimplifier=$USE_SIMPLIFIER -Dsun.java2d.renderer.pixelsize=$SIZE -Dsun.java2d.renderer.tileSize_log2=$TILE_LOG2 -Dsun.java2d.renderer=$RDR $JAVA_OPTS"

echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $MARLIN_OPTS"

#taskset -c $CPU_CORE_IDS java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_TL_Tile6_1.log
#taskset -c $CPU_CORE_IDS java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_TL_Tile6_2.log
#taskset -c $CPU_CORE_IDS java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_TL_Tile6_3.log

TILE_LOG2=5


# Update Java options:
MARLIN_OPTS="-Dsun.java2d.renderer.useFastMath=false -Dsun.java2d.renderer.useThreadLocal=$USE_TL -Dsun.java2d.renderer.useSimplifier=$USE_SIMPLIFIER -Dsun.java2d.renderer.pixelsize=$SIZE -Dsun.java2d.renderer.tileSize_log2=$TILE_LOG2 -Dsun.java2d.renderer=$RDR $JAVA_OPTS"

echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $MARLIN_OPTS"

taskset -c $CPU_CORE_IDS java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_TL_Tile_noFM_1.log
taskset -c $CPU_CORE_IDS java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_TL_Tile_noFM_2.log
taskset -c $CPU_CORE_IDS java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_TL_Tile_noFM_3.log


# ThreadLocal (TL) or ConcurrentLinkedQueue (CLQ) storage for renderer contexts:
USE_TL=false

# Marlin renderer in lib folder:
BOOTCLASSPATH="-Xbootclasspath/a:$MARLIN_JAR_PREFIX.jar"

# Update Java options:
MARLIN_OPTS="-Dsun.java2d.renderer.useThreadLocal=$USE_TL -Dsun.java2d.renderer.useSimplifier=$USE_SIMPLIFIER -Dsun.java2d.renderer.pixelsize=$SIZE -Dsun.java2d.renderer.tileSize_log2=$TILE_LOG2 -Dsun.java2d.renderer=$RDR $JAVA_OPTS"

echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $MARLIN_OPTS"

#taskset -c $CPU_CORE_IDS java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_CLQ_1.log
#taskset -c $CPU_CORE_IDS java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_CLQ_2.log
#taskset -c $CPU_CORE_IDS java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_CLQ_3.log


# Optional Marlin java2d patch in lib folder:
BOOTCLASSPATH="-Xbootclasspath/p:$MARLIN_JAR_PREFIX-sun-java2d.jar -Xbootclasspath/a:$MARLIN_JAR_PREFIX.jar"

echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $MARLIN_OPTS"

#taskset -c $CPU_CORE_IDS java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_CLQ_Tile_1.log
#taskset -c $CPU_CORE_IDS java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_CLQ_Tile_2.log
#taskset -c $CPU_CORE_IDS java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $MARLIN_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> marlin_CLQ_Tile_3.log


