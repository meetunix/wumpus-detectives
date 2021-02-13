package de.unirostock.wumpus.monitor.world;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.unirostock.wumpus.core.messages.*;
import de.unirostock.wumpus.core.world.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.unirostock.wumpus.core.communicator.Communicator;
import de.unirostock.wumpus.core.communicator.CommunicatorImpl;
import de.unirostock.wumpus.core.messageQueue.MessageQueue;
import de.unirostock.wumpus.core.messageQueue.MessageQueueImpl;
import de.unirostock.wumpus.monitor.entities.MonitorContext;


/**
 * This thread takes a new generated world and consumes messages from a message queue. The
 * messages in the queue are provided by the acceptor and were sent by agents.
 * 
 * While processing a message the world state may be altered. The minimal change is the update
 * of the agents position. The state of the new explored field will be sent back to the agent.
 * 
 * After this the next message is processed.
 * 
 */

public class WorldOperatorThread implements Runnable {

    private static Logger logger = LogManager.getLogger(WorldOperatorThread.class);

    private Duration subscriptionPhase;
    private Instant threadStartedAt;
    
    MessageQueue 			acceptorQueue;
    MonitorContext			context;
    Communicator			com;
    Map<URI,MessageQueue>	mailboxMap;
    List<URI>				actionSequence;
    
    Thread					mailboxManager;
    

    public WorldOperatorThread(MonitorContext context) {
    	this.acceptorQueue = context.getAcceptorQueue();
    	this.context = context;
    	this.threadStartedAt = Instant.now();
    	this.mailboxMap = new HashMap<>();
    	this.actionSequence = new LinkedList<>();
    	this.com = new CommunicatorImpl();
    	this.subscriptionPhase = Duration.ofSeconds(context.getSecondsSubscriptionPhase());

		this.mailboxManager = new Thread(
						new	MailboxManager(mailboxMap,acceptorQueue),
						"MailBoxManager");
	}
    
    
    private void sendMessage(URI recipientURI, Message message) {
		com.sendMessage(recipientURI, message, 2000);
    }

    private boolean subscriptionTime() {
    	return Duration.between(threadStartedAt, Instant.now()).compareTo(subscriptionPhase) <= 0;
    }
    
    /**
     * Adds the sender to the list of registered agents and sends a response to the sender with
     * a list of all agents currently registered at the Monitor;
     * 
     * TODO: Add the coordinates for the Agent to the response and refresh the worldState
     * 
     * @param m  The SubscritionRequest message from the agent
     */
    private void processSubscription(Message m) {

    	if (subscriptionTime()) {
			logger.info("subscription request arrived from {} - {}",
					m.getSenderName(), m.getSenderURL());
			System.out.printf("Agent %s subscribed from %s\n", m.getSenderName(), m.getSenderURL());

			context.addAgent(m.getSenderURL(), m.getSenderName());

			// create new Mailbox for the sender and add him to action list
			mailboxMap.put(m.getSenderURL(), new MessageQueueImpl());
			actionSequence.add(m.getSenderURL());
			
			// send response to client
			SubscriptionResponse response = new SubscriptionResponse(context,m.getSenderURL());
			sendMessage(m.getSenderURL(), response);
			logger.debug("subscription response to: {} - {}",
					m.getSenderName(), m.getSenderURL());

    	} else {
    		logger.info("Incoming subscription request ignored. Subscription phase is over!");
    	}
    }

    private void processUnsubscribe(Message m) {

    	logger.info("unsubscription request arrived from {} - {}",
    			m.getSenderName(), m.getSenderURL());
    	
    	context.removeAgent(m.getSenderURL());
    	mailboxMap.remove(m.getSenderURL());
    	actionSequence.remove(m.getSenderURL());
    	// TODO remove the agents position from WorldState
    }
    

