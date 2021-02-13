package de.unirostock.wumpus.core.messages;

import java.net.URI;

import de.unirostock.wumpus.core.entities.Context;
import de.unirostock.wumpus.core.world.Field;
import de.unirostock.wumpus.core.world.WorldCreator;
import de.unirostock.wumpus.core.world.WorldState;

public class CurrentStateMessage extends Message {
	
	WorldState myWorldState;
	
	public CurrentStateMessage() {};
	
	public CurrentStateMessage(Context context, URI recipient, WorldState worldState) {
		super(context, recipient);
		setMyWorldState(worldState);
	}

	/**
	 * This constructor can be used to simply generate an answer
	 * 
	 * @param question	the Message to answer to
	 * @param context	the Context object
	 */
	public CurrentStateMessage(Message question, Context context) {
		super(question,context);
	}

	public WorldState getMyWorldState() {
		return myWorldState;
	}

	public void setMyWorldState(WorldState myWorldState) {
		this.myWorldState = myWorldState;
	}
	
	private String printWorldState(WorldState worldState) {
		Field[][] world = worldState.getWorld();
		
		StringBuilder sb = new StringBuilder();
		for(int x = 0 ; x < WorldCreator.X_DIM ; x++) {
			sb.append("\n[");
			for(int y = 0; y < WorldCreator.Y_DIM; y++) {
				sb.append(world[x][y]);
				sb.append(" ");
			}
			sb.append("]");
		}
		return sb.toString();
	}
	
	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("\n%-22s %-20d\n","messageID:" , super.getMessageID()));
		sb.append(String.format("%-22s %-20s\n","senderURL:", super.getSenderURL()));
		sb.append(String.format("%-22s %-20s\n","recipientURL:", super.getRecipientURL()));
		sb.append(String.format("%-22s %-20s\n","version:", super.getSenderName()));
		sb.append(String.format("\n%s", printWorldState(myWorldState)));
		sb.append("\n");
		
		return sb.toString();
	}
}