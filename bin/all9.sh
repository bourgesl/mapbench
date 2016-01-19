echo "ductus"
#./bench.sh &> ductus.log

for i in $(seq 5)
do
echo "marlin ojdk9 $i"
sleep 1
./bench_marlin_ojdk9.sh &> marlin_ojdk9_$i.log
tail -n 4 marlin_ojdk9_$i.log
done
