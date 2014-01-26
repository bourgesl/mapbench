JAVA_OPTS="-server -XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal"
#JAVA_OPTS="-XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal"

#JAVA_TUNING=""
#JAVA_TUNING=" -Xms128m  -Xmx128m -XX:-TieredCompilation"
#JAVA_TUNING=" -Xms128m  -Xmx128m -XX:+TieredCompilation"
#JAVA_TUNING=" -Xms128m  -Xmx128m -XX:+AggressiveOpts -XX:CompileThreshold=1000"
JAVA_TUNING=" -Xms2048m  -Xmx2048m"
#JAVA_TUNING=" -Xms2048m  -Xmx2048m -ea -XX:+AggressiveOpts -XX:+UseConcMarkSweepGC"

# MapBench 0.1 from target folder:
CLASSPATH=../target/mapbench-0.1-SNAPSHOT.jar

# Marlin 0.3 in lib folder:
BOOTCLASSPATH="-Xbootclasspath/a:../lib/marlin-0.3.jar"

# Optional Marlin 0.3 java2d patch in lib folder:
BOOTCLASSPATH="-Xbootclasspath/p:../lib/marlin-0.3-sun-java2d.jar $BOOTCLASSPATH"

# Marlin tuning options:
USE_TL=true
#SIZE=4096
SIZE=2048

JAVA_OPTS="-Dsun.java2d.renderer.useThreadLocal=$USE_TL -Dsun.java2d.renderer.pixelsize=$SIZE -Dsun.java2d.renderer=org.marlin.pisces.PiscesRenderingEngine $JAVA_OPTS"

#DURATION="20000"

echo "CP:      $CLASSPATH"
echo "Boot CP: $BOOTCLASSPATH"

echo "JVM path"
which java

echo "Java version"
java -version

java $BOOTCLASSPATH $JAVA_OPTS $JAVA_TUNING -cp $CLASSPATH it.geosolutions.java2d.MapBench $DURATION
