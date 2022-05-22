package edu.wpi.rail.jrosbridge.handler;

import org.springframework.web.socket.WebSocketSession;

/**
 * The RosHandler interface defines the methods that will be called during
 * certain events in the Ros connection object.
 * 
 * @author Russell Toris - russell.toris@gmail.com
 * @version April 1, 2014
 */
public interface RosHandler {

	/**
	 * Handle the connection event. This occurs during a successful connection
	 * to rosbridge.
	 * 
	 * @param session
	 *            The session associated with the connection.
	 */
	public void handleConnection(WebSocketSession session);

	/**
	 * Handle the disconnection event. This occurs during a successful
	 * disconnection from rosbridge.
	 * 
	 * @param session
	 *            The session associated with the disconnection.
	 */
	public void handleDisconnection(WebSocketSession session);

	/**
	 * Handle the error event.
	 * 
	 * @param session
	 *            The session associated with the error.
	 * @param t
	 *            The error.
	 */
	public void handleError(WebSocketSession session, Throwable t);
}
