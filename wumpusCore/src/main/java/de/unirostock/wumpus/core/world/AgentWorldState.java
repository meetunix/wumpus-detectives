package de.unirostock.wumpus.core.world;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Saves the world state for a client 
 */
public class AgentWorldState extends WorldState {

	private static final Logger logger = LogManager.getLogger(AgentWorldState.class);
	private Coordinate currentPosition;
	
	public AgentWorldState() {};

	public AgentWorldState(Field[][] world) {
		super(world);
	}

	public Coordinate getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(Coordinate currentPosition) {
		this.currentPosition = currentPosition;
	};
	public void merge(AgentWorldState otherState) {
	    merge(otherState, new HashSet<>());
    }

	public void merge(AgentWorldState otherState, HashSet<Coordinate> ignoreCoordinates) {
	    Field[][] currWorld = getWorld();
		Field[][] otherWorld = otherState.getWorld();
		for(int y = 0; y < WorldCreator.Y_DIM; y++) {
			for(int x = 0; x < WorldCreator.X_DIM ; x++) {
			    if (ignoreCoordinates.contains(new Coordinate(x, y)))
			    	continue;
				Field currField = currWorld[y][x];
				Field otherField = otherWorld[y][x];
				List<GroundState> currGS = currField.getGroundStates();
				List<GroundState> otherGS = otherField.getGroundStates();
				// If the other agent says it's free we can guarantee that it's the best outcome
				boolean shouldPrint = !currField.isUnknown()
						&& !otherField.isUnknown()
						&& !(currGS.containsAll(otherGS) && otherGS.containsAll(currGS));
				if (shouldPrint)
					logger.debug(currGS + " merging with " + otherGS + " at " + new Coordinate(x, y));
				if (otherField.isFree()) {
					currField.getGroundStates().clear();
					currField.addGroundState(GroundState.FREE);
				}
				else if (!otherField.isUnknown()) {
				    if (currField.isUnknown()) {
				    	currField.removeGroundState(GroundState.UNKNOWN);
						currGS.addAll(otherGS);
					} else {
				    	currGS.removeIf(gs -> !otherGS.contains(gs));
				    	if (currGS.isEmpty())
				    		currGS.add(GroundState.FREE);
					}
				}
				if (shouldPrint)
					logger.debug(" => result: " + currGS);
			}
		}
	}
}