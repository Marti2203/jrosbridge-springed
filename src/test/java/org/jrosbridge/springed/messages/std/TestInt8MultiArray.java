package org.jrosbridge.springed.messages.std;

import static org.junit.Assert.*;

import java.util.Arrays;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;

import org.jrosbridge.springed.messages.Message;

public class TestInt8MultiArray {

	private Int8MultiArray empty, a1;

	@Before
	public void setUp() {
		empty = new Int8MultiArray();
		a1 = new Int8MultiArray(new MultiArrayLayout(new MultiArrayDimension[] {
				new MultiArrayDimension("test", 10, 20),
				new MultiArrayDimension("test2", 30, 40) }, 50), new byte[] {
				5, 10 });
	}

	@Test
	public void testConstructor() {
		assertEquals(new MultiArrayLayout(), empty.getLayout());
		assertTrue(Arrays.equals(new byte[0], empty.getData()));
		assertEquals(0, empty.getData().length);
		assertEquals(0, empty.size());

		assertEquals("{\"layout\":{\"dim\":[],\"data_offset\":0},\"data\":[]}",
				empty.toString());

		assertEquals(2, empty.toJsonObject().size());
		assertEquals(new MultiArrayLayout(),
				MultiArrayLayout.fromJsonObject(empty.toJsonObject()
						.getJsonObject(Int8MultiArray.FIELD_LAYOUT)));
		assertEquals(0,
				empty.toJsonObject().getJsonArray(Int8MultiArray.FIELD_DATA)
						.size());

		assertEquals(Int8MultiArray.TYPE, empty.getMessageType());
	}

	@Test
	public void testMultiArrayLayoutAndByteArrayConstructor() {
		assertEquals(new MultiArrayLayout(new MultiArrayDimension[] {
				new MultiArrayDimension("test", 10, 20),
				new MultiArrayDimension("test2", 30, 40) }, 50), a1.getLayout());
		assertTrue(Arrays.equals(new byte[] { 5, 10 }, a1.getData()));
		assertEquals(2, a1.size());
		assertEquals((byte) 5, a1.get(0));
		assertEquals((byte) 10, a1.get(1));

		assertEquals("{\"layout\":{\"dim\":["
				+ "{\"label\":\"test\",\"size\":10,\"stride\":20},"
				+ "{\"label\":\"test2\",\"size\":30,\"stride\":40}"
				+ "],\"data_offset\":50},\"data\":[5,10]}", a1.toString());

		assertEquals(2, a1.toJsonObject().size());
		assertEquals(new MultiArrayLayout(new MultiArrayDimension[] {
				new MultiArrayDimension("test", 10, 20),
				new MultiArrayDimension("test2", 30, 40) }, 50),
				MultiArrayLayout.fromJsonObject(a1.toJsonObject()
						.getJsonObject(Int8MultiArray.FIELD_LAYOUT)));
		assertEquals(2,
				a1.toJsonObject().getJsonArray(Int8MultiArray.FIELD_DATA)
						.size());
		assertEquals((byte) 5,
				(byte) a1.toJsonObject()
						.getJsonArray(Int8MultiArray.FIELD_DATA).getInt(0));
		assertEquals((byte) 10,
				(byte) a1.toJsonObject()
						.getJsonArray(Int8MultiArray.FIELD_DATA).getInt(1));

		assertEquals(Int8MultiArray.TYPE, a1.getMessageType());
	}

	@Test
	public void testSetMessageType() {
		empty.setMessageType("test");
		assertEquals("test", empty.getMessageType());
	}

	@Test
	public void testHashCode() {
		assertEquals(empty.toString().hashCode(), empty.hashCode());
		assertEquals(a1.toString().hashCode(), a1.hashCode());
	}

	@Test
	public void testEquals() {
		assertFalse(empty.equals(a1));
		assertFalse(a1.equals(empty));

		assertTrue(empty.equals(empty));
		assertTrue(a1.equals(a1));
	}

	@Test
	public void testEqualsWrongObject() {
		assertFalse(empty.equals(new String(empty.toString())));
	}

