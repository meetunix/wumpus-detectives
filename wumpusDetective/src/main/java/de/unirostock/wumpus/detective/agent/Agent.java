package de.unirostock.wumpus.detective.agent;

import java.net.URI;
import java.util.*;

import de.unirostock.wumpus.core.messages.*;
import de.unirostock.wumpus.core.world.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.unirostock.wumpus.core.communicator.Communicator;
import de.unirostock.wumpus.core.communicator.CommunicatorImpl;
import de.unirostock.wumpus.detective.entities.AgentContext;
/**
 * This class is the main API to control the detective.
 */
public class Agent {
	
    private static final Logger logger = LogManager.getLogger(Agent.class);
    private static final int WAIT_TIME = 2000; // millis

	private boolean isAlive = false;
	private final AgentContext context;
	private final Communicator com = new CommunicatorImpl();
	private final AgentWorldState agentWorldState;
	private int reward = 0;
	private int arrows = 10;

	// Map number of stenches (1-4) surrounding wumpus to wumpus with that number of known stenches
	private final HashMap<Integer, ArrayList<Coordinate>> wumpiWithKnownStenches = new HashMap<>();
	// Ignore this in further listings in wumpiWithKnownStenches
	private final HashSet<Coordinate> confirmedNoWumpi = new HashSet<>();
	// List of confirmed wumpi, slightly different list than wumpi with known stenches
	private final ArrayList<Coordinate> confirmedWumpi = new ArrayList<>();
	private final HashSet<Coordinate> ignoreCoordinatesWhileMerging = new HashSet<>();

	public Agent(AgentContext context) {
		this.context = context;
		this.agentWorldState = new AgentWorldState(WorldCreator.getEmptyWorld(GroundState.UNKNOWN));
		for (int i = 1; i <= 4; i++)
			wumpiWithKnownStenches.put(i, new ArrayList<>());

		if(subscribeAtMonitor())
			logger.info("subscription was successfully sent to monitor and was accepted");
		else
			logger.error("subscription could not be sent or was rejected by monitor");
	}
	
	

	/***************************** WorldState **********************************/
	
	/**
	 * This method takes a ActionToken or an MonitorResponse and write the state 
	 * sent by the monitor to the agent's state.
	 * 
	 * @param m either a ActionToken or an MonitorResponse message 
	 */
	public void setStateFromMessage(Message m) {
		if (m.getClass() == ActionToken.class) {
			ActionToken t = (ActionToken) m;
			context.setOtherAgents(t.getAgentsInRadius());
			updateAgentState(t.getCurrentPosition(), t.getStateAtCurrentPosition());
			logger.debug("Monitor sent me following agent list: {}", context.getOtherAgents());

			logger.info("I am at position: {} ", this.getPosition());
			logger.info("States at my current position: {} ", this.getGroundStatesAtCurrentPosition());
			logger.info("Current agents inside radius: {}", this.getAgents());
		}
		else if (m.getClass() == MonitorResponse.class) {
			MonitorResponse r = (MonitorResponse) m;
			updateAgentState(r.getCurrentPosition(), r.getStatus());
			this.isAlive = !r.isKillAgent();
			this.reward += r.getReward();
			
			logger.info(
					"STATUS: ALIVE: {}, REWARD: {}, POSITION {}, STATUS: {}",
					this.isAlive,
					this.reward,
					this.getPosition(),
					this.getGroundStatesAtCurrentPosition()
					);

		}
		// Just ignore instead of error.
		//else {
		//	logger.error("unable to read from message type {}", m.getClass());
		//}
	}

