package de.unirostock.wumpus.core.messages;

import java.net.URI;

import de.unirostock.wumpus.core.entities.Context;

public class SubscriptionResponse extends Message {
	
	public SubscriptionResponse() {};

	public SubscriptionResponse(Context context, URI recipientURI) {
		super(context, recipientURI);
	}
}
