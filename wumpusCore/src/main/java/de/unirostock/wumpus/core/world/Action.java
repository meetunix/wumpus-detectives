package de.unirostock.wumpus.core.world;

public class Action {
	
    public enum Type {
        MOVE, LEAVE, SHOOT
    }

    private final Type type;
    private final Coordinate coordinate;

    public Action() {
        type = null;
        coordinate = null;
    };

    public Action(Type actionType, Coordinate coordinate) {
        this.coordinate = coordinate;
        this.type = actionType;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Action{" + "type=" + type + ", coordinate=" + coordinate + '}';
    }
}
