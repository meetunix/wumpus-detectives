package de.unirostock.wumpus.core.acceptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.unirostock.wumpus.core.entities.Context;
import de.unirostock.wumpus.core.messages.Message;


public class Worker implements Runnable {

    private static Logger logger = LogManager.getLogger(Acceptor.class);
	
	private Socket 				client;
	private Context 			context;
	
	public Worker( Socket client, Context context) {
		this.client = client;
		this.context = context;
		
		logger.debug("New worker created who handles the connection to [ {} : {}}"
				, client.getInetAddress(), client.getPort());
	}
	
	private Optional<BufferedReader> getReader() {

		BufferedReader inReader = null;
		try {

			inReader = new BufferedReader(new InputStreamReader(client.getInputStream()));

		} catch (IOException e) {
			logger.debug("unable to read from client connection [{} : {}]",
					client.getInetAddress(), client.getPort());
		}
		return Optional.ofNullable(inReader);
	}
	
	private Optional<String> getLine(BufferedReader input) {

		String line = null;
		try {

				line = input.readLine();
				logger.trace("GET_LINE: {}", line);

		} catch (IOException e) {
			logger.error("unable to read Message from [{} : {}] due to: {}",
					client.getInetAddress(), client.getPort(), e.toString());
		}
		return Optional.ofNullable(line);
	}
	
	private Optional<String> getHeaderField(String fieldName, String header) {
		
		String fieldValue = null;
		
		String headerFields[] = header.split(Acceptor.HEADER_DELIMITER);
		
		for (String field : headerFields) {
			if (field.contains(fieldName)) {
				String field2 = field;
				fieldValue = field2.split(Acceptor.HEADER_FIELD_DELIMITER)[1];
			}
		}
		logger.trace("HEADER: {} -> {}", fieldName, fieldValue);
		return Optional.ofNullable(fieldValue);
	}
	
	private Optional<Message> getMessageFromPayload(String payload) {

		ObjectMapper mapper = new ObjectMapper();
		Message message = null;
		try {

			message = mapper.readValue(payload, Message.class);

		} catch (JsonMappingException e) {
			logger.error("unable to map payload to Message object while receiving from [{}: {}] "
					+ "due to: {}",
					client.getInetAddress(), client.getPort(), e.toString());
			Thread.currentThread().interrupt();
		} catch (JsonProcessingException e) {
			logger.error("unable to parse payload (maybe no valid json?) while"
					+ " receiving from [{}: {}]",
					client.getInetAddress(), client.getPort());
		}
		
		return Optional.ofNullable(message);
	}
	
	@Override
	public void run() {
		
		BufferedReader input;
		String header;
		String payload;
		Message message;

		int payloadLen;
		int actPayloadLen;
		
		try {

			// trying to get inputstream from socket
			input = getReader()
					.orElseThrow(() -> new InterruptedException("inputstream error"));

			// trying to read header
			header = getLine(input)
					.orElseThrow(() -> new InterruptedException("header error"));
			
			// read header field length
			String payloadLenString = getHeaderField(Acceptor.HEADER_FIELD_LENGTH,header)
					.orElseThrow(() -> new InterruptedException("header field error"));
			
			payloadLen = Integer.valueOf(payloadLenString);
			
			// read the Message (payload)
			payload = getLine(input)
					.orElseThrow(() -> new InterruptedException("payload error"));
					
			// check the payload length in byte
			actPayloadLen = payload.getBytes(Charset.forName("UTF-8")).length;
			
			if (actPayloadLen != payloadLen) {
				logger.error("header message length and actual length differ .. giving up");
				return;
			}
			
			// convert payload to Message object
			message = getMessageFromPayload(payload)
					.orElseThrow(() -> new InterruptedException("payload mapping error"));

			client.close();

		} catch (IOException e) {
			logger.error("Unable to close Socket");
			return;
		} catch (InterruptedException e) {
			logger.error("unable to receive message from [{}: {}] due to: {}",
					client.getInetAddress(), client.getPort(), e.toString());
			return;
		}
			
		context.getAcceptorQueue().put(message);

		logger.debug("a Message was received from  [{}: {}] and was added to the queue",
				client.getInetAddress(), client.getPort());
	}
}