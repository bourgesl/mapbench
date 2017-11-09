#!/bin/sh

# This file defines the JAVA_HOME and PATH pointing to the proper JDK:

# Open JDK FORREST (10) local build
export JAVA_HOME=/home/bourgesl/libs/graphics-rasterizer/jdk/client/build/linux-x86_64-normal-server-release/jdk/


echo "JAVA_HOME: $JAVA_HOME"

PATH=$JAVA_HOME/bin/:$PATH
export PATH
