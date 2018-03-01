#!/bin/sh

BOOTCLASSPATH=""

# Rendering engine:
#RDR="sun.dc.DuctusRenderingEngine"
#RDR="sun.java2d.pisces.PiscesRenderingEngine"

#RDR="sun.java2d.marlin.MarlinRenderingEngine"
#RDR="sun.java2d.marlin.DMarlinRenderingEngine"

#JAVA_OPTS="-Dsun.java2d.renderer=$RDR $JAVA_OPTS"
#JAVA_OPTS="-Dsun.java2d.renderer.log=true $JAVA_OPTS"

# Use Cubic/Quad quality threshold values (<= 0.7.4):
#JAVA_OPTS="-Dsun.java2d.renderer.cubic_dec_d2=2.5 -Dsun.java2d.renderer.cubic_inc_d1=1.0 -Dsun.java2d.renderer.quad_dec_d2=1.0 $JAVA_OPTS"

# X/Y subpixels
#SP=6
#JAVA_OPTS="-Dsun.java2d.renderer.subPixel_log2_X=$SP -Dsun.java2d.renderer.subPixel_log2_Y=$SP $JAVA_OPTS"

JAVA_OPTS="-Dsun.java2d.renderer.log=true  $JAVA_OPTS"

