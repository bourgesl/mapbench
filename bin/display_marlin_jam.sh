#!/bin/bash
# get paths and jvm settings:
source ./env.sh

# get marlin settings and boot class path:
source ./env_marlin.sh

# Enable stats
STATS=false
JAVA_OPTS="-Dsun.java2d.renderer.doStats=$STATS $JAVA_OPTS"

#FILTER="-Dfilter.type=BELL -Dfilter.blur=1.0"

#MARLIN_G2D="-DMarlinGraphics.blendComposite=true -DMapBench.useMarlinGraphics2D=true"
# $FILTER $MARLIN_G2D 

JAVA_TUNING=" -Xms2g  -Xmx2g"

BOOTCLASSPATH="-Xbootclasspath/a:$MARLIN_JAR_PREFIX.jar"
#JAM=$JAMAICA/target/linux-x86_64/lib/rt.jar
#BOOTCLASSPATH="-Xbootclasspath/p:$MARLIN_JAR_PREFIX.jar:$MARLIN_JAR_PREFIX-sun-java2d.jar:$JAM"

echo "CLASSPATH:   $CLASSPATH"
echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $JAVA_OPTS"
echo "Java tuning: $JAVA_TUNING"

echo "JVM path"
which jamaicavm

echo "Java version"
jamaicavm -version

# Reference (pisces):
#echo "---"
#JVM_PROPS="-Dsun.java2d.renderer=sun.java2d.pisces.PiscesRenderingEngine -Dmapbench.profile=$PROFILE"
#jamaicavm $BOOTCLASSPATH $JAVA_TUNING $JVM_PROPS -cp $CLASSPATH it.geosolutions.java2d.MapDisplay
#jamaicavmp $BOOTCLASSPATH $JAVA_TUNING $JVM_PROPS -cp $CLASSPATH it.geosolutions.java2d.MapDisplay

#jamaicabuilder -verbose=2 -jobs=4 -parallel -useProfile MapDisplay.prof $JAVA_PROPS -cp $CLASSPATH it.geosolutions.java2d.MapDisplay
#echo "---"


# Marlin 0.7.3.2:
JVM_PROPS="-Dsun.java2d.renderer=org.marlin.pisces.MarlinRenderingEngine -Dmapbench.profile=$PROFILE"
JAVA_PROPS="-XdefineProperty=sun.java2d.renderer=org.marlin.pisces.MarlinRenderingEngine -XdefineProperty=mapbench.profile=$PROFILE"

#echo "Command: jamaicavm $BOOTCLASSPATH $JAVA_TUNING $JVM_PROPS -cp $CLASSPATH it.geosolutions.java2d.MapDisplay"
#jamaicavm $BOOTCLASSPATH $JAVA_TUNING $JVM_PROPS -cp $CLASSPATH it.geosolutions.java2d.MapDisplay

# get profile:
#jamaicavmp $BOOTCLASSPATH $JAVA_TUNING $JVM_PROPS -cp $CLASSPATH it.geosolutions.java2d.MapDisplay


JAM_CP="$JAMAICA/target/linux-x86_64/lib/rt.jar"
BOOTCLASSPATH="-Xbootclasspath=$MARLIN_JAR_PREFIX.jar:$MARLIN_JAR_PREFIX-sun-java2d.jar:$JAM_CP"

# Builder with profile:
#echo "Command: jamaicabuilder -verbose=2 -jobs=4 -parallel -useProfile MapDisplay.prof $BOOTCLASSPATH $JAVA_PROPS -cp $CLASSPATH it.geosolutions.java2d.MapDisplay"
#jamaicabuilder -verbose=2 -jobs=4 -parallel -useProfile MapDisplay.prof $BOOTCLASSPATH $JAVA_PROPS -cp $CLASSPATH it.geosolutions.java2d.MapDisplay

