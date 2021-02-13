package de.unirostock.wumpus.core.world;

import de.unirostock.wumpus.core.Util;

public enum Direction {
    NORTH(0, -1),
    SOUTH(0, 1),
    EAST(1, 0),
    WEST(-1, 0);

    private final Coordinate coordinate;

    Direction(int x, int y) {
        coordinate = new Coordinate(x, y);
    }

    public Direction getOpposite() {
        switch (this) {
            case NORTH:
                return SOUTH;
            case SOUTH:
                return NORTH;
            case EAST:
                return WEST;
            case WEST:
                return EAST;
        }
        return this;
    }

    public Coordinate asCoordinate() {
        return coordinate;
    }

    public static Direction getRandom() {
        return Direction.values()[Util.getRandomInteger(0, Direction.values().length)];
    }
}
