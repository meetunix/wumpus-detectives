package de.unirostock.wumpus.core.messages;

import java.net.URI;

import de.unirostock.wumpus.core.entities.Context;
import de.unirostock.wumpus.core.world.AgentWorldState;

public class AgentStateResponse extends Message {
	
	AgentWorldState state;

	public AgentStateResponse() {}

	public AgentStateResponse(Context context, URI recipient, AgentWorldState state) {
		super(context, recipient);
		this.state = state;
	}

	public AgentStateResponse(Message question, Context context) {
		super(question, context);
	}
	
	public void setState(AgentWorldState state) {
		this.state = state;
	}

	public AgentWorldState getState() {
		return state;
	}
}
