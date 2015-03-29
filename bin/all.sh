echo "ductus"
#./bench.sh &> ductus.log
echo "pisces 1"
#./bench_pisces.sh &> pisces_1.log
echo "pisces 2"
#./bench_pisces.sh &> pisces_2.log
echo "marlin 1"
./bench_marlin.sh &> marlin_1.log
echo "marlin 2"
./bench_marlin.sh &> marlin_2.log
echo "marlin 3"
./bench_marlin.sh &> marlin_3.log

