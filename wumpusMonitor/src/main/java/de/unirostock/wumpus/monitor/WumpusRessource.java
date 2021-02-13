package de.unirostock.wumpus.monitor;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.unirostock.wumpus.core.world.MonitorWorldState;
import de.unirostock.wumpus.monitor.entities.MonitorContext;

/**
 * The world state is provided at /wumpus/worldstate
 */
@Path("/wumpus/")
public class WumpusRessource {

    private static Logger logger = LogManager.getLogger(WumpusRessource.class);

	@Inject
	MonitorContext context;

    ObjectMapper mapper;

    public WumpusRessource() {
    	this.mapper = new ObjectMapper();
	}

    @GET
    @Path("worldstate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWordState(@DefaultValue("false") @QueryParam("human") boolean human) {

    	logger.debug("PARAM human: {}", human);
		MonitorWorldState state = context.getMonitorState();

		try {
			
			// request was sent too early, e.g. in subscription phase
			if (state == null)
				return Response.noContent().status(Response.Status.NO_CONTENT).build();
			
			if (human)
				mapper.enable(SerializationFeature.INDENT_OUTPUT); // for human eyes only

			String jsonResponse = mapper.writeValueAsString(state);
			return Response.ok(jsonResponse).build();

		} catch (JsonProcessingException e) {
			logger.error("Unable to (de)serialize. Exception was thrown: {}", e.toString());
		}

		return Response.noContent().status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}