#!/bin/bash

rm loop.log
touch loop.log

for i in {1..9}
do
   echo "Run benchmark [$i pass]"
   ./bench_marlin.sh &>> loop.log
done

echo "done."
