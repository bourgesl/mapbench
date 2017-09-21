#!/bin/bash
# get paths and jvm settings:
source ./env.sh

JAVA_TUNING=" -Xms2g  -Xmx2g"

echo "CLASSPATH:   $CLASSPATH"
echo "Boot CP:     $BOOTCLASSPATH"
echo "Java tuning: $JAVA_TUNING"

echo "JVM path"
which jamaicavm

echo "Java version"
jamaicavm -version

# Reference (pisces):

echo "jamaicavmp $BOOTCLASSPATH $JAVA_TUNING $JVM_PROPS -cp $CLASSPATH it.geosolutions.java2d.MapBench"

export JAMAICAVM_NUMTHREADS="32"

JVM_PROPS="-Dsun.java2d.renderer=sun.java2d.pisces.PiscesRenderingEngine -Dmapbench.profile=$PROFILE"
JAVA_PROPS="-XdefineProperty=sun.java2d.renderer=sun.java2d.pisces.PiscesRenderingEngine -XdefineProperty=mapbench.profile=$PROFILE"

#jamaicavm $BOOTCLASSPATH $JAVA_TUNING $JVM_PROPS -cp $CLASSPATH it.geosolutions.java2d.MapBench
#jamaicavmp $BOOTCLASSPATH $JAVA_TUNING $JVM_PROPS -cp $CLASSPATH it.geosolutions.java2d.MapBench

# core dump: FPE exception


# Builder with profile:
#echo "Command: jamaicabuilder -verbose=2 -jobs=4 -parallel -useProfile MapDisplay.prof $BOOTCLASSPATH $JAVA_PROPS -cp $CLASSPATH it.geosolutions.java2d.MapBench"
#jamaicabuilder -verbose=2 -jobs=4 -parallel -useProfile MapDisplay.prof $BOOTCLASSPATH $JAVA_PROPS -cp $CLASSPATH it.geosolutions.java2d.MapBench

#jamaicabuilder -verbose=2 -jobs=4 -parallel -compile $JAVA_PROPS -cp $CLASSPATH it.geosolutions.java2d.MapBench

jamaicabuilder -verbose=2 -jobs=4 -parallel -useProfile MapBench_pisces.prof $BOOTCLASSPATH $JAVA_PROPS -cp $CLASSPATH it.geosolutions.java2d.MapBench

