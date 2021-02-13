package de.unirostock.wumpus.core.communicator;

import java.net.URI;

import de.unirostock.wumpus.core.messages.Message;

/*
 * The communicator sends messages to other agents. Because all agents are able to receive
 * messages asynchronously, the sending will be implemented synchronously. Means an agent
 * can only send one message at a time.
 */

public interface Communicator {
	
	/**
	 * Sends a message to agentURL in an synchronously manner. If timeout is reached the
	 * sending is aborted.  
	 * 
	 * @param agentURL - the message url
	 * @param message - the message to send
	 * @param timeout - timeout im milliseconds
	 * @return true if message was successfully sent
	 */
	public boolean sendMessage(URI agentURL, Message message, int timeout);
	
}
