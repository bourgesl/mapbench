#!/bin/bash
# get paths
source ./env.sh

# server jvm 
JAVA_OPTS="-server -XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal"
# headless mode
#JAVA_OPTS="-server -Djava.awt.headless=true -XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal"
#JAVA_OPTS="-XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal"

#JAVA_TUNING=" -Xms256m  -Xmx256m -XX:-TieredCompilation"
#JAVA_TUNING=" -Xms128m  -Xmx128m -XX:+TieredCompilation"
#JAVA_TUNING=" -Xms128m  -Xmx128m -XX:+AggressiveOpts -XX:CompileThreshold=1000"
JAVA_TUNING=" -Xms2048m  -Xmx2048m"
#JAVA_TUNING=" -Xms2048m  -Xmx2048m -ea -XX:+AggressiveOpts -XX:+UseConcMarkSweepGC"

# MapBench:
CLASSPATH=$MAP_BENCH_JAR

# Marlin 0.3 in lib folder:
BOOTCLASSPATH="-Xbootclasspath/a:$MARLIN_JAR_PREFIX.jar"

# Optional Marlin 0.3 java2d patch in lib folder:
BOOTCLASSPATH="-Xbootclasspath/p:$MARLIN_JAR_PREFIX-sun-java2d.jar $BOOTCLASSPATH"

# Marlin tuning options:
USE_TL=true
#SIZE=5120
SIZE=2048

JAVA_OPTS="-Dsun.java2d.renderer.useThreadLocal=$USE_TL -Dsun.java2d.renderer.pixelsize=$SIZE -Dsun.java2d.renderer=org.marlin.pisces.PiscesRenderingEngine $JAVA_OPTS"

echo "CP:      $CLASSPATH"
echo "Boot CP: $BOOTCLASSPATH"

echo "JVM path"
which java

echo "Java version"
java -version

java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench

