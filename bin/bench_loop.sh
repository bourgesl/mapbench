#!/bin/bash

touch loop.log

for i in {1..5}
do
   echo "Run benchmark [$i pass]"
   ./bench_marlin.sh &>> loop.log
done

echo "done."