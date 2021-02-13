package de.unirostock.wumpus.detective.entities;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import de.unirostock.wumpus.core.entities.Context;
import de.unirostock.wumpus.core.messageQueue.MessageQueue;

/*
 * encapsulate the whole meta context of the agent
 * 
 */
public class AgentContext implements Context {
	
	private final String name;
	private final URI localUrl;
	private final URI monitorUrl;
	private final MessageQueue acceptorQueue;
	private Map<URI, String> otherAgents;
	
	// how many action token the agent has received
	private long  currentActionCount; 
	
	private AgentContext (String name, URI localURL, URI monitorURL, MessageQueue acceptorQueue) {
		this.name = name;
		this.localUrl = localURL;
		this.monitorUrl = monitorURL;
		this.acceptorQueue = acceptorQueue;
		this.currentActionCount = 0;
		this.otherAgents = new HashMap<>(8);
	}
	
	public String getName() {
		return name;
	}

	public URI getLocalUrl() {
		return localUrl;
	}

	@Override
	public MessageQueue getAcceptorQueue() {
		return acceptorQueue;
	}
	public URI getMonitorUrl() {
		return monitorUrl;
	}
	
	public void incrementActionCount() {
		currentActionCount++;
	}

	public long getCurrentActionCount() {
		return currentActionCount;
	}
	
	public synchronized void addOtherAgent(URI agentURI, String name) {
		otherAgents.put(agentURI, name);
	}

	public Map<URI, String> getOtherAgents() {
		return otherAgents;
	}

	public void setOtherAgents(Map<URI, String> otherAgents) {
		this.otherAgents = otherAgents;
	}



	public static class Builder {

		private  String name;
		private  URI localUrl;
		private  URI monitorUrl;
		private  MessageQueue acceptorQueue;
		
		public Builder() {
			
		}
		
		public Builder name(String name) {
			this.name = name;
			return this;
		}
		
		public Builder localURL(URI localURL) {
			this.localUrl = localURL;
			return this;
		}

		public Builder monitorURL(URI monitorURL) {
			this.monitorUrl = monitorURL;
			return this;
		}
		
		public Builder acceptorQueue(MessageQueue acceptorQueue) {
			this.acceptorQueue = acceptorQueue;
			return this;
		}
		
		public AgentContext build() {
			return new AgentContext(name, localUrl, monitorUrl, acceptorQueue);
		}
	}

}