	private void inferWumpiOrPitsFromCoord(Coordinate coord) {
		Field curr = agentWorldState.getFieldOnPosition(coord);
		GroundState state = null;
		if (curr.hasGroundState(GroundState.BREEZE))
			state = GroundState.PIT;
		if (curr.hasGroundState(GroundState.STENCH))
			state = GroundState.WUMPUS;
		if (state == null)
			return;
		Coordinate unknown = null;
		for (Coordinate adj : coord.adjacentValid()) {
			Field adjField = agentWorldState.getFieldOnPosition(adj);
		    if (adjField.hasGroundState(state))
		    	return;
			if (adjField.isUnknown()) {
				if (unknown != null)
					return;
				unknown = adj;
			}
		}
		if (unknown != null) {
			agentWorldState.getFieldOnPosition(unknown).removeGroundState(GroundState.UNKNOWN);
			agentWorldState.getFieldOnPosition(unknown).addGroundState(state);
			if (state == GroundState.WUMPUS)
				confirmedWumpi.add(unknown);
			if (state == GroundState.PIT) {
				confirmedNoWumpi.add(unknown);
				confirmedNoWumpi.addAll(unknown.adjacentValid());
			}
		}
	}

	public Optional<Coordinate> getClosestConfirmedWumpusWithAdjacentUnknown() {
		Coordinate best = null;
		int dist = 999_999_999;
		for (Coordinate wumpusCoord : confirmedWumpi) {
			boolean hasUnknown = false;
			for (Coordinate adj : wumpusCoord.adjacentValid())
				if (agentWorldState.getFieldOnPosition(adj).isUnknown())
					hasUnknown = true;
			if (hasUnknown)
				if (getPosition().distance(wumpusCoord) < dist) {
					dist = getPosition().distance(wumpusCoord);
					best = wumpusCoord;
				}
		}
		return Optional.ofNullable(best);
	}

	private void removeAgentFromField(Field field) {
		if (field.containsAgent(this.context.getLocalUrl()))
			field.removeAgent(this.context.getLocalUrl());
	}

	private void updateAgentState(Coordinate newPos, Field newField) {
		Coordinate assumedPosition = Coordinate.from(getPosition());
		removeAgentFromField(newField);
		newField.setOtherAgents(null);
		// if (assumedPosition != null)
		// 	removeAgentFromField(agentWorldState.getFieldOnPosition(assumedPosition));
		agentWorldState.setCurrentPosition(newPos);
		agentWorldState.setFieldOnPosition(newField, newPos);
		removeCoordFromWumpiList(newPos);
		if (!getPosition().equals(assumedPosition) && assumedPosition != null) {
			agentWorldState.setFieldOnPosition(new Field(GroundState.ROCK), assumedPosition);
			logger.debug("Adding rock at " + assumedPosition);
		}
		handleIfStench(newPos);
		inferWumpiOrPitsFromCoord(newPos);
		for (Coordinate adj : newPos.adjacentValid())
			inferWumpiOrPitsFromCoord(adj);
	}

	private void handleIfStench(Coordinate coord) {
	    for (Coordinate adj : coord.adjacentValid()) {
	    	if (confirmedNoWumpi.contains(adj))
	    		continue;
	    	if (agentWorldState.getFieldOnPosition(adj).hasGroundState(GroundState.UNKNOWN)) {
				int stenches = 0;
				for (Coordinate adj2 : adj.adjacent()) {
					if (adj2.isValid()) {
						Field f = agentWorldState.getFieldOnPosition(adj2);
						// If coordinate has stench or is inaccessible count it as stench
						if (f.hasGroundState(GroundState.STENCH) || f.isInaccessible())
							stenches++;
						// If instead it is known and it has no stench then there can't be a wumpus here
						if (!f.isUnknown() && !f.hasGroundState(GroundState.STENCH)) {
							stenches = 0;
							break;
						}
					} else {
						stenches++;
					}
				}
				removeCoordFromWumpiList(adj);
				if (stenches != 0)
					wumpiWithKnownStenches.get(stenches).add(adj);
			}
		}
	}

	/**
	 * @param knownStenches integer between 1-4
	 * @return		list of wumpi with that number of adjacent stenches
	 */
	public List<Coordinate> getWumpiWithKnownStenches(int knownStenches) {
		return wumpiWithKnownStenches.get(knownStenches);
	}
	
