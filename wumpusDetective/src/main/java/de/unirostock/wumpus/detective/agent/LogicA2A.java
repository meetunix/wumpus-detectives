package de.unirostock.wumpus.detective.agent;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.unirostock.wumpus.core.messages.Message;
import de.unirostock.wumpus.detective.entities.AgentContext;


public class LogicA2A implements AgentLogic {
	
    private static Logger logger = LogManager.getLogger(LogicA2A.class);

	private Agent 	a;
	private AgentContext context;

	public LogicA2A(AgentContext context) {
		this.context = context;
		this.a = new Agent(context);
	}
	
	public void start() {

		while(a.isAlive()) {
			
			if (!context.getName().equalsIgnoreCase("dieter")) {
				
				//for (int i = 0 ; i < 50000 ; i++) {

					Message mm = new Message(context, context.getMonitorUrl());
				
					logger.debug("I'am going to send my message to {}", context.getMonitorUrl());
					while (! a.sendMessage(mm)) {
						a.sleep(20);
					}
					//a.sleep(50);
				//}
			}
			
			if (context.getName().equalsIgnoreCase("dieter")) {
				a.sleep(1000);
				logger.info("TRYING TO RECEIVE MESSAGES FROM QUEUE");

				//while(context.getQueue().hasMessage()) {
				int i = 0;
				while(true) {
					i++;
					a.sleep(1000);
					List<Message> list = a.getAllMessages();
					/*
					Message m1 = context.getQueue().take();
					logger.trace(m1.toString());
					*/
					logger.info("Iteration {} - got {} messages", i, list.size());
				}
			}
			
			logger.info("goodbye");
			a.sleep(1000);
			a.kill();
		}
		logger.info("Agent killed");
	}
}