package org.jrosbridge.springed.messages.std;

import java.io.StringReader;
import java.util.Arrays;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.jrosbridge.springed.messages.Message;

/**
 * The std_msgs/Int64MultiArray message. Please look at the MultiArrayLayout
 * message definition for documentation on all multiarrays.
 * 
 * @author Russell Toris -- russell.toris@gmail.com
 * @version April 1, 2014
 */
public class Int64MultiArray extends Message {

	/**
	 * The name of the layout field for the message.
	 */
	public static final java.lang.String FIELD_LAYOUT = "layout";

	/**
	 * The name of the data field for the message.
	 */
	public static final java.lang.String FIELD_DATA = "data";

	/**
	 * The message type.
	 */
	public static final java.lang.String TYPE = "std_msgs/Int64MultiArray";

	private final MultiArrayLayout layout;
	private final long[] data;

	/**
	 * Create a new, empty Int64MultiArray.
	 */
	public Int64MultiArray() {
		this(new MultiArrayLayout(), new long[] {});
	}

	/**
	 * Create a new Int64MultiArray with the given layout and data. The array of
	 * data will be copied longo this object.
	 * 
	 * @param layout
	 *            The specification of data layout.
	 * @param data
	 *            The array of data.
	 */
	public Int64MultiArray(MultiArrayLayout layout, long[] data) {
		// build the JSON object
		super(Json
				.createObjectBuilder()
				.add(Int64MultiArray.FIELD_LAYOUT, layout.toJsonObject())
				.add(Int64MultiArray.FIELD_DATA,
						Json.createReader(
								new StringReader(Arrays.toString(data)))
								.readArray()).build(), Int64MultiArray.TYPE);
		this.layout = layout;
		// copy the array
		this.data = new long[data.length];
		System.arraycopy(data, 0, this.data, 0, data.length);
	}

	/**
	 * Get the layout value of this Int64MultiArray.
	 * 
	 * @return The layout value of this Int64MultiArray.
	 */
	public MultiArrayLayout getLayout() {
		return this.layout;
	}

	/**
	 * Get the size of the array.
	 * 
	 * @return The size of the array.
	 */
	public int size() {
		return this.data.length;
	}

	/**
	 * Get the data value at the given index.
	 * 
	 * @param index
	 *            The index to get the data value of.
	 * @return The data value at the given index.
	 */
	public long get(int index) {
		return this.data[index];
	}

	/**
	 * Get the data array. Note that this array should never be modified
	 * directly.
	 * 
	 * @return The data array.
	 */
	public long[] getData() {
		return this.data;
	}

	/**
	 * Create a clone of this Int64MultiArray.
	 */
	@Override
	public Int64MultiArray clone() {
		return new Int64MultiArray(this.layout, this.data);
	}

	/**
	 * Create a new Int64MultiArray based on the given JSON string. Any missing
	 * values will be set to their defaults.
	 * 
	 * @param jsonString
	 *            The JSON string to parse.
	 * @return A Int64MultiArray message based on the given JSON string.
	 */
	public static Int64MultiArray fromJsonString(java.lang.String jsonString) {
		// convert to a message
		return Int64MultiArray.fromMessage(new Message(jsonString));
	}

	/**
	 * Create a new Int64MultiArray based on the given Message. Any missing
	 * values will be set to their defaults.
	 * 
	 * @param m
	 *            The Message to parse.
	 * @return A Int64MultiArray message based on the given Message.
	 */
	public static Int64MultiArray fromMessage(Message m) {
		// get it from the JSON object
		return Int64MultiArray.fromJsonObject(m.toJsonObject());
	}

	/**
	 * Create a new Int64MultiArray based on the given JSON object. Any missing
	 * values will be set to their defaults.
	 * 
	 * @param jsonObject
	 *            The JSON object to parse.
	 * @return A Int64MultiArray message based on the given JSON object.
	 */
	public static Int64MultiArray fromJsonObject(JsonObject jsonObject) {
		// check the layout
		MultiArrayLayout layout = jsonObject
				.containsKey(Int64MultiArray.FIELD_LAYOUT) ? MultiArrayLayout
				.fromJsonObject(jsonObject
						.getJsonObject(Int64MultiArray.FIELD_LAYOUT))
				: new MultiArrayLayout();

		// check the array
		long[] data = new long[] {};
		JsonArray jsonData = jsonObject
				.getJsonArray(Int64MultiArray.FIELD_DATA);
		if (jsonData != null) {
			// convert each data
			data = new long[jsonData.size()];
			for (int i = 0; i < data.length; i++) {
				data[i] = jsonData.getJsonNumber(i).longValue();
			}
		}
		return new Int64MultiArray(layout, data);
	}
}