	public Field getField() {
		return agentWorldState.getFieldOnPosition(this.getPosition());
	}
	
	public Map<URI,String> getAgents(){
		return context.getOtherAgents();
	}
	
	public Coordinate getPosition() {
		return agentWorldState.getCurrentPosition();
	}
	
	public void setPosition(Coordinate position) {
		agentWorldState.setCurrentPosition(position);
	}

	public List<GroundState> getGroundStatesAtPosition(Coordinate position){
		return agentWorldState.getFieldOnPosition(position).getGroundStates();
	}

	public List<GroundState> getGroundStatesAtCurrentPosition() {
		return getGroundStatesAtPosition(getPosition());
	}

	/***************************** AI **********************************/

	/**
	 * Traverses the known world using BFS looking for a UNKNOWN tile
	 * which is adjacent to a safe (FREE, no STENCH or BREEZE) tile.
	 *
	 * @return Optional Coordinate of a safe tile if it exists
	 */
	public Optional<Coordinate> findClosestCoordinateWithGroundState(GroundState ofType) {
		ArrayDeque<Coordinate> bfsQueue = new ArrayDeque<>();
		bfsQueue.add(getPosition());
		HashSet<Coordinate> visited = new HashSet<>();
		while (!bfsQueue.isEmpty()) {
			Coordinate curr = bfsQueue.pollFirst();
			visited.add(curr);
			List<GroundState> currStates = getGroundStatesAtPosition(curr);
			logger.trace("\t" + curr + " -> " + currStates);
			for (Coordinate adj : curr.adjacent()) {
				if (adj.isValid() && !visited.contains(adj)) {
					List<GroundState> adjStates = getGroundStatesAtPosition(adj);
					logger.trace("\t\t" + adj + " -> " + adjStates);
					if (currStates.contains(GroundState.FREE)
							&& !currStates.contains(GroundState.BREEZE)
							&& !currStates.contains(GroundState.STENCH))
						if (adjStates.contains(ofType))
							return Optional.of(adj);
					if (adjStates.contains(GroundState.FREE)) {
						bfsQueue.add(adj);
						visited.add(adj);
					}
				}
			}
		}
		return Optional.empty();
	}

	public Optional<Coordinate> findClosestUnexploredSafeCoordinate() {
		return findClosestCoordinateWithGroundState(GroundState.UNKNOWN);
	}


	public ArrayDeque<Direction> getSafePathToCoordinate(Coordinate goal) {
		ArrayDeque<Coordinate> bfsQueue = new ArrayDeque<>();
		bfsQueue.add(getPosition());
		HashMap<Coordinate, Coordinate> previous = new HashMap<>();
		previous.put(getPosition(), null);
		while (!bfsQueue.isEmpty() && !previous.containsKey(goal)) {
			Coordinate curr = bfsQueue.pollFirst();
			// List<GroundState> currStates = getGroundStatesAtCurrentPosition();
			for (Coordinate adj : curr.adjacent()) {
				if (adj.isValid() && !previous.containsKey(adj)) {
					List<GroundState> adjStates = getGroundStatesAtPosition(adj);
					if (adjStates.contains(GroundState.FREE) || adj.equals(goal)) {
						bfsQueue.add(adj);
						previous.put(adj, curr);
					}
				}
			}
		}
		ArrayDeque<Coordinate> path = new ArrayDeque<>();
		path.add(goal.copy());
		while (!path.getFirst().equals(getPosition()))
			path.addFirst(previous.get(path.getFirst()));
		Coordinate curr = path.pollFirst();
		ArrayDeque<Direction> directions = new ArrayDeque<>();
		for (Coordinate next : path) {
		    // If optional is null or curr is null something has horribly gone wrong anyway
			directions.addLast(next.copy().subtract(curr).asDirection().get());
			curr = next;
		}
		return directions;
	}

