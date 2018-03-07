
REM set profile
SET PROFILE=default.properties

REM use shared image (less gc overhead but less realistic)
REM SET PROFILE=sharedImage.properties

REM use shared image but single thread only
SET PROFILE=shared_1T.properties


REM scaling test (1T but image x4):
REM SET PROFILE=scaleTest.properties

REM use shared image and only 1 thread for insane maps:
REM SET PROFILE=insane_1T.properties

REM set paths


REM MapBench jar:
SET MAP_BENCH_JAR=..\lib\mapbench-0.5.0.jar

REM Marlin jar prefix
REM SET MARLIN_JAR_PREFIX=..\lib\marlin-0.8.2-Unsafe
SET MARLIN_JAR_PREFIX=..\lib\marlin-0.9.2-Unsafe


REM server jvm
SET JAVA_OPTS=-server

REM Normal settings:
SET JAVA_TUNING=-Xms2g  -Xmx2g -XX:+UseConcMarkSweepGC

SET CLASSPATH=%MAP_BENCH_JAR%


REM MapBench Quality mode:
SET QUALITY=true
SET CLIP=false
SET SKIP_DRAW=false
SET SKIP_FILL=false

SET PRE=true
SET ACCEL=true
SET VOLATILE=true
SET FILTER=false


SET JAVA_OPTS=-DMapBench.skipDraw=%SKIP_DRAW% -DMapBench.skipFill=%SKIP_FILL% -DMapBench.clip.small=%CLIP% -DMapBench.qualityMode=%QUALITY% -DMapBench.premultiplied=%PRE% -DMapBench.acceleration=%ACCEL% -DMapBench.volatile=%VOLATILE% %JAVA_OPTS%

REM windows: adjust tile to 32x32 (see D3DMaskCache !)
SET JAVA_OPTS=-Dsun.java2d.renderer.tileSize_log2=5 -Dsun.java2d.renderer.tileWidth_log2=5 %JAVA_OPTS%

REM SET JAVA_OPTS=-Dsun.java2d.opengl=true -Dsun.java2d.opengl.bufferSize=4194304 -Dsun.java2d.opengl.flushDelay=5 %JAVA_OPTS%
REM  -Dsun.java2d.opengl.bufferSize=32768
REM  -Dsun.java2d.opengl.bufferSize=4194304 -Dsun.java2d.opengl.flushDelay=20

REM D3D
SET JAVA_OPTS=-Dsun.java2d.opengl.bufferSize=1000000 %JAVA_OPTS%


REM date / time
date /T
time /T
ver
