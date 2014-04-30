#!/bin/bash

touch loop_ref.log

for i in {1..5}
do
   echo "Run benchmark [$i pass]"
   ./bench.sh &>> loop_ref.log
done

echo "done."
