package de.unirostock.wumpus.core.messages;

import de.unirostock.wumpus.core.entities.Context;

public class SubscriptionRequest extends Message {
	
	public  SubscriptionRequest() {};
	
	public SubscriptionRequest(Context context) {
		super(context, context.getMonitorUrl());
	}

}
