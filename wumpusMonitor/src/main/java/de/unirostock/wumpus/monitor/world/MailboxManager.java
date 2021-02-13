package de.unirostock.wumpus.monitor.world;

import java.net.URI;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.unirostock.wumpus.core.messageQueue.MessageQueue;
import de.unirostock.wumpus.core.messages.Message;

public class MailboxManager implements Runnable {
	
    private static Logger logger = LogManager.getLogger(MailboxManager.class);

	private MessageQueue acceptorQueue;
	private Map<URI,MessageQueue> mailboxMap;

	public MailboxManager(Map<URI,MessageQueue> mailboxMap, MessageQueue acceptorQueue) {
		this.mailboxMap = mailboxMap;
		this.acceptorQueue = acceptorQueue;
	}

	@Override
	public void run() {
		
		while(! Thread.currentThread().isInterrupted() ) {
			Message m = acceptorQueue.take();
			MessageQueue currentQueue = mailboxMap.get(m.getSenderURL());
			if (currentQueue != null)
				currentQueue.put(m);
			else
				logger.warn("received a message from an unsubscribed agent: {}", m.getSenderName());
		}
	}
}