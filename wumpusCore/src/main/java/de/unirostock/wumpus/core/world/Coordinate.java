package de.unirostock.wumpus.core.world;

import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Coordinate {

	private int x;
	private int y;
	
	Coordinate() {
		this(0, 0);
	};

	Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
	};

	Coordinate(Coordinate coordinate) {
		this.x = coordinate.x;
		this.y = coordinate.y;
	}

	/**
	 * @param coordinate	coordinate to be copied
	 * @return	copied coordinate if not null, otherwise null
	 */
	public static Coordinate from(Coordinate coordinate) {
		return coordinate == null ? null : coordinate.copy();
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public List<Coordinate> adjacentValid() {
		return adjacent().stream().filter(Coordinate::isValid).collect(Collectors.toList());
	}

	/**
	 * Returns list of 4 adjacent coordinates which have been shuffled
	 * to reduce bias towards going in the same direction in agents
	 *
	 * @return shuffled list of adjacent coordinates
	 */
	public List<Coordinate> adjacent() {
		List<Coordinate> arr = Arrays.asList(
				this.copy().add(Direction.NORTH), this.copy().add(Direction.SOUTH),
				this.copy().add(Direction.EAST), this.copy().add(Direction.WEST)
		);
		Collections.shuffle(arr);
		return arr;
	}

	/**
	 * @param other 	Coordinate to compare distance to
	 * @return			manhattan distance between this and other coordinate
	 */
	public int distance(Coordinate other) {
		return Math.abs(x - other.x) + Math.abs(y - other.y);
	}

	/**
	 * @return Manhattan distance from 0, 0
	 */
	public int distance() {
		return x + y;
	}

	public Coordinate copy() {
		return new Coordinate(this);
	}

	/**
	 * @param other	coordinate to add to this one
	 * @return		return this modified coordinate
	 */
	public Coordinate add(Coordinate other) {
		this.x += other.x;
		this.y += other.y;
		return this;
	}

	public Coordinate subtract(Coordinate other) {
		return this.add(other.copy().negate());
	}

	/**
	 * @param direction	direction to add this coordinate
	 * @return		    return this modified coordinate
	 */
	public Coordinate add(Direction direction) {
		return this.add(direction.asCoordinate());
	}

	public Coordinate subtract(Direction direction) {
		return this.add(direction.getOpposite());
	}

	/**
	 * @return negated self
	 */
	public Coordinate negate() {
		this.x = -this.x;
		this.y = -this.y;
		return this;
	}

	@Override
	public String toString() {
		return String.format("[%d,%d]", this.x, this.y);
	}

	@JsonIgnore // not necessary to serialize
	public boolean isValid() {
		return 0 <= this.x && this.x < WorldCreator.X_DIM
			&& 0 <= this.y && this.y < WorldCreator.Y_DIM;
	}

	public void validate() {
		this.x = Math.max(0, Math.min(this.x, WorldCreator.X_DIM));
		this.y = Math.max(0, Math.min(this.y, WorldCreator.Y_DIM));
	}

	public Optional<Direction> asDirection() {
		for (Direction direction : Direction.values())
			if (direction.asCoordinate().equals(this))
				return Optional.of(direction);
		return Optional.empty();
	}
	
	@JsonIgnore
	public boolean isCoordinateInRadius(int radius, Coordinate coord) {
		return Math.sqrt((y - coord.y) * (y - coord.y) + (x - coord.x) * (x - coord.x)) < radius;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Coordinate that = (Coordinate) o;
		return x == that.x && y == that.y;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}
}