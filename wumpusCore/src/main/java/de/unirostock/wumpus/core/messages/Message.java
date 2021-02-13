package de.unirostock.wumpus.core.messages;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import de.unirostock.wumpus.core.Util;
import de.unirostock.wumpus.core.entities.Context;

@JsonTypeInfo(
		  use = JsonTypeInfo.Id.NAME, 
		  include = JsonTypeInfo.As.PROPERTY, 
		  property = "type")
@JsonSubTypes({
	@Type(value = CurrentStateMessage.class, name = "CurrentStateMessage"),
	@Type(value = SubscriptionRequest.class, name = "SubscriptionRequest"),
	@Type(value = SubscriptionResponse.class, name = "SubscriptionResponse"),
	@Type(value = UnsubscribeRequest.class, name = "UnsubscribeRequest"),
	@Type(value = ActionToken.class, name = "ActionToken"),
	@Type(value = MonitorInformation.class, name = "MonitorInformation"),
	@Type(value = MonitorResponse.class, name = "MonitorResponse"),
	@Type(value = AgentStateRequest.class, name = "AgentStateResquest"),
	@Type(value = AgentStateResponse.class, name = "AgentStateResponse")
	})
public class Message {
	
	long messageID; // random message id for identifying answer
	URI senderURL;
	URI recipientURL;
	String senderName;
	
	public Message() {};
	
	public Message(Context context, URI recipient) {
		this.messageID = Util.getRandomLong(100000000, 999999999);
		this.senderName = context.getName();
		this.senderURL = context.getLocalUrl();
		this.recipientURL = recipient;
	}
	
	/**
	 * This constructor can be used to simply generate an answer
	 * 
	 * @param question	the Message to answer to
	 * @param context	the Context object
	 */
	public Message(Message question, Context context) {
		this.senderURL = question.getRecipientURL();
		this.recipientURL = question.getSenderURL();
		this.messageID = question.getMessageID();
		this.senderName = context.getName();
	}

	public long getMessageID() {
		return messageID;
	}
	
	public void setMessageID(long messageID) {
		this.messageID = messageID;
	}

	public URI getSenderURL() {
		return senderURL;
	}

	public void setSenderURL(URI senderURL) {
		this.senderURL = senderURL;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}
	
	public URI getRecipientURL() {
		return recipientURL;
	}

	public void setRecipientURL(URI recipientURL) {
		this.recipientURL = recipientURL;
	}

	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("\n%-22s %-20d\n","messageID:" , messageID));
		sb.append(String.format("%-22s %-20s\n","senderURL:", senderURL));
		sb.append(String.format("%-22s %-20s\n","recipientURL:", recipientURL));
		sb.append(String.format("%-22s %-20s\n","name:", senderName));
		sb.append("\n");
		
		return sb.toString();
	}

}
