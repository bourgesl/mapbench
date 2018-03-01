#~/libs/jitwatch/jarScan.sh /home/bourgesl/libs/marlin/mapbench/lib/marlin-0.7.3.3-Unsafe.jar | grep "\"org.marlin."

#~/libs/jitwatch/jarScan.sh --mode=maxMethodSize --limit=325 /home/bourgesl/libs/marlin/mapbench/lib/marlin-0.7.5-Unsafe.jar | grep "\"*marlin."
~/libs/jitwatch/jarScan.sh --mode=maxMethodSize --limit=325 /home/bourgesl/libs/marlin/mapbench/lib/marlin-0.9.1-Unsafe.jar | grep "\"*marlin."
