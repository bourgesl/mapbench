echo "ductus"
#./bench.sh &> ductus.log
echo "pisces 1"
#./bench_pisces.sh &> pisces_1.log
echo "pisces 2"
#./bench_pisces.sh &> pisces_2.log

for i in $(seq 5)
do
echo "marlin $i"
sleep 1
./bench_marlin.sh &> marlin_$i.log
tail -n 4 marlin_$i.log
done

