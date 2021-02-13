# wumpus-detectives

[German README here](README_DE.md)

**wumpus-detective** is to a simple agent, surviving in a multi-agent scenario together with other agents in the Wumpus world.


## Compilation

```
$ mvn clean compile package
```

## Running

```
$ cp target/wumpusDetective-[VERSION]-jar-with-dependencies.jar wDetective.jar
$ java -jar wDetective.jar --local-url wd://localhost:2000 --name jochen ----monitor-url wd://example.com:6666

```

On Linux/BSD/Mac you can start multiple agents in parallel using the script `test-scripts/startParallelAgents.sh`. To use it, [GNU Parallel](https://www.gnu.org/software/parallel/) is required.

The agents connect to a monitor at the address `localhost:6666`. The script can be easily adapted to use a monitor on another system for example.

##### Example - starting 16 agents:


```bash
test-scripts/startParallelAgents.sh 16
```

## Output

The agent also prints its state on the command line. In the following example, you can see the output of a *CarefulAgent* (right) and the output of the monitor (left) with three other agents, a communication distance of 4 fields and a throttling of 50 ms per action. You can see very clearly how the agent updates its world state when other agents are within communication range.


![Output CarefulAgent](../media/agent_4_agents.gif)

## Usage

A short example of a simple agent that simply runs in an arbitrary direction.


```java

public class LogicA2M implements AgentLogic {

    private static Logger logger = LogManager.getLogger(LogicA2M.class);

	private agent a;

	public LogicA2M(AgentContext context) {
		this.a = new Agent(context); // create new agent and send description
	}

	@Override
	public void start() {

		while(a.isALive()) {
			// wait until a new message arrives
			Message m = a.waitForMessage();

			if (m.getClass() == ActionToken.class) {
				logger.info("---- ActionToken arrived! Performing some action. -----");

				// read the message and write the content to my own state
				a.setStateFromMessage(m);

				// do some action: walk in a random direction
				while(!a.walk(a.getRandomDirection())) {}

				// send my new state to the monitor
				a.sendMonitorInformation();

			} else if (m.getClass() == MonitorResponse.class) { // response for MonitorInformation
				logger.info("---- MonitorResponse arrived! -----");

				// read the message and write the content to my own state
				a.setStateFromMessage(m);
				// I'am so sleepy
				a.sleep(100);
				// send unsubscribe message and kill myself
				//a.kill();
			}
		}
	}
}
```
