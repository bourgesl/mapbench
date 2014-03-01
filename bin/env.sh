#!/bin/sh

# set profile
PROFILE=default.properties

# use shared image (less gc overhead but less realistic)
#PROFILE=sharedImage.properties

# scaling test (1T but image x4):
#PROFILE=scaleTest.properties

# use shared image and only 1 thread for insane maps:
#PROFILE=insane_1T.properties

# set paths
# MapBench jar:
MAP_BENCH_JAR=../lib/mapbench-0.3.jar

# Marlin jar prefix:
MARLIN_JAR_PREFIX=../lib/marlin-0.5A-Unsafe