	private void removeCoordFromWumpiList(Coordinate coord) {
		for (int i = 1; i <= 4; i++)
			wumpiWithKnownStenches.get(i).remove(coord);
		confirmedWumpi.remove(coord);
	}

	public int getArrows() {
		return arrows;
	}

	public void takeAction(Action action) {
		switch (action.getType()) {
			case MOVE:
				Optional<Direction> dir = action.getCoordinate().copy().subtract(getPosition()).asDirection();
				if (dir.isPresent())
					walk(dir.get());
				else {
					logger.error("Move direction is illegal! From " + getPosition() + " to " + action.getCoordinate());
					while (!walk(Direction.getRandom()));
				}
				break;
			case SHOOT:
				// sleep(1000);
				if (!agentWorldState.getFieldOnPosition(action.getCoordinate()).isUnknown())
					agentWorldState.setFieldOnPosition(new Field(GroundState.UNKNOWN), action.getCoordinate());
				for (Coordinate adj : action.getCoordinate().adjacentValid())
					if (!adj.equals(getPosition()))
						agentWorldState.setFieldOnPosition(new Field(GroundState.UNKNOWN), adj);
				confirmedNoWumpi.add(action.getCoordinate());
				removeCoordFromWumpiList(action.getCoordinate());
				ignoreCoordinatesWhileMerging.add(action.getCoordinate());
				ignoreCoordinatesWhileMerging.addAll(action.getCoordinate().adjacentValid());
				arrows -= 1;
				break;
			case LEAVE:
				// kill(); don't kill myself as otherwise monitor information with leave does not get sent
				break;
		}

		// send my new state to the monitor
		sendMonitorInformation(action.getType(), action.getCoordinate());
	}


	/***** Movement *****/
	
	/**
	 * Let the agent walk to Direction direction. The action is only committed, if walking
	 * to direction is possible.
	 * 
	 * @param direction (N, S, E, W)
	 * @return True if walking to the requested direction is possible
	 */
	public boolean walk(Direction direction) {
		Coordinate currPos = agentWorldState.getCurrentPosition();
		currPos.add(direction);
		if (currPos.isValid())
			return true;
		currPos.validate();
		return false;
	}
	

	/***************************** Communication **********************************/

