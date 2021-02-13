package de.unirostock.wumpus.core.acceptor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.unirostock.wumpus.core.entities.Context;



/**
 * The Acceptor is the thread which listens to incoming messages. His job is to receive the
 * messages, decode them and put them into a message queue.
 */

public class Acceptor implements Runnable {
	
    private static Logger		logger = LogManager.getLogger(Acceptor.class);
    
    public static final int 	DEFAULT_PORT = 6666;
    public static final int 	WORKER = 64;
    public static final String 	HEADER_DELIMITER = ":";
    public static final String 	HEADER_FIELD_DELIMITER = "=";
    public static final String 	HEADER_FIELD_LENGTH = "length";
    
    private Context				context;
    private int 				port;
	private ServerSocket		serverSocket;
	private ExecutorService 	threadPool;
    
    
    public Acceptor(Context context) {
    	this.context = context;
    
    	// if no port is set in url use the default one
    	if ((this.port = context.getLocalUrl().getPort()) == -1) {
    		logger.debug("No local port specified, using default one: {}", DEFAULT_PORT);
    		this.port = DEFAULT_PORT;
    	}
    	
    	threadPool = Executors.newFixedThreadPool(WORKER);
    }

	@Override
	public void run() {
		
		int retryCount = 3;
		boolean socketSuccess = openServerSocket();
		
		while (! socketSuccess && retryCount > 0) { 

			logger.warn("Retry {} times", retryCount);

			try {
				Thread.sleep(20 * (4 - retryCount));
			} catch (InterruptedException e) {
				logger.warn("can not open server socket on port {}: {}",port, e.toString());
			}
			socketSuccess = openServerSocket();
			retryCount--;
		}
		
		if (socketSuccess) {
			logger.debug("Server socket started", serverSocket.getInetAddress().getHostAddress());
		} else {
			logger.fatal("can not open server socket on port {}: {} giving up",port);
			System.exit(2);
		}

		while(! Thread.currentThread().isInterrupted() ) {

			Socket client = null;
			
			try {

				client = serverSocket.accept();

			} catch (IOException e) {
				logger.error("unable to open client port due to exception {}", e.toString());
			}
			
			// handle client socket in new Thread
			threadPool.execute( new Worker(client, context));
		}
	}
	
	private boolean openServerSocket() {

		try {

			serverSocket = new ServerSocket(port);

		} catch (IOException e) {
			logger.warn("can not open server socket on port {}: {}",port, e.toString());
			return false;
		}
		return true;
	}
}
