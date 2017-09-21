#!/bin/sh

# This file defines the JAVA_HOME and PATH pointing to the proper JDK10:

# OpenJDK10 local build
export JAVA_HOME=/home/graphics-rasterizer/jdk10/client/build/linux-x86_64-normal-server-release/images/jdk/


echo "JAVA_HOME: $JAVA_HOME"

PATH=$JAVA_HOME/bin/:$PATH
export PATH
