#!/bin/bash

unset _JAVA_OPTIONS

HOST="localhost"
MONITOR_URL="wumpus://localhost:6666"
MONITOR_COMMAND="java -jar wumpusMonitor/target/wumpusMonitor-0.0.0-jar-with-dependencies.jar -b http://127.0.0.1:12345 -l warn"
AGENT_PORT=10000

# do not change
AGENTS=(2 4 8 16 32)
DISTANCES=(0 1 2 3 4 6 8 10 20 30)

ITERATIONS=10

# generate the job_file later used by gnu parallel
# parameter: 1: agents, 2: distance
gen_jobs_file() {

    #at first: start the monitor
    echo "$MONITOR_COMMAND -w $MONITOR_URL -s $(($1 + 6)) -r $2 > /dev/null" > jobs_file

    for i in `seq 1 $1`
    do
        RAND=$(($RANDOM % 10000 + 1 ))
        PORT=$(($AGENT_PORT + $RAND)) # random port beetween 10001 and 20000
        COMMAND="java -jar wumpusDetective/target/wumpusDetective-0.0.0-jar-with-dependencies.jar -l http://$HOST:$PORT -m $MONITOR_URL -n AGENT-$PORT -v warn > /dev/null "

        echo $COMMAND

    done >> jobs_file
}

# parameter: 1: agents, 2: distance, 3: iteration
benchmark() {

    echo -e "\n--- Run simulation with $1 agents and a distance of $2 - run: $3  ---\n"
    # start monitor (+1) and agent processes with a delay of one second
    # if a simulation runs more than 6 minutes it will be killed
    START=$(date +%s)
    parallel --delay 1 --jobs $(($1 + 1)) --timeout 360 < jobs_file
    END=$(date +%s)
    STEPS=$(tail -n 1 results_benchmark.csv | cut -d "," -f "3")
    REWARD=$(tail -n 1 results_benchmark.csv | cut -d "," -f "4")
    echo "Simulation ended in  $(($END - $START)) seconds"
    echo "steps: $STEPS reward: $REWARD"

}

for agents in ${AGENTS[@]};
do
    for distance in ${DISTANCES[@]};
    do
        for iter in `seq 1 $ITERATIONS`;
        do
            gen_jobs_file $agents $distance
            benchmark $agents $distance $iter
        done

    done
done

rm jobs_file
