echo off
REM get paths
CALL env.bat


REM Marlin renderer in lib folder:
SET BOOTCLASSPATH=-Xbootclasspath/a:%MARLIN_JAR_PREFIX%.jar

REM Optional Marlin java2d patch in lib folder:
SET BOOTCLASSPATH=-Xbootclasspath/p:%MARLIN_JAR_PREFIX%-sun-java2d.jar %BOOTCLASSPATH%


SET JAVA_OPTS=-Dsun.java2d.renderer=org.marlin.pisces.DMarlinRenderingEngine %JAVA_OPTS%

echo "CLASSPATH:   %CLASSPATH%"
echo "Boot CP:     %BOOTCLASSPATH%"
echo "Java opts:   %JAVA_OPTS%"
echo "Java tuning: %JAVA_TUNING%"


echo "JVM path"
REM which java

echo "Java version"
java -version

java -Dmapbench.profile=%PROFILE% %BOOTCLASSPATH% %JAVA_OPTS% %JAVA_TUNING% -cp %CLASSPATH% it.geosolutions.java2d.MapDisplay
