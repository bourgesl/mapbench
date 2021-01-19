#!/bin/sh

# This file defines the JAVA_HOME and PATH pointing to the proper JDK:
export JAVA_HOME=/home/bourgesl/libs/graphics-rasterizer/jdk/jdk-lbo/build/linux-x86_64-server-release/jdk/
#export JAVA_HOME=/home/bourgesl/libs/graphics-rasterizer/jdk/jdk-gh/build/linux-x86_64-server-release/jdk/

# Open JDK FORREST (10) local build
#export JAVA_HOME=/home/bourgesl/libs/graphics-rasterizer/jdk/client/build/linux-x86_64-server-release/jdk/

#export JAVA_HOME=/home/bourgesl/apps/jdk-12/
#export export JAVA_HOME=/home/bourgesl/apps/openjdk-11+28_linux-x64_bin/

echo "JAVA_HOME: $JAVA_HOME"

PATH=$JAVA_HOME/bin/:$PATH
export PATH
