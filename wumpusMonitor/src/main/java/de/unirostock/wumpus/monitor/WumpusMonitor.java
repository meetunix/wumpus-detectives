package de.unirostock.wumpus.monitor;

import de.unirostock.wumpus.core.acceptor.Acceptor;
import de.unirostock.wumpus.core.messageQueue.MessageQueue;
import de.unirostock.wumpus.core.messageQueue.MessageQueueImpl;
import de.unirostock.wumpus.core.world.WorldCreator;
import de.unirostock.wumpus.monitor.entities.MonitorContext;
import de.unirostock.wumpus.monitor.world.WorldOperatorThread;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Callable;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

@Command(description = "Wumpus Monitor application",
			mixinStandardHelpOptions = true,
			name = "WumpusMonitor",
			version = "0.0.0")
public class WumpusMonitor implements Callable<String>{

	@Option(names = { "--benchmark"}, description = "Sets the base URI.")
	public static boolean benchmark = false;

	@Option(names = { "-b", "--base-http-uri"},required=true,
			description = "Sets the base URI.")
	public static String baseHttpURI;
	
	@Option(names = { "-w", "--wumpus-uri"},required=true,
			description = "Sets the URI for the communication to the detectives (agents).")
	public static String wumpusURIString;

	@Option(names = { "-l", "--log-level"},
			required=true,
			description = "Sets the log level (trace, debug, info, warn).")
	private static String logLevel;
	
	@Option(names = { "-p", "--log-path"},
			description = "Sets the path to the log file.")
	private static String logPath = "./";

	@Option(names = { "-s", "--subscription-phase"},
			description = "Sets the duration of the subscription phase in seconds.")
	private static int secondsSubscrPhase = 5; // default: 5 seconds
	
	@Option(names = { "-t", "--throttle-down-seconds"},
			description = "Inserts a delay (millis) between each simulation step.")
	private static long throttleMillis = 0; // default: no throttling

	@Option(names = { "-r", "--communication-radius"},
			description = "the radius specifies the distance (fields) over which an agent can "
			+ "communicate with other agents.")
	private static int communicationRadius = 0; // default: the agent can only talk to himself

	@Option(names = { "-he", "--field-height"},
			description = "the height of the field")
	private static int height = 32;

	@Option(names = { "-wu", "--number-wumpi"},
			description = "the number of Wumpi on the field")
	private static int wumpi = 95;


	@Option(names = { "-pi", "--number-pits"},
			description = "the number of pits on the field")
	private static int pits = 95;


	@Option(names = { "-go", "--number-gold"},
			description = "the number of gold pieces on the field")
	private static int gold = 75;


	@Option(names = { "-ro", "--number-rocks"},
			description = "the number of rocks on the field")
	private static int rocks = 65;

	@Option(names = { "-ex", "--number-exits"},
			description = "the number of exits on the field")
	private static int exits = 10;


	private static MonitorContext context;
	
	private static final String BENCHMARK_RESULT_FILE = "results_benchmark.csv";

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
    	// parse cli arguments
    	int exitCode = new CommandLine(new WumpusMonitor()).execute(args);
    	System.exit(exitCode);
    }
	
    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
    	
        // create a resource config that scans for JAX-RS resources and providers
        // in de.unirostock.monitor package
        final ResourceConfig rc = new ResourceConfig()
        		.packages("de.unirostock.wumpus.monitor");
        rc.property(ServerProperties.WADL_FEATURE_DISABLE, true);
        
        /**** dependency injection ****/
        rc.register(new AbstractBinder() {
        	@Override
        	protected void configure() {
        		bind(context).to(MonitorContext.class);
        	}
        });

        // register the interceptor classes for compressed transmission
        rc.register(GZIPWriterInterceptor.class);
        rc.register(GZIPReaderInterceptor.class);

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(baseHttpURI), rc);
    }
    
    private void writeStatsToFile() {
    	
    	Path path = Path.of(BENCHMARK_RESULT_FILE);
    	
    	String line = String.format(
    			"%s,%s,%s,%s\n",
    			context.getAmountInitialAgents(),
    			context.getCommunicationRadius(),
    			context.getCurrentActionCount(),
    			context.getEarnedReward()
    			);
    	
		try {

			if (Files.exists(path)) { 

				if (Files.isWritable(path)) {
					System.out.println("Stats written to: " + path.toString());
					Files.write(path, line.getBytes(Charset.forName("UTF-8")), StandardOpenOption.APPEND);
				}
				else {
					System.out.println("Unable to write stat file, printing stats instead\n" + line);
				}

			} else {
				String[] firstLines = {"agents,radius,steps,reward\n", line};
				Files.createFile(path);
				
				for(String s: firstLines) 
					Files.write(path, s.getBytes(Charset.forName("UTF-8")), StandardOpenOption.APPEND);
			}
				
		} catch (IOException e) {
			System.err.println("Something went wrong while writing to stat file, check permissions");
			System.err.println(e.toString());
		}
    }


	@Override
	public String call() throws Exception {

		WorldCreator.setyDim(height);
		WorldCreator.setxDim((int) Math.round(height * (16.0 / 9.0)));

        // set log configuration
        System.setProperty("logPath", logPath);
        System.setProperty("logLevel", logLevel);
		
		// create the acceptor queue
		MessageQueue acceptorQueue = new MessageQueueImpl();
		
		// build up monitor context
		URI wumpusURI = new URI(wumpusURIString);
		context = new MonitorContext(wumpusURI,acceptorQueue);
		context.setSecondsSubscriptionPhase(secondsSubscrPhase);
		context.setThrottleMillis(throttleMillis);
		context.setCommunicationRadius(communicationRadius);
		context.setFieldWidth(height);
		context.setNumberWumpi(wumpi);
		context.setNumberPits(pits);
		context.setNumberGold(gold);
		context.setNumberRocks(rocks);
		context.setNumberExits(exits);
		
		// start an acceptor thread for receiving messages from agent
		// the acceptor is the provider for the acceptorQueue
		Thread acceptor = new Thread(new Acceptor(context), "acceptor");
		acceptor.start();
		
		// change java.util.logging levels for grizzly http server 
		String[] grizzlyPaths = {
				"org.glassfish.grizzly.http.server.HttpServer",
				"org.glassfish.grizzly.http.server.NetworkListener"
		};
		
		for (String s: grizzlyPaths) {
			Logger l = Logger.getLogger(s);
			l.setLevel(Level.WARNING);
			l.setUseParentHandlers(false);
			ConsoleHandler ch = new ConsoleHandler();
			ch.setLevel(Level.WARNING);
			l.addHandler(ch);
		}
		
		// start the WorldOperatorThread
		// the operator thread is the consumer for the acceptorQueue
		Thread operator = new Thread(new WorldOperatorThread(context));
		operator.start();
		
		
		// start the http sever to create a proper HTTP-endpoint used by wumpusVis or other software
        final HttpServer server = startServer();

        System.out.println(String.format(
        		"\nWumpusMonitor started at %s.\nWaiting %d seconds for agents to subscribe.\n"
        		,baseHttpURI, secondsSubscrPhase));
               
		while (operator.isAlive()) {
			Thread.sleep(500);
		};
		
		if (benchmark)
			if (context.simulationSuccessful) {writeStatsToFile();}
		
		System.out.println("\nSimulation finished - waiting 5 seconds before closing.");
		Thread.sleep(5000);
		
        server.shutdownNow();
        System.out.println("Goodbye");
		return null;
	}
}