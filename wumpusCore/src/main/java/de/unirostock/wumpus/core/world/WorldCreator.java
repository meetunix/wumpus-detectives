package de.unirostock.wumpus.core.world;

import java.net.URI;
import java.util.*;

import de.unirostock.wumpus.core.Util;

public class WorldCreator {

	public static int Y_DIM;
	public static int X_DIM;
	private static String stateToColor(List<GroundState> gs) {
		if (gs.contains(GroundState.ROCK))
			return "\033[1;37mR";
		else if (gs.contains(GroundState.WUMPUS))
			return "\033[35mW";
		else if (gs.contains(GroundState.PIT))
			return "\033[34mP";
		else if (gs.contains(GroundState.EXIT))
			return "\033[1;31mO";
		else if (gs.contains(GroundState.GOLD))
			return "\033[1;33mG";
		else if (gs.contains(GroundState.BREEZE) && gs.contains(GroundState.STENCH))
			return "\033[36m≈";
		else if (gs.contains(GroundState.BREEZE))
			return "\033[34m~";
		else if (gs.contains(GroundState.STENCH))
			return "\033[35m~";
		else if (gs.contains(GroundState.FREE))
			return " ";
		else if (gs.contains(GroundState.UNKNOWN))
			return "?";
		else
			return "X";
	}

	private static final EnumMap<GroundState, Integer> groundStateOccurrences = new EnumMap<>(GroundState.class);

	private static void createGroundStateOccurrences(List<Integer> config){
		groundStateOccurrences.put(GroundState.PIT, config.get(2));
		groundStateOccurrences.put(GroundState.GOLD, config.get(3));
		groundStateOccurrences.put(GroundState.WUMPUS, config.get(1));
		groundStateOccurrences.put(GroundState.EXIT, config.get(5));
		groundStateOccurrences.put(GroundState.ROCK, config.get(4));
	}

	public static Field[][] getEmptyWorld(GroundState withType) {
		Field[][] world = new Field[Y_DIM][X_DIM];
		for(int y = 0; y < Y_DIM; y++) {
			for(int x = 0 ; x < X_DIM ; x++) {
				world[y][x] = new Field(withType);
			}
		}
		return world;
	}
	
	public static Field[][] getWorld(Map<URI,String> registeredAgents, List<Integer> config) {
		Field[][] world = getBasicWorld(config);
		// place agents
		for (URI agentURI : registeredAgents.keySet()) {
		    Coordinate c = getRandomEmptyCoordinate(world);
			world[c.getY()][c.getX()].addAgent(agentURI, registeredAgents.get(agentURI));
		}

		return world;
	}

	public static String printWorldState(WorldState worldState) {
		Field[][] world = worldState.getWorld();
		StringBuilder sb = new StringBuilder();
		String horizontalBorder = new String(new char[X_DIM]).replace("\0", "═");
		sb.append("╔").append(horizontalBorder).append("╗\n");
		for(int y = 0; y < WorldCreator.Y_DIM; y++) {
			sb.append("║");
			for(int x = 0 ; x < WorldCreator.X_DIM ; x++) {
				if (world[y][x].hasOtherAgents()) {
					sb.append("\033[1;31m");
					sb.append(world[y][x].getAgentNames().toString().charAt(1));
				}
				else {
					sb.append(stateToColor(world[y][x].getGroundStates()));
				}
				sb.append("\033[0m");
			}
			sb.append("║\n");
		}
		sb.append("╚").append(horizontalBorder).append("╝\n");

		return sb.toString();
	}

	private static Coordinate getRandomCoordinate() {
		return new Coordinate(Util.getRandomInteger(0, X_DIM), Util.getRandomInteger(0, Y_DIM));
	}

	private static Coordinate getRandomEmptyCoordinate(Field[][] world) {
		Coordinate c;
		do {
			c = getRandomCoordinate();
		} while(!world[c.getY()][c.getX()].isFree());
		return c;
	}

	public static Field[][] getBasicWorld(List<Integer> config) {

		//Y_DIM = config.get(0);
		//X_DIM = (int)Math.round(Y_DIM * (16.0 / 9.0));
		//System.out.println(X_DIM + "x" + Y_DIM);
		createGroundStateOccurrences(config);

		Field[][] world = getEmptyWorld(GroundState.FREE);
		groundStateOccurrences.forEach((groundState, occ) -> {
			Coordinate xy;
			for (int i = 0; i < occ; i++) {
				xy = getRandomEmptyCoordinate(world);
				world[xy.getY()][xy.getX()].addGroundState(groundState);
				GroundState addAdjacent = null;
				switch (groundState) {
					case PIT:
						addAdjacent = GroundState.BREEZE;
						break;
					case WUMPUS:
						addAdjacent = GroundState.STENCH;
						break;
				}
				if (addAdjacent != null) {
					for (Coordinate adj : xy.adjacent()) {
						if (adj.isValid())
						    world[adj.getY()][adj.getX()].addGroundState(addAdjacent);
					}
				}
			}
		});
		return world;
	}

	public static void setyDim(int yDim) {
		Y_DIM = yDim;
	}

	public static void setxDim(int xDim) {
		X_DIM = xDim;
	}

}