    /**
     * The MonitorInformation message (m) sent from the agent is processed. The complete
     * logic of the world has to be implemented here. 
     * 
     * @param m Message from type MonitorInformation
     */
    private void processMonitorInformation(Message m) {
    	MonitorInformation info = (MonitorInformation) m;
    	MonitorWorldState state = context.getMonitorState();

		MonitorResponse response = new MonitorResponse(context, info.getSenderURL());
		Action action = info.getAction();
		Coordinate actionCoord = action.getCoordinate();
		if (!actionCoord.isValid()) {
			System.out.println("Coordinate " + actionCoord + " is invalid!");
			System.out.println("Action: " + action.getType() + " by " + info.getSenderName());
		}
		Field actionField = state.getFieldOnPosition(actionCoord);
		Coordinate currAgentCoord = state.getAgentsCoordinates(info.getSenderURL());
		response.setCurrentPosition(currAgentCoord);
		
		state.updateLastActionForAgent(info.getSenderURL(), action);

    	if (action.getType() == Action.Type.MOVE) {

			logger.debug(
					"{} moves from position {} to {}.",
					info.getSenderName(),
					state.getAgentCoordinates().get(info.getSenderURL()),
					action.getCoordinate()
					);


			response.setCurrentPosition(action.getCoordinate());
			response.setStatus(state.getFieldOnPosition(action.getCoordinate()));

			if (actionField.hasGroundState(GroundState.WUMPUS)) {
				logger.info("WUMPUS-KILL: Agent {} was killed by wumpus in round {}",
						info.getSenderName(), context.getCurrentActionCount());

				response.killAgent();

			} else if (actionField.hasGroundState(GroundState.PIT)) {
				logger.info("PIT-KILL: Agent {} fell into a pit at round {}",
						info.getSenderName(), context.getCurrentActionCount());

				response.killAgent();

			} else if (actionField.hasGroundState(GroundState.ROCK)) { // do not update agents position
				response.setCurrentPosition(currAgentCoord);
				response.setStatus(state.getFieldOnPosition(currAgentCoord));
			} else if (actionField.hasGroundState(GroundState.GOLD)) {
				response.setReward(1000);
				context.addToEarnedReward(1000);
				state.removeGroundState(GroundState.GOLD, response.getCurrentPosition());
			}
    	} else if (action.getType() == Action.Type.LEAVE) {
			response.killAgent(); // Make sure to "kill" agent before setting reward to 10k as this overwrites it
			if (state.getFieldOnPosition(currAgentCoord).hasGroundState(GroundState.EXIT)) {
				logger.info("EXIT: Agent {} found the exit in {} rounds",
						state.getAgentName(response.getRecipientURL()),
						context.getCurrentActionCount()
				);
				response.setReward(10000);
				context.addToEarnedReward(10000);
			}
			response.setStatus(state.getFieldOnPosition(currAgentCoord));
		} else if (action.getType() == Action.Type.SHOOT) {
			if (actionField.hasGroundState(GroundState.WUMPUS)) {
				actionField.removeGroundState(GroundState.WUMPUS);
				if (actionField.groundStateCount() == 0)
					actionField.addGroundState(GroundState.FREE);
				// Remove all adjacent stenches which have no other adjacent wumpi
				for (Coordinate adj : actionCoord.adjacentValid()) {
					boolean noAdjacentWumpi = true;
					for (Coordinate adj2 : adj.adjacentValid())
						if (state.getFieldOnPosition(adj2).hasGroundState(GroundState.WUMPUS))
							noAdjacentWumpi = false;
					if (noAdjacentWumpi) {
						state.getFieldOnPosition(adj).removeGroundState(GroundState.STENCH);
						if (state.getFieldOnPosition(adj).groundStateCount() == 0)
							state.getFieldOnPosition(adj).addGroundState(GroundState.FREE);
					}
				}
			}
			// Set response field after removing wumpus and stenches
			response.setStatus(state.getFieldOnPosition(currAgentCoord));
		}
    	
    	// if agent must die: delete him from world, action sequence and drop his message queue
    	if (response.isKillAgent()) {
    		logger.info("DEAD: Agent {} killed after {} rounds.",
    				state.getAgentName(response.getRecipientURL()),
    				context.getCurrentActionCount()
    				);

    		state.removeAgent(response.getRecipientURL());
    		actionSequence.remove(response.getRecipientURL());
    		mailboxMap.remove(response.getRecipientURL());

    	} else {
    		state.updateAgent(response.getRecipientURL(), response.getCurrentPosition());
    	}

    	state.incrementVersion();
    	sendMessage(info.getSenderURL(), response);
    }
    
