package de.unirostock.wumpus.core.world;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		  use = JsonTypeInfo.Id.NAME, 
		  include = JsonTypeInfo.As.PROPERTY, 
		  property = "type")
@JsonSubTypes({
	@Type(value = AgentWorldState.class, name = "AgentWorldState"),
	@Type(value = MonitorWorldState.class, name = "MonitorWorldState")
})
public class WorldState {
	
	private long version = 0;
	private Field[][] world;

	public WorldState() {};
	
	public WorldState(Field[][] world) {
		this.world = world;
	}
	

	public Field[][] getWorld() {
		return world;
	}

	public void setWorld(Field[][] world) {
		this.world = world;
	}
	
	public Field getFieldOnPosition(Coordinate position) {
		return world[position.getY()][position.getX()];
	}

	public void setFieldOnPosition(Field field, Coordinate position) {
		world[position.getY()][position.getX()] = field;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}
	
	public void incrementVersion() {
		version++;
	}

}
