package org.jrosbridge.springed.callback;

import org.jrosbridge.springed.messages.Message;

/**
 * The TopicCallback interface defines a single method which will be called when
 * an incoming message is received for an associated topic.
 * 
 * @author Russell Toris - russell.toris@gmail.com
 * @version April 1, 2014
 */
@FunctionalInterface
public interface TopicCallback {

	/**
	 * This function is called when an incoming message is received for a given
	 * topic. No ROS type checking is done on the internal message data.
	 * 
	 * @param message
	 *            The message that was received.
	 */
	void handleMessage(Message message);
}
