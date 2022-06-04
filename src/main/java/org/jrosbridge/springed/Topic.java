package org.jrosbridge.springed;

import java.io.IOException;
import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonObject;

import org.jrosbridge.springed.callback.TopicCallback;
import org.jrosbridge.springed.messages.Message;

/**
 * The Topic object is responsible for publishing and/or subscribing to a topic
 * in ROS.
 * 
 * @author Russell Toris - russell.toris@gmail.com
 * @version April 1, 2014
 */
public class Topic<Identifier> {

	private final Ros<Identifier> ros;
	private final Identifier id;
	private final String name;
	private final String type;
	private boolean isAdvertised;
	private boolean isSubscribed;
	private final ROSConstants.CompressionType compression;
	private final int throttleRate;

	// used to keep track of this object's callbacks
	private final ArrayList<TopicCallback> callbacks;

	// used to keep track of the subscription IDs
	private final ArrayList<String> ids;

	/**
	 * Create a ROS topic with the given information. No compression or
	 * throttling is used.
	 * 
	 * @param ros
	 *            A handle to the ROS connection.
	 * @param name
	 *            The name of the topic (e.g., "/cmd_vel").
	 * @param type
	 *            The message type (e.g., "std_msgs/String").
	 */
	public Topic(Ros<Identifier> ros, Identifier id, String name, String type) {
		this(ros, id, name, type, ROSConstants.CompressionType.none, 0);
	}

	/**
	 * Create a ROS topic with the given information. No throttling is used.
	 * 
	 * @param ros
	 *            A handle to the ROS connection.
	 * @param name
	 *            The name of the topic (e.g., "/cmd_vel").
	 * @param type
	 *            The message type (e.g., "std_msgs/String").
	 * @param compression
	 *            The type of compression used for this topic.
	 */
	public Topic(Ros<Identifier> ros, Identifier id, String name, String type,
			ROSConstants.CompressionType compression) {
		this(ros,id, name, type, compression, 0);
	}

	/**
	 * Create a ROS topic with the given information. No compression is used.
	 * 
	 * @param ros
	 *            A handle to the ROS connection.
	 * @param name
	 *            The name of the topic (e.g., "/cmd_vel").
	 * @param type
	 *            The message type (e.g., "std_msgs/String").
	 * @param throttleRate
	 *            The throttle rate to use for this topic.
	 */
	public Topic(Ros<Identifier> ros, Identifier id, String name, String type, int throttleRate) {
		this(ros,id , name, type, ROSConstants.CompressionType.none, throttleRate);
	}

	/**
	 * Create a ROS topic with the given information.
	 * 
	 * @param ros
	 *            A handle to the ROS connection.
	 * @param name
	 *            The name of the topic (e.g., "/cmd_vel").
	 * @param type
	 *            The message type (e.g., "std_msgs/String").
	 * @param compression
	 *            The type of compression used for this topic.
	 * @param throttleRate
	 *            The throttle rate to use for this topic.
	 */
	public Topic(Ros<Identifier> ros, Identifier id, String name, String type,
			ROSConstants.CompressionType compression, int throttleRate) {
		this.ros = ros;
		this.id = id;
		this.name = name;
		this.type = type;
		this.isAdvertised = false;
		this.isSubscribed = false;
		this.compression = compression;
		this.throttleRate = throttleRate;
		this.callbacks = new ArrayList<TopicCallback>();
		this.ids = new ArrayList<String>();
	}

	/**
	 * Get the ROS connection handle for this topic.
	 * 
	 * @return The ROS connection handle for this topic.
	 */
	public Ros<Identifier> getRos() {
		return this.ros;
	}

	/**
	 * Get the name of this topic.
	 * 
	 * @return The name of this topic.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Return the message type of this topic.
	 * 
	 * @return The message type of this topic.
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * Check if the current topic is advertising to ROS.
	 * 
	 * @return If the current topic is advertising to ROS.
	 */
	public boolean isAdvertised() {
		return this.isAdvertised;
	}

	/**
	 * Check if the current topic is subscribed to ROS.
	 * 
	 * @return If the current topic is subscribed to ROS.
	 */
	public boolean isSubscribed() {
		return this.isSubscribed;
	}

	/**
	 * Get the compression type for this topic.
	 * 
	 * @return The compression type for this topic.
	 */
	public ROSConstants.CompressionType getCompression() {
		return this.compression;
	}

