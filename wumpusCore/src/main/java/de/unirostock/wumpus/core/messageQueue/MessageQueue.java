package de.unirostock.wumpus.core.messageQueue;

import java.util.List;

import de.unirostock.wumpus.core.messages.Message;


/*
 * Because a lot of agents are able to send messages at the same time
 * the receiving of messages must be implemented asynchronous. Therefore
 * this class abstracts a blocking input queue for concurrent access.
 * 
 */

public interface MessageQueue {
	
	/**
	 * Takes a message out of the queue.
	 * 
	 * @return
	 */
	Message take();

	/**
	 * Takes a message out of the queue, if there is no message return null.
	 * 
	 * @return A Message Object or null
	 */
	Message poll();

	/**
	 * Tries to take a message from the queue, but only wait for timeout in milliseconds.
	 * 
	 * @param timeout long milliseconds to wait
	 * @return A Message Object or null
	 */
	Message poll(long timeout);
	
	/**
	 * Check if a minimum of one Message is inside the Queue.
	 * 
	 * @return true is >= 1 Message is inside Queue, otherwise false
	 */
	boolean hasMessage();
	
	/**
	 * Puts a message in the queue.
	 * 
	 * @param message
	 */
	void put(Message message);
	
	/**
	 * 
	 * Return all currently stored Messages from the queue;
	 * 
	 * @return An ArrayList of Messages
	 */
	List<Message> getAllMessages();

	/**
	 * Return number of messages in queue without draining
	 *
	 * @return size of queue as int
	 */
	int size();
}
