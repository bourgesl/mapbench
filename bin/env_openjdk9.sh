#!/bin/sh

# This file defines the JAVA_HOME and PATH pointing to the proper JDK9:

# OpenJDK9 local build
export JAVA_HOME=/home/bourgesl/libs/graphics-rasterizer/client/build/linux-x86_64-normal-server-release/images/jdk/


echo "JAVA_HOME: $JAVA_HOME"

PATH=$JAVA_HOME/bin/:$PATH
export PATH
