package de.unirostock.wumpus.core.messages;

import de.unirostock.wumpus.core.entities.Context;

public class UnsubscribeRequest extends Message {
	
	
	public  UnsubscribeRequest() {};
	
	public UnsubscribeRequest(Context context) {
		super(context, context.getMonitorUrl());
	}

}