	@Test
	public void testClone() {
		Int8MultiArray clone = a1.clone();
		assertEquals(a1.toString(), clone.toString());
		assertEquals(a1.toJsonObject(), clone.toJsonObject());
		assertEquals(a1.getMessageType(), clone.getMessageType());
		assertTrue(Arrays.equals(a1.getData(), clone.getData()));
		assertEquals(a1.getLayout(), clone.getLayout());
		assertEquals(a1.size(), clone.size());
		assertNotSame(a1, clone);
		assertNotSame(a1.toString(), clone.toString());
		assertNotSame(a1.toJsonObject(), clone.toJsonObject());
		assertNotSame(a1.getData(), clone.getData());
	}

	@Test
	public void testFromJsonString() {
		Int8MultiArray p = Int8MultiArray.fromJsonString(a1.toString());
		assertEquals(a1.toString(), p.toString());
		assertEquals(a1.toJsonObject(), p.toJsonObject());
		assertEquals(a1.getMessageType(), p.getMessageType());
		assertTrue(Arrays.equals(a1.getData(), p.getData()));
		assertEquals(a1.getLayout(), p.getLayout());
		assertNotSame(a1, p);
		assertNotSame(a1.toString(), p.toString());
		assertNotSame(a1.toJsonObject(), p.toJsonObject());
		assertEquals(a1.size(), p.size());
	}

	@Test
	public void testFromMessage() {
		Message m = new Message(a1.toString());
		Int8MultiArray p = Int8MultiArray.fromMessage(m);
		assertEquals(a1.toString(), p.toString());
		assertEquals(a1.toJsonObject(), p.toJsonObject());
		assertEquals(a1.getMessageType(), p.getMessageType());
		assertEquals(a1.size(), p.size());
		assertTrue(Arrays.equals(a1.getData(), p.getData()));
		assertEquals(a1.getLayout(), p.getLayout());
		assertNotSame(a1, p);
		assertNotSame(a1.toString(), p.toString());
		assertNotSame(a1.toJsonObject(), p.toJsonObject());
	}

	@Test
	public void testFromJsonObject() {
		JsonArrayBuilder array = Json.createArrayBuilder();
		for (byte d : a1.getData()) {
			array.add(d);
		}
		JsonObject jsonObject = Json
				.createObjectBuilder()
				.add(Int8MultiArray.FIELD_DATA, array.build())
				.add(Int8MultiArray.FIELD_LAYOUT, a1.getLayout().toJsonObject())
				.build();
		Int8MultiArray p = Int8MultiArray.fromJsonObject(jsonObject);
		assertEquals(a1.toString(), p.toString());
		assertEquals(a1.toJsonObject(), p.toJsonObject());
		assertEquals(a1.getMessageType(), p.getMessageType());
		assertTrue(Arrays.equals(a1.getData(), p.getData()));
		assertEquals(a1.getLayout(), p.getLayout());
		assertNotSame(a1, p);
		assertNotSame(a1.toString(), p.toString());
		assertNotSame(a1.toJsonObject(), p.toJsonObject());
		assertEquals(a1.size(), p.size());
	}

	@Test
	public void testFromJsonObjectNoData() {
		JsonObject jsonObject = Json
				.createObjectBuilder()
				.add(Int8MultiArray.FIELD_LAYOUT, a1.getLayout().toJsonObject())
				.build();
		Int8MultiArray p = Int8MultiArray.fromJsonObject(jsonObject);
		assertTrue(Arrays.equals(new byte[0], p.getData()));
		assertEquals(0, p.size());
		assertEquals(a1.getLayout(), p.getLayout());
	}

	@Test
	public void testFromJsonObjectNoLayout() {
		JsonArrayBuilder array = Json.createArrayBuilder();
		for (byte d : a1.getData()) {
			array.add(d);
		}
		JsonObject jsonObject = Json.createObjectBuilder()
				.add(Int8MultiArray.FIELD_DATA, array.build()).build();
		Int8MultiArray p = Int8MultiArray.fromJsonObject(jsonObject);
		assertTrue(Arrays.equals(a1.getData(), p.getData()));
		assertEquals(a1.size(), p.size());
		assertEquals(new MultiArrayLayout(), p.getLayout());
	}
}
