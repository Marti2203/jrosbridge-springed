package edu.wpi.rail.jrosbridge;

import jakarta.websocket.ClientEndpoint;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonParsingException;


import edu.wpi.rail.jrosbridge.callback.CallServiceCallback;
import edu.wpi.rail.jrosbridge.services.ServiceRequest;

import edu.wpi.rail.jrosbridge.callback.ServiceCallback;
import edu.wpi.rail.jrosbridge.callback.TopicCallback;
import edu.wpi.rail.jrosbridge.handler.RosHandler;
import edu.wpi.rail.jrosbridge.messages.Message;
import edu.wpi.rail.jrosbridge.services.ServiceResponse;
import org.springframework.util.Base64Utils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * The Ros object is the main connection point to the rosbridge server. This
 * object manages all communication to-and-from ROS. Typically, this object is
 * not used on its own. Instead, helper classes, such as
 * {@link edu.wpi.rail.jrosbridge.JRosbridge.Topic Topic}, are used.
 * 
 * @author Russell Toris - russell.toris@gmail.com
 * @version April 1, 2014
 */
public class Ros extends TextWebSocketHandler {

	// active session (stored upon connection)
	private WebSocketSession session;

	// used throughout the library to create unique IDs for requests.
	private long idCounter = 0;

	// keeps track of callback functions for a given topic
	private final HashMap<String, ArrayList<TopicCallback>> topicCallbacks = new HashMap<>();

	// keeps track of callback functions for a given service request
	private final HashMap<String, ServiceCallback> serviceCallbacks = new HashMap<>();

	// keeps track of callback functions for a given advertised service
	private final HashMap<String, CallServiceCallback> callServiceCallbacks = new HashMap<>();

	// keeps track of handlers for this connection
	private final ArrayList<RosHandler> handlers = new ArrayList<>();



	/**
	 * Create a connection to ROS with the given web socket session.
	 */
	public Ros(WebSocketSession session) {
		this.session = session;
	}

	/**
	 * Get the next unique ID number for this connection.
	 * 
	 * @return The next unique ID number for this connection.
	 */
	public long nextId() {
		return this.idCounter++;
	}

	/**
	 * Add a handler to this connection. This handler is called when the
	 * associated events occur.
	 * 
	 * @param handler
	 *            The handler to add.
	 */
	public void addRosHandler(RosHandler handler) {
		this.handlers.add(handler);
	}


	/**
	 * Disconnect the connection to rosbridge. Errors are printed to the error
	 * output stream.
	 * 
	 * @return Returns true if the disconnection was successful and false
	 *         otherwise.
	 */
	public boolean disconnect() {
		if (this.isConnected()) {
			try {
				this.session.close();
				return true;
			} catch (IOException e) {
				System.err.println("[ERROR]: Could not disconnect: "
						+ e.getMessage());
			}
		}
		// could not disconnect cleanly
		return false;
	}

	/**
	 * Check if there is a connection to rosbridge.
	 * 
	 * @return If there is a connection to rosbridge.
	 */
	public boolean isConnected() {
		return this.session != null && this.session.isOpen();
	}

