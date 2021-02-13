package de.unirostock.wumpus.detective.agent;

import de.unirostock.wumpus.core.messages.*;
import de.unirostock.wumpus.core.world.*;
import de.unirostock.wumpus.detective.entities.AgentContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class CarefulAgent implements AgentLogic {

    private static final Logger logger = LogManager.getLogger(CarefulAgent.class);

	private final Agent a;
	private final ArrayDeque<Action> actionQueue = new ArrayDeque<>();

	public CarefulAgent(AgentContext context) {
		this.a = new Agent(context); // create new agent and send description
	}

	private boolean parseActionPath(Coordinate goal, Action.Type replaceLastWith) {
		Coordinate curr = a.getPosition().copy();
		for (Direction dir : a.getSafePathToCoordinate(goal))
			actionQueue.addLast(new Action(Action.Type.MOVE, curr.add(dir).copy()));
		if (replaceLastWith != null) {
		    Action last = actionQueue.pollLast();
		    if (last != null)
				actionQueue.addLast(new Action(replaceLastWith, last.getCoordinate()));
		}
		return true;
	}

	private void clearPathBeforeShotIfWumpusNoLongerExists() {
		if (a.getPosition() == null || actionQueue.isEmpty())
			return;

		Action first = actionQueue.getFirst();
		if (first.getType() == Action.Type.SHOOT) {
			if (!a.getField().hasGroundState(GroundState.STENCH)) {
				logger.debug("Clearing path as ground state has no (longer) stench");
				actionQueue.clear();
			}
			//else
			//	actionQueue.addLast(new Action(Action.Type.MOVE, first.getCoordinate()));
		}
	}


	private boolean computePathIfNeeded() {
	    clearPathBeforeShotIfWumpusNoLongerExists();
		// If not yet initialized -> do nothing
		if (a.getPosition() == null || !actionQueue.isEmpty())
			return false;

		// logger.debug("My position: " + a.getPosition());

	 	//	If there is field with 4 known adjacent stenches -> move to it and kill wumpus
		//if (!a.getWumpiWithKnownStenches(4).isEmpty())
		//	return parseActionPath(a.getWumpiWithKnownStenches(4).get(0), Action.Type.SHOOT);
		logger.debug("Computing path");

	  	//	If there is a safe coordinate -> move to it
		Optional<Coordinate> safeCoordinate = a.findClosestUnexploredSafeCoordinate();
		if (safeCoordinate.isPresent()) {
			logger.debug("Safe coordinate to move to at " + safeCoordinate.get());
			return parseActionPath(safeCoordinate.get(), null);
		}

		// Hunt wumpi if we still have arrows
		if (a.getArrows() > 0) {

			// Hunt closest confirmed wumpus
			Optional<Coordinate> closestWumpus = a.getClosestConfirmedWumpusWithAdjacentUnknown();
			if (closestWumpus.isPresent()) {
				logger.debug("Hunting closest wumpus at " + closestWumpus.get());
				return parseActionPath(closestWumpus.get(), Action.Type.SHOOT);
			}

			// If there is a wumpus with known 3, 2, 1, or 4 adjacent stenches -> move to it and kill it
			// The reasoning behind this order is that a wumpus with 4 adjacent stenches is very unlikely
			// to help get through anywhere, as it already meant that all its fields had been accessible.
			// The only reason to shoot it as in case agents communicate about a wumpus from 2 enclosed
			// sides of the world, then it is useful and might be done as a last ditch attempt to do something.
			List<Coordinate> wumpi = new ArrayList<>();
			for (int i : new int[]{3, 2, 1, 4}) {
				if (!a.getWumpiWithKnownStenches(i).isEmpty()) {
					wumpi = a.getWumpiWithKnownStenches(i);
					break;
				}
			}
			if (!wumpi.isEmpty()) {
				logger.debug("Hunting unclear wumpi at " + wumpi.get(0));
				return parseActionPath(wumpi.get(0), Action.Type.SHOOT);
			}
		}

	  	//	If there is a known exit -> move to it and leave the game
		Optional<Coordinate> exit = a.findClosestCoordinateWithGroundState(GroundState.EXIT);
		if (exit.isPresent()) {
			parseActionPath(exit.get(), null);
			logger.debug("Taking exit at " + exit.get());
			actionQueue.add(new Action(Action.Type.LEAVE, actionQueue.getLast().getCoordinate()));
			return true;
		}

	  	//	If nothing else works -> move randomly to valid coordinate YOLO
		Coordinate validRandomCoordinate;
		do {
			validRandomCoordinate = a.getPosition().copy().add(Direction.getRandom());
		} while (!validRandomCoordinate.isValid());
		logger.debug("Moving randomly to " + validRandomCoordinate);
		actionQueue.add(new Action(Action.Type.MOVE, validRandomCoordinate));
		return true;
	}

	@Override
	public void start() {
		while(a.isAlive()) {
			// Wait until a new message arrives. This makes sure that no consecutive
			// ActionTokens or MonitorResponses arrive
			Message m = a.waitForMessageAndGuaranteeOrder();
			logger.info("---- " + m.getClass() + " arrived! ----");

			// Read the message and write the content to my own state
			a.setStateFromMessage(m);

			if (ActionToken.class.equals(m.getClass())) {
				a.sendAgentStateRequests();
				computePathIfNeeded();
				for (Action a : actionQueue)
					logger.debug("\t\t Action: " + a);
				// Warning explanation: if action queue is empty then the agent should crash anyways
				a.takeAction(actionQueue.pollFirst());
			} else if (MonitorResponse.class.equals(m.getClass())) {
				a.sleep(0);
			} else if (AgentStateRequest.class.equals(m.getClass())) {
				a.replyToAgentStateRequest(m.getSenderURL());
			} else if (AgentStateResponse.class.equals(m.getClass())) {
			    a.parseAgentStateResponse((AgentStateResponse) m);
			}
		}
	}
}