    /**
     * Takes a arbitrary message and invoke the correct action.
     * 
     * @param m a arbitrary message from a client.
     */
    private void processMessage(Message m) {
    	
    	logger.debug("Incoming message {} from: {}", m.getClass(), m.getSenderName());

		if (m.getClass() == SubscriptionRequest.class)
			processSubscription(m);
		else if (m.getClass() == UnsubscribeRequest.class)
			processUnsubscribe(m);
		else if (m.getClass() == MonitorInformation.class)
			processMonitorInformation(m);
		else
			logger.error("An unsupported message type was sent from {} - {}",
					m.getSenderName(), m.getSenderURL());
    }
    
    private void sendActionToken(URI agentURI) {
    	MonitorWorldState state = context.getMonitorState();
    	ActionToken token = new ActionToken(context, agentURI);
    	Coordinate currentAgentPos = context.getMonitorState().getAgentsCoordinates(agentURI);
    	logger.debug("Sending ActionToken with Coordinates: {}", currentAgentPos);

    	token.setCurrentPosition(currentAgentPos);
    	// send all agents inside the given radius inside the action token
    	token.setAgentsInRadius(state.getAgentsInRadius(
    			context.getCommunicationRadius(),
    			currentAgentPos,
    			context.getRegisteredAgents()
    			));

    	token.setStateAtCurrentPosition(
    			context.getMonitorState()
    			.getFieldOnPosition(currentAgentPos));

    	sendMessage(agentURI, token);
	}
    
    
    private void action() {
    	
    	// needed because actionSequence is altered while iteration
    	List<URI> currentActionList = new ArrayList<>(actionSequence);

		for (URI agentURI: currentActionList) {
			sendActionToken(agentURI);
			logger.debug("ActionToken sent to {} ", context.getAgentName(agentURI));

			// switch to the suitable queue and process message
			MessageQueue q = mailboxMap.get(agentURI);
			processMessage(q.take());
		}
    }
    
    private static void throttleDownForMillis(long millis) {

    	try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			logger.error("Interrupted while sleeping.");
			Thread.currentThread().interrupt();
		}
    }

	@Override
	public void run() {
			
		/**************************
		 * Phase 1 - Subscription *
		 **************************/
		logger.info("-------- Starting subscription phase for {} seconds. --------",
				subscriptionPhase.getSeconds());

		// wait for subscription messages
		while (subscriptionTime()) { 
			Message m = acceptorQueue.poll(500);
			if (m != null) {processMessage(m);}
		}

		logger.info("---------- Starting action phase ----------");

		// from now an all incoming messages will be distributed to the agents personal mailbox
		logger.debug("starting mailbox manager");
		mailboxManager.start();
			
			
		/**************************
		 * Phase 2 - Agent action *
		 **************************/

		// create the world
		List<Integer> config = new ArrayList<>();
		config.add(context.getFieldWidth());
		config.add(context.getNumberWumpi());
		config.add(context.getNumberPits());
		config.add(context.getNumberGold());
		config.add(context.getNumberRocks());
		config.add(context.getNumberExits());
		context.setMonitorState(
				new MonitorWorldState(WorldCreator.getWorld(context.getRegisteredAgents(), config)));
		
		logger.info("World created with following agents: {} ",
				context.getMonitorState().getAgentCoordinates());
		
		context.setAmountInitialAgents(actionSequence.size());
		
		// clear the screen
		System.out.print("\033[H\033[2J");

		while(! Thread.currentThread().isInterrupted() ) {

				
			context.incrementActionCount();
			
			if (actionSequence.size() > 0) {

				String logActionCount = new String(String.format(
						"### action sequence number: %d with %d registered agents ###",
						context.getCurrentActionCount(),actionSequence.size()
						));
				logger.debug(logActionCount);

				action(); 

				// Uncomment this to remove flickering as this puts the console cursor at 0 0
				System.out.println("\033[0;0H");
				System.out.println(logActionCount + "\n");
				System.out.println(WorldCreator.printWorldState(context.getMonitorState()));
				
				// throttle down the simulation (no throtteling per default)
				throttleDownForMillis(context.getThrottleMillis());

			} else {
				logger.info("No agents in action queue ... closing world operation thread");
				context.simulationSuccessful = true;
				Thread.currentThread().interrupt();
			}
		}
	}
}
