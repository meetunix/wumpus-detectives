package de.unirostock.wumpus.core.world;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Field {

	private Map<URI,String> otherAgents; // will be initiated when it is needed
	private List<GroundState> groundStates;
	
	public Field() {};
	
	public Field(GroundState initialGroundState) {
		this.groundStates = new ArrayList<>(3);
		addGroundState(initialGroundState);
		
	};
	
	public Field(List<GroundState> initialGroundStates) {
		setGroundState(initialGroundStates);
	};

	/**
	 * Returns the amount of other agents at this field. 
	 * @return int
	 */
	public int agentCount() {

		return otherAgents != null && otherAgents.size() > 0 ? otherAgents.size() : 0;
	}
	
	/**
	 * @return true if there are other agents at this field, false if there aren't agents
	 */
	public boolean hasOtherAgents() {
		return otherAgents != null && otherAgents.size() > 0;
	}

	/**
	 * Adds an agent (his URL) to the field
	 * @param agentURL
	 */
	public void addAgent(URI agentURL, String agentName) {

		if (otherAgents == null)
			otherAgents = new HashMap<>();
		
		otherAgents.put(agentURL, agentName);
	}

	/**
	 * Get the corresponding name of an agent for an URI.
	 * 
	 * @param agentURI
	 * @return agentName
	 */
	public String getAgentName(URI agentURI) {
		return otherAgents.get(agentURI);
	}
	
	/**
	 * @return a collection with all agent names on this field
	 */
	@JsonIgnore // needs to be ignored while serialization - nothing bad
	public Collection<String> getAgentNames() {
		return otherAgents.values();
	}
	
	/**
	 * Returns the amount of groundStates in this field. 
	 * 
	 * @return int
	 */
	public int groundStateCount() {
		return groundStates.size();
	}

	/**
	 * Adds an GroundState to the field if it' not already present
	 * 
	 * @param agentURL
	 */
	public void addGroundState(GroundState groundState) {
		if (!groundStates.contains(groundState))
			groundStates.add(groundState);
	}
	
	public boolean removeGroundState(GroundState gs) {
		return groundStates.remove(gs);
	}
	
	// getter and setter
	
	public Map<URI,String> getOtherAgents() {
		return otherAgents;
	}

	public void setOtherAgents(Map<URI,String> otherAgents) {
		this.otherAgents = otherAgents;
	}


	public List<GroundState> getGroundStates() {
		return groundStates;
	}

	public void setGroundState(List<GroundState> groundState) {
		this.groundStates = groundState;
	}
	
	@JsonIgnore // not necessary to serialize
	public boolean isFree() {
		return groundStates.contains(GroundState.FREE) && groundStates.size() == 1;
	}

	@JsonIgnore // not necessary to serialize
	public boolean isUnknown() {
		return groundStates.contains(GroundState.UNKNOWN) && groundStates.size() == 1;
	}

	public boolean hasGroundState(GroundState groundState) {
		return groundStates.contains(groundState);
	}
	
	public void removeAgent(URI agentURI) {
		otherAgents.remove(agentURI);
	}

	public boolean containsAgent(URI agentURI) {
		return otherAgents != null && otherAgents.containsKey(agentURI);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("State(s): ");
		groundStates.forEach(s -> sb.append(s + " "));
		sb.append("\n");
		return sb.toString();
	}

	@JsonIgnore // do not serialize
	public boolean isInaccessible() {
		return groundStates.contains(GroundState.ROCK);
	}
}