package de.unirostock.wumpus.core.communicator;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.unirostock.wumpus.core.acceptor.Acceptor;
import de.unirostock.wumpus.core.messages.Message;


/*
 * TODO: timeout not yet implemented
 * 
 */

public class CommunicatorImpl implements Communicator {

    private static Logger logger = LogManager.getLogger(CommunicatorImpl.class);
	
    private Optional<String> getJsonFromMessage(Message message) {

    	String json = null;
    	ObjectMapper mapper = new ObjectMapper();

    	try {
    		
			json = mapper.writeValueAsString(message);

		} catch (JsonProcessingException e) {
			logger.debug("unable to map Message to json while sending due to: {}", e.toString());
		}
    	return Optional.ofNullable(json);
    }
    
    private String buildMessage(String payload, Map<String,String> headerFields) {
    	
    	StringBuilder sb = new StringBuilder();
    	for (String field: headerFields.keySet()) {
    		sb.append(field);
    		sb.append(Acceptor.HEADER_FIELD_DELIMITER);
    		sb.append(headerFields.get(field));
    		sb.append(Acceptor.HEADER_DELIMITER);
    	}
    	sb.append("\n");
    	sb.append(payload);
    	sb.append("\n");
		return sb.toString();
    }
    
    
    private Optional<BufferedOutputStream> getOutputStream(Socket socket) {

    	BufferedOutputStream bos = null; 

		try {
			
			bos = new BufferedOutputStream(socket.getOutputStream());

		} catch (IOException e) {
			logger.error("unable to obtain output stream from socket due to: {}",
					e.toString());
		}
		
		return Optional.ofNullable(bos);
    }
    
    
    private boolean sendMessageToURL(String messageString, URI agentURL) {
    	
    	boolean successfullTransmitted = false;
    	int port = agentURL.getPort();
    	String host  = agentURL.getHost();
    	Socket client = null;
		BufferedOutputStream bos;
    	
		try {

			client = new Socket(host, port);
			
			client.setTcpNoDelay(true);
			logger.debug("new socket created: [{}: {}]", client.getInetAddress(), client.getPort());
		
			bos = getOutputStream(client).orElseThrow(Exception::new);
			bos.write(messageString.getBytes(Charset.forName("UTF-8")));

			bos.flush();
			bos.close();

			client.close();

			successfullTransmitted = true;

		} catch (IOException e) {
			logger.error("Unable to close socket");
			
		} catch (Exception e) {
			logger.error("unable to establish connection to [{}: {}] due to: ",
					host, port, e.toString());
		}
		
		return successfullTransmitted;
    }
    

	@Override
	public boolean sendMessage(URI agentURL, Message message, int timeout) {

		long startTime, stopTime;
		int payloadLen;
		
		String payload;
		
		Map<String,String> headerFields = new HashMap<>(4);
		
		// generate payload
		try {

			payload = getJsonFromMessage(message).orElseThrow(Exception::new);
			
			payloadLen = payload.getBytes(Charset.forName("UTF-8")).length;
			
		} catch (Exception e) {
			logger.error("unable to send message to [{}: {}] giving up", agentURL, message);
			return false;
		}
		
		headerFields.put(Acceptor.HEADER_FIELD_LENGTH, String.valueOf(payloadLen));
		
		String messageString = buildMessage(payload, headerFields);
		
		if (sendMessageToURL(messageString, agentURL)) {
			logger.debug("Message to {} was successfully transmitted", agentURL.toString());
			logger.trace("the Message was:  {}", payload);
			return true;
		} else {
			logger.warn("Message to {} was NOT successfully transmitted", agentURL.toString());
			logger.trace("the Message was:  {}", payload);
			return false;
		}
	}

}