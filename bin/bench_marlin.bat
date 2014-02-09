REM get paths
CALL env.bat

REM server or client JVM:
SET JAVA_OPTS=-server -XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal
REM SET JAVA_OPTS=-client -d32 -XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal

REM SET JAVA_OPTS=-XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal

REM SET JAVA_TUNING= -Xms128m  -Xmx128m -XX:-TieredCompilation
REM SET JAVA_TUNING= -Xms128m  -Xmx128m -XX:+TieredCompilation
REM SET JAVA_TUNING= -Xms128m  -Xmx128m -XX:+AggressiveOpts -XX:CompileThreshold=1000

SET JAVA_TUNING= -Xms2048m  -Xmx2048m -XX:+UseConcMarkSweepGC
REM SET JAVA_TUNING= -Xms2048m  -Xmx2048m -ea -XX:+AggressiveOpts -XX:+UseConcMarkSweepGC

REM MapBench:
SET CLASSPATH=%MAP_BENCH_JAR%

REM Marlin 0.3 in lib folder:
SET BOOTCLASSPATH=-Xbootclasspath/a:%MARLIN_JAR_PREFIX%.jar

REM Optional Marlin 0.3 java2d patch in lib folder:
SET BOOTCLASSPATH=-Xbootclasspath/p:%MARLIN_JAR_PREFIX%-sun-java2d.jar %BOOTCLASSPATH%

REM Marlin tuning options:
SET USE_TL=true
REM SET SIZE=4096
SET SIZE=2048

SET JAVA_OPTS=-Dsun.java2d.renderer.useThreadLocal=%USE_TL% -Dsun.java2d.renderer.pixelsize=%SIZE% -Dsun.java2d.renderer=org.marlin.pisces.PiscesRenderingEngine %JAVA_OPTS%

REM SET DURATION=20000

echo "CP:      %CLASSPATH%"
echo "Boot CP: %BOOTCLASSPATH%"

echo "JVM path"
REM which java

echo "Java version"
java -version

java -Dmapbench.profile=%PROFILE% %BOOTCLASSPATH% %JAVA_OPTS% %JAVA_TUNING% -cp %CLASSPATH% it.geosolutions.java2d.MapBench
