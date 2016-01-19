#for i in 14 16 18 20; do
#for i in 10 12 14 16 18 20 22; do
#	export MARLIN_CUSTOM="-DMergeSort.threshold=$i"
for i in 20 30 40 50 60 70 80; do
	export MARLIN_CUSTOM="-DMergeSort.size_threshold=$i"
	echo "MARLIN_CUSTOM: $MARLIN_CUSTOM"
	echo "marlin 1"
	./bench_marlin.sh &> "marlin_th$i-1.log"
	echo "marlin 2"
	./bench_marlin.sh &> "marlin_th$i-2.log"
	echo "marlin 3"
	./bench_marlin.sh &> "marlin_th$i-3.log"
	done