	/**
	 * This function is called once a successful connection is made.
	 *
	 * @param session
	 *            The session associated with the connection.
	 */
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		this.session = session;
		// call the handlers
		for (RosHandler handler : this.handlers) {
			handler.handleConnection(session);
		}
	}


	/**
	 * This function is called once a successful disconnection is made.
	 *
	 * @param session
	 *            The session associated with the disconnection.
	 */

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		this.session = null;

		// call the handlers
		for (RosHandler handler : this.handlers) {
			handler.handleDisconnection(session);
		}
	}

	/**
	 * This function is called if an error occurs.
	 *
	 * @param session
	 *            The session for the error.
	 */

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		// call the handlers
		for (RosHandler handler : this.handlers) {
			handler.handleError(session, exception);
		}
	}

	/**
	 * This method is called once an entire message has been read in by the
	 * connection from rosbridge. It will parse the incoming JSON and attempt to
	 * handle the request appropriately.
	 *
	 * @param message
	 *            The incoming JSON message from rosbridge.
	 */
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		try {
			// parse the JSON
			JsonObject jsonObject = Json
					.createReader(new StringReader(message.getPayload())).readObject();

			// check for compression
			String op = jsonObject.getString(JRosbridge.FIELD_OP);
			if (op.equals(JRosbridge.OP_CODE_PNG)) {
				String data = jsonObject.getString(JRosbridge.FIELD_DATA);
				// decompress the PNG data
				byte[] bytes = Base64Utils.decode(data.getBytes());
				Raster imageData = ImageIO
						.read(new ByteArrayInputStream(bytes)).getRaster();

				// read the RGB data
				int[] rawData = null;
				rawData = imageData.getPixels(0, 0, imageData.getWidth(),
						imageData.getHeight(), rawData);
				StringBuilder buffer = new StringBuilder();
				for (int rawDatum : rawData) {
					buffer.append((char) rawDatum);
				}

				// reparse the JSON
				JsonObject newJsonObject = Json.createReader(
						new StringReader(buffer.toString())).readObject();
				handleMessage(newJsonObject);
			} else {
				handleMessage(jsonObject);
			}
		} catch (NullPointerException | IOException | JsonParsingException e) {
			// only occurs if there was an error with the JSON
			System.err.println("[WARN]: Invalid incoming rosbridge protocol: "
					+ message);
		}
	}

	/**
	 * Handle the incoming rosbridge message by calling the appropriate
	 * callbacks.
	 * 
	 * @param jsonObject
	 *            The JSON object from the incoming rosbridge message.
	 */
	private void handleMessage(JsonObject jsonObject) {
		// check for the correct fields
		String op = jsonObject.getString(JRosbridge.FIELD_OP);
		switch (op) {
			case JRosbridge.OP_CODE_PUBLISH:
				// check for the topic name
				String topic = jsonObject.getString(JRosbridge.FIELD_TOPIC);

				// call each callback with the message
				ArrayList<TopicCallback> callbacks = topicCallbacks.get(topic);
				if (callbacks != null) {
					Message msg = new Message(
							jsonObject.getJsonObject(JRosbridge.FIELD_MESSAGE));
					for (TopicCallback cb : callbacks) {
						cb.handleMessage(msg);
					}
				}
				break;
			case JRosbridge.OP_CODE_SERVICE_RESPONSE: {
				// check for the request ID
				String id = jsonObject.getString(JRosbridge.FIELD_ID);

				// call the callback for the request
				ServiceCallback cb = serviceCallbacks.get(id);
				if (cb != null) {
					// check if a success code was given
					boolean success = !jsonObject
							.containsKey(JRosbridge.FIELD_RESULT) || jsonObject
							.getBoolean(JRosbridge.FIELD_RESULT);
					// get the response
					JsonObject values = jsonObject
							.getJsonObject(JRosbridge.FIELD_VALUES);
					ServiceResponse response = new ServiceResponse(values, success);
					cb.handleServiceResponse(response);
				}
				break;
			}
			case JRosbridge.OP_CODE_CALL_SERVICE: {
				// check for the request ID
				String id = jsonObject.getString("id");
				String service = jsonObject.getString("service");

				// call the callback for the request
				CallServiceCallback cb = callServiceCallbacks.get(service);
				if (cb != null) {
					// get the response
					JsonObject args = jsonObject
							.getJsonObject(JRosbridge.FIELD_ARGS);
					ServiceRequest request = new ServiceRequest(args);
					request.setId(id);
					cb.handleServiceCall(request);
				}
				break;
			}
			default:
				System.err.println("[WARN]: Unrecognized op code: "
						+ jsonObject);
				break;
		}

	}

	/**
	 * Send the given JSON object to rosbridge.
	 * 
	 * @param jsonObject
	 *            The JSON object to send to rosbridge.
	 * @return If the sending of the message was successful.
	 */
	public boolean send(JsonObject jsonObject) {
		// check the connection
		if (this.isConnected()) {
			try {
				// send it as text
				this.session.sendMessage(new TextMessage(jsonObject.toString()));
				return true;
			} catch (IOException e) {
				System.err.println("[ERROR]: Could not send message: "
						+ e.getMessage());
			}
		}
		// message send failed
		return false;
	}

	/**
	 * Sends an authorization request to the server.
	 * 
	 * @param mac
	 *            The MAC (hash) string given by the trusted source.
	 * @param client
	 *            The IP of the client.
	 * @param dest
	 *            The IP of the destination.
	 * @param rand
	 *            The random string given by the trusted source.
	 * @param t
	 *            The time of the authorization request.
	 * @param level
	 *            The user level as a string given by the client.
	 * @param end
	 *            The end time of the client's session.
	 */
	public void authenticate(String mac, String client, String dest,
			String rand, int t, String level, int end) {
		// build and send the rosbridge call
		JsonObject call = Json.createObjectBuilder()
				.add(JRosbridge.FIELD_OP, JRosbridge.OP_CODE_AUTH)
				.add(JRosbridge.FIELD_MAC, mac)
				.add(JRosbridge.FIELD_CLIENT, client)
				.add(JRosbridge.FIELD_DESTINATION, dest)
				.add(JRosbridge.FIELD_RAND, rand).add(JRosbridge.FIELD_TIME, t)
				.add(JRosbridge.FIELD_LEVEL, level)
				.add(JRosbridge.FIELD_END_TIME, end).build();
		this.send(call);
	}

	/**
	 * Register a callback for a given topic.
	 * 
	 * @param topic
	 *            The topic to register this callback with.
	 * @param cb
	 *            The callback that will be called when messages come in for the
	 *            associated topic.
	 */
	public void registerTopicCallback(String topic, TopicCallback cb) {
		// check if any callbacks exist yet
		if (!this.topicCallbacks.containsKey(topic)) {
			this.topicCallbacks.put(topic, new ArrayList<>());
		}

		// add the callback
		this.topicCallbacks.get(topic).add(cb);
	}

	/**
	 * Deregister a callback for a given topic.
	 * 
	 * @param topic
	 *            The topic associated with the callback.
	 * @param cb
	 *            The callback to remove.
	 */
	public void deregisterTopicCallback(String topic, TopicCallback cb) {
		// check if any exist for this topic
		if (this.topicCallbacks.containsKey(topic)) {
			// remove the callback if it exists
			ArrayList<TopicCallback> callbacks = this.topicCallbacks.get(topic);
			callbacks.remove(cb);

			// remove the list if it is empty
			if (callbacks.size() == 0) {
				this.topicCallbacks.remove(topic);
			}
		}
	}

	/**
	 * Register a callback for a given outgoing service call.
	 *
	 * @param serviceCallId
	 *            The unique ID of the service call.
	 * @param cb
	 *            The callback that will be called when a service response comes
	 *            back for the associated request.
	 */
	public void registerServiceCallback(String serviceCallId, ServiceCallback cb) {
		// add the callback
		this.serviceCallbacks.put(serviceCallId, cb);
	}

	/**
	 * Register a callback for a given incoming service request.
	 *
	 * @param serviceName
	 *            The unique name of the service call.
	 * @param cb
	 *            The callback that will be called when a service request comes
	 *            in for the associated request.
	 */
	public void registerCallServiceCallback(String serviceName, CallServiceCallback cb) {
		// add the callback
		this.callServiceCallbacks.put(serviceName, cb);
	}

	/**
	 * Deregister a callback for a given incoming service request.
	 *
	 * @param serviceName
	 *            The unique name of the service call.
	 */
	public void deregisterCallServiceCallback(String serviceName) {
		// remove the callback
		callServiceCallbacks.remove(serviceName);
	}
}