	/**
	 * Get the throttle rate for this topic.
	 * 
	 * @return The throttle rate for this topic.
	 */
	public int getThrottleRate() {
		return this.throttleRate;
	}

	/**
	 * Subscribe to this topic. A callback function is required and will be
	 * called with any incoming message for this topic.
	 * 
	 * @param cb
	 *            The callback that will be called when incoming messages are
	 *            received.
	 */
	public void subscribe(TopicCallback cb) throws IOException {
		// register the callback function
		this.ros.registerTopicCallback(this.name, cb);
		// internal reference used during unsubscribe
		this.callbacks.add(cb);

		String subscribeId = "subscribe:" + this.name + ":" + this.ros.nextId();
		this.ids.add(subscribeId);

		// build and send the rosbridge call
		JsonObject call = Json.createObjectBuilder()
				.add(ROSConstants.FIELD_OP, ROSConstants.OP_CODE_SUBSCRIBE)
				.add(ROSConstants.FIELD_ID, subscribeId)
				.add(ROSConstants.FIELD_TYPE, this.type)
				.add(ROSConstants.FIELD_TOPIC, this.name)
				.add(ROSConstants.FIELD_COMPRESSION, this.compression.toString())
				.add(ROSConstants.FIELD_THROTTLE_RATE, this.throttleRate).build();
		this.ros.send(call,id);

		// set the flag indicating we have subscribed
		this.isSubscribed = true;
	}

	/**
	 * Unregisters as a subscriber for the topic. Unsubscribing will remove all
	 * the associated subscribe callbacks.
	 */
	public void unsubscribe() throws IOException {
		// remove this object's associated callbacks.
		for (TopicCallback cb : this.callbacks) {
			this.ros.deregisterTopicCallback(this.name, cb);
		}
		this.callbacks.clear();

		// build and send the rosbridge calls
		for (String id : this.ids) {
			JsonObject call = Json.createObjectBuilder()
					.add(ROSConstants.FIELD_OP, ROSConstants.OP_CODE_UNSUBSCRIBE)
					.add(ROSConstants.FIELD_ID, id)
					.add(ROSConstants.FIELD_TOPIC, this.name).build();
			this.ros.send(call,this.id);
		}

		// set the flag indicating we are not longer subscribed
		this.isSubscribed = false;
	}

	/**
	 * Registers as a publisher for the topic. This call will be automatically
	 * called by publish if you do not explicitly call it.
	 */
	public void advertise() throws IOException {
		// build and send the rosbridge call
		String advertiseId = "advertise:" + this.name + ":" + this.ros.nextId();
		JsonObject call = Json.createObjectBuilder()
				.add(ROSConstants.FIELD_OP, ROSConstants.OP_CODE_ADVERTISE)
				.add(ROSConstants.FIELD_ID, advertiseId)
				.add(ROSConstants.FIELD_TYPE, this.type)
				.add(ROSConstants.FIELD_TOPIC, this.name).build();
		this.ros.send(call,id);

		// set the flag indicating we are registered
		this.isAdvertised = true;
	}

	/**
	 * Unregister as a publisher for the topic.
	 */
	public void unadvertise() throws IOException {
		// build and send the rosbridge call
		String unadvertiseId = "unadvertise:" + this.name + ":"
				+ this.ros.nextId();
		JsonObject call = Json.createObjectBuilder()
				.add(ROSConstants.FIELD_OP, ROSConstants.OP_CODE_UNADVERTISE)
				.add(ROSConstants.FIELD_ID, unadvertiseId)
				.add(ROSConstants.FIELD_TOPIC, this.name).build();
		this.ros.send(call,id);

		// set the flag indicating we are no longer registered
		this.isAdvertised = false;
	}

	/**
	 * Publish the given message to ROS on this topic. If the topic is not
	 * advertised, it will be advertised first.
	 * 
	 * @param message
	 *            The message to publish.
	 */
	public void publish(Message message) throws IOException {
		// check if we have advertised yet.
		if (!this.isAdvertised()) {
			this.advertise();
		}

		// build and send the rosbridge call
		String publishId = "publish:" + this.name + ":" + this.ros.nextId();
		JsonObject call = Json.createObjectBuilder()
				.add(ROSConstants.FIELD_OP, ROSConstants.OP_CODE_PUBLISH)
				.add(ROSConstants.FIELD_ID, publishId)
				.add(ROSConstants.FIELD_TOPIC, this.name)
				.add(ROSConstants.FIELD_MESSAGE, message.toJsonObject()).build();
		this.ros.send(call,id);
	}
}
