echo "ductus"
#./bench.sh &> ductus.log

for i in $(seq 5)
do
echo "marlin ojdk $i"
sleep 1
./bench_marlin_ojdk.sh &> marlin_ojdk_$i.log
tail -n 4 marlin_ojdk_$i.log
done
