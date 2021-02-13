package de.unirostock.wumpus.core;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Util {
	
	
    private static Logger logger = LogManager.getLogger(Util.class);

	public static URI validateURI(String urlString) {

		URI uri = null;
			
		try {

			uri = new URI(urlString);

		} catch (URISyntaxException e) {
			logger.error("malformed URI entered: {}", uri);
			System.exit(-1);
		}
			
		return uri;
	}

	/**
	 * Returns a random long (n) within [x,z].
	 * 
	 * @param x; long
	 * @param y long
	 * @return long with x <= n < z
	 */
	public static long getRandomLong (long x, long y) {
		return x + (long) (Math.random() * (y - x));
	}
	
	/**
	 * Returns a random positive long
	 * 
	 * @return a long where 1 <= x <= (2^63 - 1)
	 */
	public static long getRandomLong () {
		return 1 + (long) (Math.random() * ( Long.MAX_VALUE ));
	}

	/**
	 * Returns a random int (n) within a given range [x,y[
	 * 
	 * @param x int min value
	 * @param y int max value
	 * @return int with x <= n < y
	 */
	public static int getRandomInteger(int x, int y) {
		return x + (int) (new Random().nextFloat() * (y - x));
	}
	
	
	/**
	 * Returns a random positive integer.
	 * 
	 * @return a int where 1 <= x <= (2^31 - 1)
	 */
	public static int getRandomInteger () {
		return 1 + (int) (new Random().nextFloat() * (Integer.MAX_VALUE));
	}
}
