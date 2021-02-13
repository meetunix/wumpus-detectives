package de.unirostock.wumpus.core.messageQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.unirostock.wumpus.core.messages.Message;


public class MessageQueueImpl implements MessageQueue {
	
    private static Logger logger = LogManager.getLogger(MessageQueueImpl.class);
    
    private static final int QUEUE_SIZE = 1024;

	BlockingQueue<Message> queue = new LinkedBlockingQueue<>(QUEUE_SIZE);

	@Override
	public Message take() {
		
		Message message = null;
		
		try {

			message = queue.take();

		} catch (InterruptedException e) {
			logger.error("Exception while TAKE action on queue: {}", e.toString());
		}
		
		return message;
	}

	@Override
	public void put(Message message) {
		
		try {

			queue.put(message);

		} catch (InterruptedException e) {
			logger.error("Exception while PUT action on queue: {}", e.toString());
		}
	}

	@Override
	public Message poll() {
		return queue.poll();
	}

	@Override
	public Message poll(long timeout) {
		try {
			return queue.poll(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.error("Exception while POLL action on queue: {}", e.toString());
		}
		return null;
	}

	@Override
	public boolean hasMessage() {
		return queue.size() >= 1 ? true : false;
	}

	@Override
	public List<Message> getAllMessages() {

		List<Message> list = new ArrayList<>(queue.size());
		queue.drainTo(list);
		return list;
	}

	@Override
	public int size() {
		return queue.size();
	}
}
