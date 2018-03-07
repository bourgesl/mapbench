REM get paths
CALL env.bat

echo %MAP_BENCH_JAR%

SET JAVA_OPTS=-server
REM SET JAVA_OPTS=-XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal

REM SET JAVA_TUNING= -Xms128m  -Xmx128m -XX:-TieredCompilation
REM SET JAVA_TUNING= -Xms128m  -Xmx128m -XX:+TieredCompilation
REM SET JAVA_TUNING= -Xms128m  -Xmx128m -XX:+AggressiveOpts -XX:CompileThreshold=1000

SET JAVA_TUNING= -Xms2048m  -Xmx2048m -XX:+UseConcMarkSweepGC
REM SET JAVA_TUNING= -Xms2048m  -Xmx2048m -ea -XX:+AggressiveOpts -XX:+UseConcMarkSweepGC

REM MapBench:
SET CLASSPATH=%MAP_BENCH_JAR%

SET BOOTCLASSPATH=

echo "CP:      %CLASSPATH%"
echo "Boot CP: %BOOTCLASSPATH%"

echo "JVM path"
REM which java

echo "Java version"
java -version

java -Dmapbench.profile=%PROFILE% %BOOTCLASSPATH% %JAVA_OPTS% %JAVA_TUNING% -cp %CLASSPATH% it.geosolutions.java2d.MapBench 

