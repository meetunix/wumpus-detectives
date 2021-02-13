# wumpus-detectives

**wumpus-detective** soll ein einfacher Agent werden, der in einem Multi-Agenten-Szenario
zusammen mit anderen Agenten in der Wumpus-Welt überleben soll.


## Übersetzen

```
$ mvn clean compile package
```

## Starten

```
$ cp target/wumpusDetective-[VERSION]-jar-with-dependencies.jar wDetective.jar
$ java -jar wDetective.jar --local-url wd://localhost:2000 --name jochen ----monitor-url wd://example.com:6666

```

Unter Linux/BSD/Mac können mithilfe des Skriptes `test-scripts/startParallelAgents.sh`
mehrere Agenten parallel gestartet werden. Zur Benuztung wird
[GNU Parallel](https://www.gnu.org/software/parallel/) benötigt.

Die Agenten verbinden sich mit einem Monitor unter der Adresse `localhost:6666`. Das
Skript kann einfach angepasst werden um zum Beispiel einen Monitor auf einem anderem
System zu verwenden.

##### Beispiel - Starten von 16 Agenten:


```bash
test-scripts/startParallelAgents.sh 16
```

## Ausgabe

Auch der Agent gibt seinen Zustand auf der Kommandozeile aus. Im folgenden Beispiel,
sieht man die Ausgabe eines *CarefullAgent* (rechts) und die Ausgabe des Monitors (links)
mit drei weiteren Agenten, einer Kommunikationsdistanz von 4 Feldern und einer
Drosselung von 50 ms pro Aktion. Man kann sehr gut erkennen, wie der Agent seinen
Weltzustand aktualisiert, wenn andere Agenten in Kommunikationsreichweite sind.


![Ausgabe CarefullAgent](../media/agent_4_agents.gif)

## Verwendung

Ein kurzes Beispiel für einen einfachen Agenten, der einfach in eine beliebige Richtung
läuft.


```java

public class LogicA2M implements AgentLogic {

    private static Logger logger = LogManager.getLogger(LogicA2M.class);

	private Agent 	a;

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
