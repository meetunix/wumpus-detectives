package de.unirostock.wumpus.core.messages;

import java.net.URI;

import de.unirostock.wumpus.core.entities.Context;
import de.unirostock.wumpus.core.world.Coordinate;
import de.unirostock.wumpus.core.world.Field;

public class MonitorResponse extends Message {
	
	Coordinate 	currentPosition;
	Field 		status;
	boolean		killAgent;
	int			reward; 

	public MonitorResponse() {}

	public MonitorResponse(Context context, URI recipient) {
		super(context, recipient);
	}


	public Coordinate getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(Coordinate currentPosition) {
		this.currentPosition = currentPosition;
	}

	public Field getStatus() {
		return status;
	}

	public void setStatus(Field status) {
		this.status = status;
	}

	public boolean isKillAgent() {
		return killAgent;
	}

	public void setKillAgent(boolean killAgent) {
		this.killAgent = killAgent;
	}
	
	public void killAgent() {
		this.setReward(-1000);
		this.setKillAgent(true);
	}

	public int getReward() {
		return reward;
	}

	public void setReward(int reward) {
		this.reward = reward;
	}
}