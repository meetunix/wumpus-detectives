package de.unirostock.wumpus.core.world;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.unirostock.wumpus.core.world.Action.Type;

/**
 * This class encapsulates the current world state not known by the agents. 
 */
public class MonitorWorldState extends WorldState {

    private static Logger logger = LogManager.getLogger(MonitorWorldState.class);
	
	Map<URI,Coordinate> agentCoordinates;
	Map<URI,Boolean> agentAlive;
	Map<URI,Type> lastAction;
	
	
    public MonitorWorldState() {};

    public MonitorWorldState(Field[][] world) {
    	super(world);
    	this.agentCoordinates = createAgentCoordinates();

    	// create a simple dict with alive states of the corresponding agent
    	this.agentAlive = agentCoordinates.keySet().stream()
    			.collect(Collectors.toMap(s -> s, s -> true));

    	// last action the agent has been executed
    	this.lastAction = agentCoordinates.keySet().stream()
    			.collect(Collectors.toMap(k -> k, k -> Type.MOVE));
    };
    
    /**
     * Builds a map with all agents and corresponding Coordinates.
     * This happends only once.
     * 
     * @param agentURI
     * @return Coordinate of the requested agent
     */
    private Map<URI,Coordinate> createAgentCoordinates(){

    	Map<URI,Coordinate> map = new HashMap<>();
    	Field[][] world = this.getWorld();
    	
		for(int y = 0; y < WorldCreator.Y_DIM; y++) {
			for(int x = 0; x < WorldCreator.X_DIM; x++) {
				if (world[y][x].hasOtherAgents())
					for(URI uri : world[y][x].getOtherAgents().keySet())
						map.put(uri, new Coordinate(x,y));
			}
		}
		return map;
    }
    
    /**
     * Returns all agents that are inside the radius as a map(URI->AgentsName). 
     * 
     * @param radius	int radius
     * @param myPos		Coordinate my coordinate
     * @param agents	Map<URI,String> URI->AgentsName
     * @return A map of agents inside the radius Map<URI,String>
     */
    
    public Map<URI,String> getAgentsInRadius(int radius, Coordinate myPos, Map<URI,String> agents) {

    	if (myPos == null) {
    		logger.fatal("Own coordinate is null, something went wrong.");
    		// TODO This happens extremely seldom, needs to handled in operator Thread
    		return null;
    		
    	} else {
    	
			return agentCoordinates.entrySet().stream()
					.filter(x -> x.getValue() != null)
					.filter(x -> x.getValue().isCoordinateInRadius(radius, myPos)) 
					.filter(p -> agents.containsKey(p.getKey())) // agent isn't registered anymore
					.filter(t -> ! t.getValue().equals(myPos)) // don't add myself
					.collect(Collectors.toMap(x -> x.getKey(), x -> agents.get(x.getKey())));
    	}
    }
    
    /**
     * Returns the coordinate of the passed agent URI.
     * 
     * @param agentURI
     * @return Coordinate of the requested agent
     */
    public Coordinate getAgentsCoordinates(URI agentURI){
		return agentCoordinates.get(agentURI);
    }

	public Map<URI, Coordinate> getAgentCoordinates() {
		return agentCoordinates;
	}

	public void setAgentCoordinates(Map<URI, Coordinate> agentCoordinates) {
		this.agentCoordinates = agentCoordinates;
	}

	public void removeAgent(URI agentURI) {
		Coordinate agentsCoord = this.getAgentsCoordinates(agentURI);
		Field agentsField = this.getFieldOnPosition(agentsCoord);
		agentsField.removeAgent(agentURI);
		agentCoordinates.remove(agentURI);
		agentAlive.put(agentURI, false);
	}
    
	public void updateAgent(URI agentURI, Coordinate newCoord) {
		
		//remove agent from old field
		Coordinate oldCoord = this.getAgentsCoordinates(agentURI);
		Field oldField = this.getFieldOnPosition(oldCoord);
		String agentName = oldField.getAgentName(agentURI);
		oldField.removeAgent(agentURI);
		
		// add agent to new field
		Field newField = this.getFieldOnPosition(newCoord);
		newField.addAgent(agentURI, agentName);
		
		// update local coordinate map
		agentCoordinates.put(agentURI, newCoord);
	}
	
	public String getAgentName(URI agentURI) {
		
		Coordinate coord = this.getAgentsCoordinates(agentURI);
		Field field = this.getFieldOnPosition(coord);
		
		return field.getAgentName(agentURI);
	}
	
	public boolean removeGroundState(GroundState gs, Coordinate coord) {

		Field field = this.getFieldOnPosition(coord);
		return field.removeGroundState(gs);
		
	}

	public Map<URI, Boolean> getAgentAlive() {
		return agentAlive;
	}

	public void setAgentAlive(Map<URI, Boolean> agentAlive) {
		this.agentAlive = agentAlive;
	}
	
	public void updateLastActionForAgent(URI agentURI, Action action) {
		this.lastAction.put(agentURI, action.getType());
	}

	public Map<URI, Type> getLastAction() {
		return lastAction;
	}

	public void setLastAction(Map<URI, Type> lastAction) {
		this.lastAction = lastAction;
	}
}