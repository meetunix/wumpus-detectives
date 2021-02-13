<h1 align="center">wumpus-detectives</h1>

<p align="center">
<a href="https://github.com/meetunix/wumpus-detectives/blob/main/LICENSE" title="License">
<img src="https://img.shields.io/badge/License-Apache%202.0-green.svg?style=flat"></a>
</p>

[German README here](README.md)

WumpusDetectives is a multi-agent framework for the simulation of an extended [wumpus world](https://de.wikipedia.org/wiki/Wumpus-Welt).
The world is simulated by the wumpusMonitor.
The agents (wumpusDetective) can communicate with each other and with the monitor via a simple TCP-based protocol.

## The Agents (wumpusDetective)

The agents are implemented in Java and can send messages to each other via a very simple
simple protocol (via TCP). Each agent needs an
installed Java Runtime Environment (JRE). The agent itself is a JAR file in
which already contains all dependencies. The currently used agent logic
*CarefulAgent* was developed by [LiquidFun](https://github.com/LiquidFun).
The rest of the project was developed by [meetunix](https://github.com/meetunix)

[**Documentation for wumpusDetective**](wumpusDetective/README_EN.md)


**Example output of the monitor and an agent (CarefulAgent):**

![Output CarefulAgent](media/agent_4_agents.gif)



## The Monitor (wumpusMonitor)

The monitor generates and manages the game world. It provides an interface for
agents to register to, report their state and get information about their current position.
The monitor also provides an HTTP endpoint that can be used to query the entire state of the world.
An external software can use this data for visualization. 
For each simulation parameters and results are are stored in the file `results_benchmark.csv` for later evaluation.


[**Documentation of the wumpusMonitor**](wumpusMonitor/README_EN.md)




## Shared library (wumpusCore)

*wumpusCore* provides shared classes and is automatically compiled together with the other projects.


## Compilation

Java 11 and Apache Maven are required.

With the following command both projects (*wumpusDetective* and
*wumpusMonitor*) can be compiled.

```
mvn clean compile package
```

## Simulation and result visualization

The shell script `benchmark.sh` can be used to run simulations with different parameters.
For execution [GNU Parallel](https://www.gnu.org/software/parallel/) is used which takes care of the concurrent execution of the monitor and the agents.
GNU Parallel should be available as a package in every common Linux distribution, mostly under the name `parallel`.
Since a separate Java VM is started for each agent, the memory requirements are relatively high.
A simulation with 32 agents needs about 14 GiB memory on one system.
The agents can be ran on different systems, which means that simulations with more than 100 agents are possible.


The Python script `plot_results.py` visualizes the simulation results:

    plot_results.py results_benchmark.csv

![Steps](misc/result_steps.png)
![Reward](misc/result_rewards.png)

## Visualization (wumpusVisualization)

Visualizes the simulated world.

See (TODO: add repository)
