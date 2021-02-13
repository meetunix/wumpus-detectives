package de.unirostock.wumpus.core.messages;

import java.net.URI;

import de.unirostock.wumpus.core.entities.Context;

public class AgentStateRequest extends Message {

	public AgentStateRequest() {}

	public AgentStateRequest(Context context, URI recipient) {
		super(context, recipient);
	}

	public AgentStateRequest(Message question, Context context) {
		super(question, context);
	}

}
