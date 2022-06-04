package org.jrosbridge.springed;

import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.JsonObject;
import org.jrosbridge.springed.callback.CallServiceCallback;
import org.jrosbridge.springed.callback.ServiceCallback;
import org.jrosbridge.springed.callback.TopicCallback;
import org.jrosbridge.springed.messages.Message;
import org.jrosbridge.springed.services.ServiceRequest;
import org.jrosbridge.springed.services.ServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.Base64Utils;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * The Ros object is the main connection point to the rosbridge server. This object manages all
 * communication to-and-from ROS. Typically, this object is not used on its own. Instead, helper
 * classes, such as {@link Topic Topic}, are used.
 *
 * @author Russell Toris - russell.toris@gmail.com
 * @author Marting Mirchev - mirchevmartin2203@gmail.com
 * @version May 22, 2022
 */
public abstract class Ros<Identifier> extends TextWebSocketHandler {

  private final Logger logger = LoggerFactory.getLogger(Ros.class);

  // keeps track of callback functions for a given topic
  private final Map<String, List<TopicCallback>> topicCallbacks = new HashMap<>();

  // keeps track of callback functions for a given service request
  private final Map<String, ServiceCallback> serviceCallbacks = new HashMap<>();

  // keeps track of callback functions for a given advertised service
  private final Map<String, CallServiceCallback> callServiceCallbacks = new HashMap<>();

  private final Map<Identifier, WebSocketSession> activeSessions = new HashMap<>();
  private final Map<WebSocketSession, Identifier> activeSessionsInverse = new HashMap<>();

  // used throughout the library to create unique IDs for requests.
  private long idCounter = 0;

  public Ros() {
  }

  /**
   * Get the next unique ID number for this connection.
   *
   * @return The next unique ID number for this connection.
   */
  public long nextId() {
    return this.idCounter++;
  }

  @Override
  public void afterConnectionEstablished(@NonNull WebSocketSession session) {
    logger.info("ROS connection established with {}", session.getRemoteAddress());

    bootstrapConnection(session);
  }

  protected abstract void bootstrapConnection(WebSocketSession session);

  @Override
  public void afterConnectionClosed(@NonNull WebSocketSession session, CloseStatus status) {
    deregisterSession(session);

    logger.info("ROS connection closed. Status code {}", status.getCode());
  }


