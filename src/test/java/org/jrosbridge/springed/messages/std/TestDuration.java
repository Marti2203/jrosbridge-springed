package org.jrosbridge.springed.messages.std;

import static org.junit.Assert.*;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;

import org.jrosbridge.springed.messages.Message;

public class TestDuration {

	private Duration empty, t1;

	@Before
	public void setUp() {
		empty = new Duration();
		t1 = new Duration(new org.jrosbridge.springed.primitives.Duration(10,
				20));
	}

	@Test
	public void testConstructor() {
		assertEquals(new org.jrosbridge.springed.primitives.Duration(),
				empty.getData());

		assertEquals("{\"data\":{\"secs\":0,\"nsecs\":0}}", empty.toString());

		assertEquals(1, empty.toJsonObject().size());
		assertEquals(new org.jrosbridge.springed.primitives.Duration(),
				org.jrosbridge.springed.primitives.Duration
						.fromJsonObject(empty.toJsonObject().getJsonObject(
								Duration.FIELD_DATA)));

		assertEquals(Duration.TYPE, empty.getMessageType());
	}

	@Test
	public void testDurationConstructor() {
		assertEquals(new org.jrosbridge.springed.primitives.Duration(10, 20),
				t1.getData());

		assertEquals("{\"data\":{\"secs\":10,\"nsecs\":20}}", t1.toString());

		assertEquals(1, t1.toJsonObject().size());
		assertEquals(new org.jrosbridge.springed.primitives.Duration(10, 20),
				org.jrosbridge.springed.primitives.Duration.fromJsonObject(t1
						.toJsonObject().getJsonObject(Duration.FIELD_DATA)));

		assertEquals(Duration.TYPE, t1.getMessageType());
	}

	@Test
	public void testHashCode() {
		assertEquals(empty.toString().hashCode(), empty.hashCode());
		assertEquals(t1.toString().hashCode(), t1.hashCode());
	}

	@Test
	public void testEquals() {
		assertFalse(empty.equals(t1));
		assertFalse(t1.equals(empty));

		assertTrue(empty.equals(empty));
		assertTrue(t1.equals(t1));
	}

	@Test
	public void testEqualsWrongObject() {
		assertFalse(empty.equals(new String(empty.toString())));
	}

	@Test
	public void testClone() {
		Duration clone = t1.clone();
		assertEquals(t1.toString(), clone.toString());
		assertEquals(t1.toJsonObject(), clone.toJsonObject());
		assertEquals(t1.getMessageType(), clone.getMessageType());
		assertEquals(t1.getData(), clone.getData());
		assertNotSame(t1, clone);
		assertNotSame(t1.toString(), clone.toString());
		assertNotSame(t1.toJsonObject(), clone.toJsonObject());
		assertNotSame(t1.getData(), clone.getData());
	}

	@Test
	public void testFromJsonString() {
		Duration data = Duration.fromJsonString(t1.toString());
		assertEquals(t1.toString(), data.toString());
		assertEquals(t1.toJsonObject(), data.toJsonObject());
		assertEquals(t1.getMessageType(), data.getMessageType());
		assertEquals(t1.getData(), data.getData());
		assertNotSame(t1, data);
		assertNotSame(t1.toString(), data.toString());
		assertNotSame(t1.toJsonObject(), data.toJsonObject());
	}

	@Test
	public void testFromMessage() {
		Message m = new Message(t1.toString());
		Duration data = Duration.fromMessage(m);
		assertEquals(t1.toString(), data.toString());
		assertEquals(t1.toJsonObject(), data.toJsonObject());
		assertEquals(t1.getMessageType(), data.getMessageType());
		assertEquals(t1.getData(), data.getData());
		assertNotSame(t1, data);
		assertNotSame(t1.toString(), data.toString());
		assertNotSame(t1.toJsonObject(), data.toJsonObject());
	}

	@Test
	public void testFromJsonObject() {
		JsonObject jsonObject = Json.createObjectBuilder()
				.add(Duration.FIELD_DATA, t1.getData().toJsonObject()).build();
		Duration data = Duration.fromJsonObject(jsonObject);
		assertEquals(t1.toString(), data.toString());
		assertEquals(t1.toJsonObject(), data.toJsonObject());
		assertEquals(t1.getMessageType(), data.getMessageType());
		assertEquals(t1.getData(), data.getData());
		assertNotSame(t1, data);
		assertNotSame(t1.toString(), data.toString());
		assertNotSame(t1.toJsonObject(), data.toJsonObject());
	}

	@Test
	public void testFromJsonObjectNoData() {
		JsonObject jsonObject = Json.createObjectBuilder().build();
		Duration data = Duration.fromJsonObject(jsonObject);
		assertEquals(new org.jrosbridge.springed.primitives.Duration(),
				data.getData());
	}
}
