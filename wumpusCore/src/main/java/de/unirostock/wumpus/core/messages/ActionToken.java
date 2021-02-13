package de.unirostock.wumpus.core.messages;

import java.net.URI;
import java.util.Map;

import de.unirostock.wumpus.core.entities.Context;
import de.unirostock.wumpus.core.world.Coordinate;
import de.unirostock.wumpus.core.world.Field;

public class ActionToken extends Message {

	Map<URI,String> agentsInRadius;
	Coordinate currentPosition;
	Field stateAtCurrentPosition;

	public ActionToken() {}

	public ActionToken(Context context, URI recipient) {
		super(context, recipient);
	}

	public Map<URI, String> getAgentsInRadius() {
		return agentsInRadius;
	};
	
	public void setAgentsInRadius(Map<URI, String> agentsInRadius) {
		this.agentsInRadius = agentsInRadius;
	}
	
	public Coordinate getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(Coordinate currentPosition) {
		this.currentPosition = currentPosition;
	}

	public Field getStateAtCurrentPosition() {
		return stateAtCurrentPosition;
	}

	public void setStateAtCurrentPosition(Field stateAtCurrentPosition) {
		this.stateAtCurrentPosition = stateAtCurrentPosition;
	}

	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("\n%-22s %-20d\n","messageID:" , super.messageID));
		sb.append(String.format("%-22s %-20s\n","senderURL:", super.senderURL));
		sb.append(String.format("%-22s %-20s\n","recipientURL:", super.recipientURL));
		sb.append(String.format("%-22s %-20s\n","name:", super.senderName));
		sb.append(String.format("%-22s %-20s\n","agentsInRadius:", agentsInRadius));
		sb.append("\n");
		
		return sb.toString();
	}
}