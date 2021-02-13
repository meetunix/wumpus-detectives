#!/bin/bash

# script starts 8 agents in parallel

PARALLEL_WORKER=$1
MONITORURL="wumpus://localhost:6666"
#MONITORURL="http://192.168.42.240:6666"
LOCAL_HOST="localhost"
#LOCAL_HOST="192.168.42.6"
LOCALPORT=10000

for i in `seq 1 $PARALLEL_WORKER`
do
    RAND=$(( $RANDOM % 10000 + 1 ))
    PORT=$(expr $LOCALPORT + $RAND) # random port beetween 10001 and 20000
    COMMAND="java -jar ../target/wumpusDetective-0.0.0-jar-with-dependencies.jar -l http://$LOCAL_HOST:$PORT -m $MONITORURL -n AGENT-$PORT -v warn"

    echo $COMMAND

done > jobs_file

echo -e "\n--- parallel execution ---"
time parallel --delay 0.5 --jobs $PARALLEL_WORKER < jobs_file

#echo -e "\n--- serial execution ---"
#
#time for i in `seq 1 $ITERATIONS`
#do
#    eval $COMMAND
#done
#
rm jobs_file
