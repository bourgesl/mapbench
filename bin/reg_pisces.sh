#!/bin/bash
# get paths and jvm settings:
source ./env.sh

# Pisces renderer in lib folder:
BOOTCLASSPATH="-Xbootclasspath/a:../lib/openjdk8-pisces.jar"

# Update Java options:
JAVA_OPTS="-Dsun.java2d.renderer=sun.java2d.pisces.PiscesRenderingEngine $JAVA_OPTS"

echo "CLASSPATH:   $CLASSPATH"
echo "Boot CP:     $BOOTCLASSPATH"
echo "Java opts:   $JAVA_OPTS"
echo "Java tuning: $JAVA_TUNING"

echo "JVM path"
which java

echo "Java version"
java -version


# longer (shared 4T) test:
PROFILE=longer_shared.properties
java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapDisplay


# scaling test (1T but image x4):
PROFILE=scaleTest.properties
java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapDisplay


# Use dashed stroke
PROFILE=longer_shared_dashed.properties
java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapDisplay


# complex affine transform test:
PROFILE=cpxTransform.properties
java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapDisplay


# even_odd winding rule test:
PROFILE=evenOddWindingRule.properties
java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapDisplay


# use createStrokedShape() + fill() test:
PROFILE=strokedShape.properties
java -Dmapbench.profile=$PROFILE $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapDisplay

