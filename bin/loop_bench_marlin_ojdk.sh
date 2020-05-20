#!/bin/bash
# get paths and jvm settings:
source ./env.sh

# get marlin settings and boot class path:
source ./env_marlin_ojdk.sh

# Enable stats
STATS=false
JAVA_OPTS="-Dsun.java2d.renderer.doStats=$STATS $JAVA_OPTS"

echo "CLASSPATH:   $CLASSPATH"
echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $JAVA_OPTS"
echo "Java tuning: $JAVA_TUNING"

echo "JVM path"
which java

echo "Java version"
java -version

# Rendering engine:
RDR="sun.java2d.marlin.MarlinRenderingEngine"

for s in 512 1024 2048 4096; do
    for e in 512 1024 2048 4096; do
        SIZE=$s
        EDGES=$e

        # Update Java options:
        JAVA_OPTS="-server"
        JAVA_OPTS="-Dsun.java2d.renderer.pixelsize=$SIZE -Dsun.java2d.renderer.edges=$EDGES -Dsun.java2d.renderer=$RDR $JAVA_OPTS"

        echo "Bench: SIZE=$SIZE EDGES=$EDGES"
        OUT="marlin_ojdk_size-$SIZE-edges-$EDGES.log"
	taskset -c $CPU_CORE_IDS java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench &> $OUT
        tail -n 4 $OUT
    done
done

echo "done."

