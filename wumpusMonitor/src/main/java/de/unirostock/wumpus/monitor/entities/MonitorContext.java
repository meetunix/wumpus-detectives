package de.unirostock.wumpus.monitor.entities;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.unirostock.wumpus.core.entities.Context;
import de.unirostock.wumpus.core.messageQueue.MessageQueue;
import de.unirostock.wumpus.core.world.MonitorWorldState;

public class MonitorContext implements Context {

	public boolean 				simulationSuccessful = false;
	
	private MonitorWorldState	monitorState;
	private MessageQueue		acceptorQueue; 
	private String				name;
	private URI					localUrl;
	private long				currentActionCount;
	private int					secondsSubscriptionPhase;
	private long				throttleMillis;
	private int					communicationRadius;
	private int					amountInitialAgents;
	private int					earnedReward;
	private int					fieldWidth;
	private int					numberWumpi;
	private int					numberPits;
	private int					numberGold;
	private int					numberRocks;
	private int					numberExits;
	
	private Map<URI,String> 	registeredAgents;
	
	public MonitorContext(URI localUrl, MessageQueue acceptorQueue) {
		
		this.name = "monitor";
		this.localUrl = localUrl;
		this.acceptorQueue = acceptorQueue;
		this.registeredAgents = new HashMap<>();
		this.currentActionCount = 0;
		this.earnedReward = 0;
	}

	public void incrementActionCount() {
		currentActionCount++;
	}
	
	public long getCurrentActionCount() {
		return currentActionCount;
	}
	
	public synchronized void addAgent(URI agentURI, String agentName) {
		this.registeredAgents.put(agentURI, agentName);
	}

	public synchronized void removeAgent(URI agentURI) {
		this.registeredAgents.remove(agentURI);
	}
	
	public String getAgentName(URI agentURI) {
		return registeredAgents.get(agentURI);
	}
	
	public Optional<URI> findAgentURIbyName(String name) {
		URI uri = null;
		for (URI key: registeredAgents.keySet()) {
			if ((registeredAgents.get(key)).equalsIgnoreCase(name)) {
				uri = key;
				break;
			}
		}
		return Optional.ofNullable(uri);
	}

	public MonitorWorldState getMonitorState() {
		return monitorState;
	}
	
	
	public void setMonitorState(MonitorWorldState monitorState) {
		this.monitorState = monitorState;
	}

	@Override
	public MessageQueue getAcceptorQueue() {
		return acceptorQueue;
	}

	public URI getLocalUrl() {
		return localUrl;
	}
	
	public URI getMonitorUrl() {
		return localUrl;
	}

	@Override
	public String getName() {
		return name;
	}

	public Map<URI, String> getRegisteredAgents() {
		return registeredAgents;
	}

	public int getSecondsSubscriptionPhase() {
		return secondsSubscriptionPhase;
	}

	public void setSecondsSubscriptionPhase(int secondsSubscriptionPhase) {
		this.secondsSubscriptionPhase = secondsSubscriptionPhase;
	}

	public long getThrottleMillis() {
		return throttleMillis;
	}

	public void setThrottleMillis(long throttleMillis) {
		this.throttleMillis = throttleMillis;
	}

	public int getCommunicationRadius() {
		return communicationRadius;
	}

	public void setCommunicationRadius(int communicationRadius) {
		this.communicationRadius = communicationRadius;
	}

	public int getAmountInitialAgents() {
		return amountInitialAgents;
	}

	public void setAmountInitialAgents(int amountInitialAgents) {
		this.amountInitialAgents = amountInitialAgents;
	}
	
	public void addToEarnedReward(int reward) {
		this.earnedReward += reward;
	}

	public int getEarnedReward() {
		return earnedReward;
	}

	public int getFieldWidth() {
		return fieldWidth;
	}

	public void setFieldWidth(int fieldWidth) {
		this.fieldWidth = fieldWidth;
	}

	public int getNumberWumpi() {
		return numberWumpi;
	}

	public void setNumberWumpi(int numberWumpi) {
		this.numberWumpi = numberWumpi;
	}

	public int getNumberPits() {
		return numberPits;
	}

	public void setNumberPits(int numberPits) {
		this.numberPits = numberPits;
	}

	public int getNumberGold() {
		return numberGold;
	}

	public void setNumberGold(int numberGold) {
		this.numberGold = numberGold;
	}

	public int getNumberRocks() {
		return numberRocks;
	}

	public void setNumberRocks(int numberRocks) {
		this.numberRocks = numberRocks;
	}

	public int getNumberExits() {
		return numberExits;
	}

	public void setNumberExits(int numberExits) {
		this.numberExits = numberExits;
	}
}