  @Override
  protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message)
      throws Exception {
    String payload = message.getPayload();
    logger.info("Receiving payload {}", payload);

    JsonObject data = Json.createReader(new StringReader(payload)).readObject();
    // check for compression
    String op = data.getString(ROSConstants.FIELD_OP);
    if (op.equals(ROSConstants.OP_CODE_PNG)) {
      handleImage(data);
    } else {
      handleMessage(data);
    }
  }

  private void handleImage(JsonObject data) throws IOException {
    String fieldData = data.getString(ROSConstants.FIELD_DATA);
    // decompress the PNG data
    byte[] bytes = Base64Utils.decode(fieldData.getBytes());
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
  }

  /**
   * Handle the incoming rosbridge message by calling the appropriate callbacks.
   *
   * @param jsonObject The JSON object from the incoming rosbridge message.
   */
  private void handleMessage(JsonObject jsonObject) {
    // check for the correct fields
    String op = jsonObject.getString(ROSConstants.FIELD_OP);
    switch (op) {
      case ROSConstants.OP_CODE_PUBLISH:
        handlePublish(jsonObject);
        break;
      case ROSConstants.OP_CODE_SERVICE_RESPONSE:
        handleServiceResponse(jsonObject);
        break;
      case ROSConstants.OP_CODE_CALL_SERVICE:
        handleCallService(jsonObject);
        break;
      case ROSConstants.OP_CODE_ADVERTISE:
        handleAdvertise(jsonObject);
        break;
      case ROSConstants.OP_CODE_ADVERTISE_SERVICE:
        handleAdvertiseService(jsonObject);
        break;
      case ROSConstants.OP_CODE_UNSUBSCRIBE:
        handleUnsubscribe(jsonObject);
        break;
      default:
        logger.warn("Unrecognized op code: {}", jsonObject);
        break;
    }

  }

  /**
   * Handle a service advertise operation.
   *
   * @param jsonObject The JSON Object from the incoming rosbridge message.
   */
  private void handleAdvertiseService(JsonObject jsonObject) {
  }

  private void handleUnsubscribe(JsonObject jsonObject) {

  }

  /**
   * Handle an advertise operation.
   *
   * @param jsonObject The JSON object from the incoming rosbridge message.
   */
  private void handleAdvertise(JsonObject jsonObject) {
  }

  /**
   * Handle a call service operation.
   *
   * @param jsonObject The JSON object from the incoming rosbridge message.
   */
  private void handleCallService(JsonObject jsonObject) {
    // check for the request ID
    String id = jsonObject.getString("id");
    String service = jsonObject.getString("service");

    // call the callback for the request
    CallServiceCallback cb = callServiceCallbacks.get(service);
    if (cb != null) {
      // get the response
      JsonObject args = jsonObject
          .getJsonObject(ROSConstants.FIELD_ARGS);
      ServiceRequest request = new ServiceRequest(args);
      request.setId(id);
      cb.handleServiceCall(request);
    }
  }

  /**
   * Handle a response service operation.
   *
   * @param jsonObject The JSON object from the incoming rosbridge message.
   */
  private void handleServiceResponse(JsonObject jsonObject) {
    // check for the request ID
    String id = jsonObject.getString(ROSConstants.FIELD_ID);

    // call the callback for the request
    ServiceCallback cb = serviceCallbacks.get(id);
    if (cb != null) {
      // check if a success code was given
      boolean success = !jsonObject
          .containsKey(ROSConstants.FIELD_RESULT) || Boolean.parseBoolean(jsonObject
          .getString(ROSConstants.FIELD_RESULT));
      // get the response
      JsonObject values = jsonObject
          .getJsonObject(ROSConstants.FIELD_VALUES);
      ServiceResponse response = new ServiceResponse(values, success);
      cb.handleServiceResponse(response);
    }
  }

  /**
   * Handle a publish operation.
   *
   * @param jsonObject The JSON object from the incoming rosbridge message.
   */
  private void handlePublish(JsonObject jsonObject) {
    // check for the topic name
    String topic = jsonObject.getString(ROSConstants.FIELD_TOPIC);

    // call each callback with the message
    List<TopicCallback> callbacks = topicCallbacks.get(topic);
    if (callbacks != null) {
      Message msg = new Message(jsonObject.getJsonObject(ROSConstants.FIELD_MESSAGE));
      for (TopicCallback cb : callbacks) {
        cb.handleMessage(msg);
      }
    }
  }

  /**
   * Register a callback for a given topic.
   *
   * @param topic The topic to register this callback with.
   * @param cb    The callback that will be called when messages come in for the associated topic.
   */
  public void registerTopicCallback(String topic, TopicCallback cb) {
    // check if any callbacks exist yet
    if (!topicCallbacks.containsKey(topic)) {
      topicCallbacks.put(topic, new ArrayList<>());
    }

    // add the callback
    topicCallbacks.get(topic).add(cb);
  }

  /**
   * Deregister a callback for a given topic.
   *
   * @param topic The topic associated with the callback.
   * @param cb    The callback to remove.
   */
  public void deregisterTopicCallback(String topic, TopicCallback cb) {
    // check if any exist for this topic
    if (topicCallbacks.containsKey(topic)) {
      // remove the callback if it exists
      List<TopicCallback> callbacks = topicCallbacks.get(topic);
      callbacks.remove(cb);

      // remove the list if it is empty
      if (callbacks.size() == 0) {
        topicCallbacks.remove(topic);
      }
    }
  }

  /**
   * Add a session for a specific robot to keep track of.
   *
   * @param session The web socket session used to communicate with the robot.
   * @param robotId The identifier of the robot. Currently, we use a MAC Address.
   */
  public void registerSession(WebSocketSession session, Identifier robotId) {
    activeSessions.put(robotId, session);
    activeSessionsInverse.put(session, robotId);
  }

  /**
   * Remove a session for a robot.
   *
   * @param robotId The identifier of the robot.
   */
  public void deregisterSession(Identifier robotId) {
    activeSessionsInverse.remove(activeSessions.get(robotId));
    activeSessions.remove(robotId);
  }

  /**
   * Remove a session for a robot.
   *
   * @param session The websocket session of the robot.
   */
  public void deregisterSession(WebSocketSession session) {
    activeSessions.remove(activeSessionsInverse.get(session));
    activeSessionsInverse.remove(session);
  }


  /**
   * Register a callback for a given outgoing service call.
   *
   * @param serviceCallId The unique ID of the service call.
   * @param cb            The callback that will be called when a service response comes back for
   *                      the associated request.
   */
  public void registerServiceCallback(String serviceCallId, ServiceCallback cb) {
    // add the callback
    serviceCallbacks.put(serviceCallId, cb);
  }

  /**
   * Register a callback for a given incoming service request.
   *
   * @param serviceName The unique name of the service call.
   * @param cb          The callback that will be called when a service request comes in for the
   *                    associated request.
   */
  public void registerCallServiceCallback(String serviceName, CallServiceCallback cb) {
    // add the callback
    callServiceCallbacks.put(serviceName, cb);
  }

  /**
   * Deregister a callback for a given incoming service request.
   *
   * @param serviceName The unique name of the service call.
   */
  public void deregisterCallServiceCallback(String serviceName) {
    // remove the callback
    callServiceCallbacks.remove(serviceName);
  }

  /**
   * Send data to a websocket representing a robot.
   *
   * @param call Object that will be sent.
   * @param id   identifier representing the robot.
   * @throws IOException if there is a websocket error.
   */
  public void send(JsonObject call, Identifier id) throws IOException {
    if (!activeSessions.containsKey(id)) {
      throw new IllegalArgumentException();
    }
    activeSessions.get(id).sendMessage(new BinaryMessage(call.toString().getBytes()));
  }
}
