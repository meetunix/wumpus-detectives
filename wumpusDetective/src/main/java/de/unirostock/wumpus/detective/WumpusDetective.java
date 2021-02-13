package de.unirostock.wumpus.detective;

import java.util.concurrent.Callable;

import de.unirostock.wumpus.core.world.WorldCreator;
import de.unirostock.wumpus.detective.agent.CarefulAgent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.unirostock.wumpus.detective.agent.AgentLogic;
import de.unirostock.wumpus.detective.agent.LogicA2M;
import de.unirostock.wumpus.detective.entities.AgentContext;
import de.unirostock.wumpus.core.Util;
import de.unirostock.wumpus.core.acceptor.Acceptor;
import de.unirostock.wumpus.core.messageQueue.MessageQueueImpl;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "Agent for detecting and killing the Wumps",mixinStandardHelpOptions = true,
			name = "WumpusDetective",version = "wumpusDetective 0.0.0")

public class WumpusDetective implements Callable<Integer>{

    private static Logger logger;
    
    public static final String PROTOCOL = "wd";
    
	@Option(names = { "-l", "--local-uri"},required=true, description = "local URI listen to")
	private static String localURLString;
	
	@Option(names = { "-m", "--monitor-uri"},required=true, description = "URI to the monitor")
	private static String monitorURLString;
	
	@Option(names = { "-n", "--name"},required=true, description = "the detectives name")
	private static String name;
	
	@Option(names = { "-v", "--verbosity-level"},
			description = "one of the log levels: info warn debug")
	private static String logLevel = "debug"; // warn, info or debug

	@Option(names = { "-he", "--field-height"},
			description = "the height of the field")
	private static int height = 32;

	AgentContext context;
	Thread acceptor;

    public static void main( String[] args ){

    	int exitCode = new CommandLine(new WumpusDetective()).execute(args);
    	System.exit(exitCode);
    }

	@Override
	public Integer call() throws Exception {

		WorldCreator.setyDim(height);
		WorldCreator.setxDim((int) Math.round(height * (16.0 / 9.0)));
	
		setUpAgent();
		
		// start the agent
		AgentLogic al = new CarefulAgent(context);
		al.start();
		
		acceptor.interrupt();
	
		return -1;
	}

	/*
	 * Set things up
	 */
	
	void setUpAgent() {

		loggingSetUp();
		
		// create context with builder pattern
		context = new AgentContext.Builder()
				.name(name)
				.localURL(Util.validateURI(localURLString))
				.monitorURL(Util.validateURI(monitorURLString))
				.acceptorQueue(new MessageQueueImpl())
				.build();
	
		// start the Acceptor Thread
		acceptor = new Thread(new Acceptor(context), "Acceptor");
		acceptor.start();
	}

	private void loggingSetUp() {
		
		// set system properties for log4j
		System.setProperty("detectiveName", name);
		System.setProperty("logLevel", logLevel.toLowerCase());

		logger = LogManager.getLogger(WumpusDetective.class);

		logger.info("WumpusDetective {} started.", name);
	}
	
}