	/**
	 * Sends a subscription to the monitor and awaits a response. If a response arrives,
	 * the currently registered agents and the current state will be updated.
	 * 
	 * @return true if subscription was successful
	 */
	private boolean subscribeAtMonitor() {

		if (!sendMessage(new SubscriptionRequest(context)))
			return false;

		// give the monitor some time to process the message and send a response
		this.sleep(WAIT_TIME);

		/* There should be no other message in the message queue, because no other agent knows
		 * about my existence. Therefore drain all messages from queue and search for the
		 * subscription response from the monitor, all other messages are ignored.
		 */
		Optional<Message> responseOptional = getAllMessages().stream()
				.filter(m -> m.getClass() == SubscriptionResponse.class)
				.findFirst();
		
		if (responseOptional.isPresent()) {
			SubscriptionResponse response = (SubscriptionResponse) responseOptional.get();
			this.isAlive = true;
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Send the MonitorInformation message. 
	 * 
	 * @return True if the message was successfully sent.
	 */
	public boolean sendMonitorInformation(Action.Type type, Coordinate actionCoordinate) {
		MonitorInformation m = new MonitorInformation(context);
		m.setAction(new Action(type, actionCoordinate));

		logger.info("Sending message to monitor " + m);
		return sendMessage(m);
	}

	/**
	 * Sends each known agent in radius a state request
	 *
	 * @return true if all messages successful, otherwise false
	 */
	public boolean sendAgentStateRequests() {
		for (URI otherAgentURI : getAgents().keySet())
			if (!sendMessage(new AgentStateRequest(context, otherAgentURI)))
				return false;
		return true;
	}

	/**
	 * Replies to AgentStateRequest
	 *
	 * @param requestingAgentURI
	 * @return true if message successfully sent
	 */
	public boolean replyToAgentStateRequest(URI requestingAgentURI) {
		return sendMessage(new AgentStateResponse(context, requestingAgentURI, agentWorldState));
	}

	/**
	 * Merges current agent state with other agent's agent state
	 * @param response
	 */
	public void parseAgentStateResponse(AgentStateResponse response) {
		agentWorldState.merge(response.getState(), ignoreCoordinatesWhileMerging);
	}

	/**
	 * Send an arbitrary Message.
	 * 
	 * @param message
	 * @return
	 */
	public boolean sendMessage(Message message) {
		return com.sendMessage(message.getRecipientURL(), message, 100);
	}
	
	/**
	 * Wait until a Message arrives.
	 * 
	 * @return a Message object
	 */
	public Message waitForMessage() {
		logger.trace("Message size before: " + context.getAcceptorQueue().size());
		Message m = context.getAcceptorQueue().take();
		logger.trace("Message size after: " + context.getAcceptorQueue().size());
		return m;
	}

	private Object ignoreIfNextMessageIsThisClass = null;

	/**
	 * Wait until a Message arrives.
	 *
	 * @return a Message object
	 */
	public Message waitForMessageAndGuaranteeOrder() {
	    while (true) {
	        Message m = waitForMessage();
			if (m.getClass() == ignoreIfNextMessageIsThisClass) {
				context.getAcceptorQueue().put(m);
			} else {
				if (m.getClass() == MonitorResponse.class || m.getClass() == ActionToken.class)
					ignoreIfNextMessageIsThisClass = m.getClass();
				return m;
			}
		}
	}

	/**
	 * Checks if a Message is in the Queue. If so, it returns The Message inside an
	 * Optional. If no there is null inside the Optional.
	 * 
	 * @return an Optional of Type Message, could be null.
	 */
	public Optional<Message> getMessageInstant() {
		Message msg = context.getAcceptorQueue().poll();
		
		return Optional.ofNullable(msg);
	}
	
	
	public List<Message> getAllMessages() {
		return context.getAcceptorQueue().getAllMessages();
	}
	
	public boolean isAlive() {
		return isAlive;
	}

	/**
	 * Sends an unsubscribe-message to the monitor and sets isAlive to false.
	 * 
	 */
	public void kill() {

		if (sendMessage(new UnsubscribeRequest(context)))
			logger.info("unsubscription to monitor successfully sent");
		else
			logger.warn("unsubscription to monitor could not be sent... kill the agent anyway");

		this.isAlive = false;
	}
	
	/**
	 * The agent is going to sleep for millis milliseconds.
	 * 
	 * @param millis
	 */
	public void sleep(int millis) {
		if (getPosition() != null) {
			agentWorldState.getFieldOnPosition(getPosition()).addAgent(context.getLocalUrl(), context.getName());
			// clear the screen
			System.out.print("\033[H\033[2J");
			// Uncomment this to remove flickering as this puts the console cursor at 0 0
			//System.out.println("\033[0;0H");
			System.out.println(WorldCreator.printWorldState(agentWorldState));
			System.out.println("Arrows: " + arrows + "     ");
			System.out.println("Reward: " + reward + "     ");
			// System.out.println("Confirmed wumpi:");
			// for (Coordinate shot : confirmedWumpi)
			// 	System.out.print(shot + ", ");
			// System.out.println("\nWumpi shot at:");
			// for (Coordinate shot : wumpiShotAt)
			// 	System.out.print(shot + ", ");
			agentWorldState.getFieldOnPosition(getPosition()).removeAgent(context.getLocalUrl());
		}
		try {
			logger.trace("going to sleep for {} seconds", (double) millis/1000);
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			logger.error("interrupted while sleeping");
		}
		
	}

}
