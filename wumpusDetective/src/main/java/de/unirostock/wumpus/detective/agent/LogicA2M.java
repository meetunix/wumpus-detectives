package de.unirostock.wumpus.detective.agent;

import de.unirostock.wumpus.core.world.Action;
import de.unirostock.wumpus.core.world.Direction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.unirostock.wumpus.core.messages.ActionToken;
import de.unirostock.wumpus.core.messages.Message;
import de.unirostock.wumpus.core.messages.MonitorResponse;
import de.unirostock.wumpus.detective.entities.AgentContext;

public class LogicA2M implements AgentLogic {

    private static Logger logger = LogManager.getLogger(LogicA2M.class);

	private Agent 	a;

	public LogicA2M(AgentContext context) {
		this.a = new Agent(context); // create new agent and send description
	}

	@Override
	public void start() {
		
		while(a.isAlive()) {
			// wait until a new message arrives
			Message m = a.waitForMessage();
			
			if (m.getClass() == ActionToken.class) {
				logger.info("---- ActionToken arrived! Performing some action. -----");

				// read the message and write the content to my own state
				a.setStateFromMessage(m);
				
				// do some action: walk in a random direction
				while(!a.walk(Direction.getRandom())) {}
			
				// send my new state to the monitor
				a.sendMonitorInformation(Action.Type.MOVE, a.getPosition());

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