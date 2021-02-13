package de.unirostock.wumpus.core.entities;

import java.net.URI;

import de.unirostock.wumpus.core.messageQueue.MessageQueue;

public interface Context {
	
	public MessageQueue getAcceptorQueue();
	public URI getLocalUrl();
	public URI getMonitorUrl();
	public String getName();
}
