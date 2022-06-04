//package org.jrosbridge.springed;
//
//import static org.junit.Assert.*;
//
//import javax.json.Json;
//
//
//import org.jrosbridge.springed.callback.ServiceCallback;
//import org.jrosbridge.springed.callback.TopicCallback;
//import org.jrosbridge.springed.handler.RosHandler;
//import org.jrosbridge.springed.messages.Message;
//import org.jrosbridge.springed.services.ServiceResponse;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.web.socket.WebSocketSession;
//
//@SpringBootTest
//public class TestRos {
//
//	private Ros<String> r1;
//
//	@Before
//	public void setUp() throws InterruptedException {
//		r1 = new Ros<Sting>();
//	}
//
//	@After
//	public void tearDown() {
//		DummyHandler.latest = null;
//	}
//
//
//	@Test
//	public void testNextID() {
//		for (int i = 0; i < 20; i++) {
//			assertEquals(i, r1.nextId());
//		}
//	}
//
//	@Test
//	public void testAddRosHandler() throws InterruptedException {
//		DummyRosHandler h = new DummyRosHandler();
//
//		r1.addRosHandler(h);
//		assertFalse(h.connection);
//		assertFalse(h.disconnection);
//		assertFalse(h.error);
//
//		Thread.sleep(500);
//		assertTrue(r1.isConnected());
//		assertTrue(h.connection);
//		assertFalse(h.disconnection);
//		assertFalse(h.error);
//
//		assertTrue(r1.disconnect());
//		Thread.sleep(500);
//		assertFalse(r1.isConnected());
//		assertTrue(h.connection);
//		assertTrue(h.disconnection);
//		assertFalse(h.error);
//
//		r1.handleTransportError(null, null);
//		assertTrue(h.connection);
//		assertTrue(h.disconnection);
//		assertTrue(h.error);
//	}
//
//	@Test
//	public void testSend() {
//		assertTrue(r1.send(Json.createObjectBuilder().add("test", "value")
//				.build()));
//
//		while (DummyHandler.latest == null) {
//			Thread.yield();
//		}
//
//		assertNotNull(DummyHandler.latest);
//		assertEquals(1, DummyHandler.latest.size());
//		assertTrue(DummyHandler.latest.containsKey("test"));
//		assertEquals("value", DummyHandler.latest.getString("test"));
//	}
//
//	@Test
//	public void testSendNoConnection() {
//		assertFalse(r1.send(Json.createObjectBuilder().build()));
//		assertNull(DummyHandler.latest);
//	}
//
//	@Test
//	public void testAuthenticate() {
//		r1.authenticate("test1", "test2", "test3", "test4", 5, "test5", 10);
//
//		while (DummyHandler.latest == null) {
//			Thread.yield();
//		}
//
//		assertNotNull(DummyHandler.latest);
//		assertEquals(8, DummyHandler.latest.size());
//		assertEquals(ROSConstants.OP_CODE_AUTH,
//				DummyHandler.latest.getString(ROSConstants.FIELD_OP));
//		assertEquals("test1",
//				DummyHandler.latest.getString(ROSConstants.FIELD_MAC));
//		assertEquals("test2",
//				DummyHandler.latest.getString(ROSConstants.FIELD_CLIENT));
//		assertEquals("test3",
//				DummyHandler.latest
//						.getString(ROSConstants.FIELD_DESTINATION));
//		assertEquals("test4",
//				DummyHandler.latest.getString(ROSConstants.FIELD_RAND));
//		assertEquals(5, DummyHandler.latest.getInt(ROSConstants.FIELD_TIME));
//		assertEquals("test5",
//				DummyHandler.latest.getString(ROSConstants.FIELD_LEVEL));
//		assertEquals(10,
//				DummyHandler.latest.getInt(ROSConstants.FIELD_END_TIME));
//	}
//
//	@Test
//	public void testOnMessageInvalidOpCode() {
//		r1.mess("{\"" + ROSConstants.FIELD_OP + "\":\"invalid\"}");
//		Thread.yield();
//		assertNull(DummyHandler.latest);
//	}
//
//	@Test
//	public void testOnMessageInvalidPngData() {
//		r1.onMessage("{\"" + ROSConstants.FIELD_OP + "\":\""
//				+ ROSConstants.OP_CODE_PNG + "\"}");
//		Thread.yield();
//		assertNull(DummyHandler.latest);
//
//		r1.onMessage("{\"" + ROSConstants.FIELD_OP + "\":\""
//				+ ROSConstants.OP_CODE_PNG + "\",\"" + ROSConstants.FIELD_DATA
//				+ "\":\"iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAMAAAC67D+PAAAAGXR"
//				+ "FWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAGBQTFRF///"
//				+ "/AGb/AGbMmcz/M5nMZpnM////AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
//				+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
//				+ "AAAAAAAAAAAAA7feQVwAAAAd0Uk5T////////ABpLA0YAAAA6SURBVHj"
//				+ "aJMtBDgBABARBs4P/P3kbfZCKEE3aAmUFLVu5fCQfGQ7nciTV0GW9zp4"
//				+ "Ds+B5SMcLfgEGADSKAPVZzedhAAAAAElFTkSuQmCC\"}");
//		Thread.yield();
//		assertNull(DummyHandler.latest);
//	}
//
//	@Test
//	public void testOnMessagePngData() {
//
//		DummyTopicCallback cb = new DummyTopicCallback();
//		r1.registerTopicCallback("myTopic", cb);
//		assertNull(cb.latest);
//
//		r1.onMessage("{\"" + ROSConstants.FIELD_OP + "\":\""
//				+ ROSConstants.OP_CODE_PNG + "\",\"" + ROSConstants.FIELD_DATA
//				+ "\":\"iVBORw0KGgoAAAANSUhEUgAAAAQAAAAFCAIAAADtz9qMAAAATEl"
//				+ "EQVR4nAFBAL7/AXsib/UAy7JOO0D89AL4RrO8ADpNAPQBttECrwVXKE3"
//				+ "8+vO5yQAzBFH6qccUACD2UUgPrwLHu1Ir+FK+vQoJ2ejGjx3lsrwJjwA"
//				+ "AAABJRU5ErkJggg==\"}");
//		Thread.yield();
//		assertNull(DummyHandler.latest);
//		assertNotNull(cb.latest);
//		assertEquals("{\"test1\":\"test2\"}", cb.latest.toString());
//	}
//
//	@Test
//	public void testOnMessageNoTopicCallbacks() {
//		r1.onMessage("{\"" + ROSConstants.FIELD_OP + "\":\""
//				+ ROSConstants.OP_CODE_PUBLISH + "\",\"" + ROSConstants.FIELD_TOPIC
//				+ "\":\"myTopic\",\"" + ROSConstants.FIELD_MESSAGE
//				+ "\":{\"test1\":\"test2\"}}");
//		Thread.yield();
//		assertNull(DummyHandler.latest);
//	}
//
//	@Test
//	public void testOnMessageMultiTopicCallbacks() {
//
//		DummyTopicCallback cb1 = new DummyTopicCallback();
//		DummyTopicCallback cb2 = new DummyTopicCallback();
//		r1.registerTopicCallback("myTopic", cb1);
//		r1.registerTopicCallback("myTopic", cb2);
//		assertNull(cb1.latest);
//		assertNull(cb2.latest);
//
//		r1.onMessage("{\"" + ROSConstants.FIELD_OP + "\":\""
//				+ ROSConstants.OP_CODE_PUBLISH + "\",\"" + ROSConstants.FIELD_TOPIC
//				+ "\":\"myTopic\",\"" + ROSConstants.FIELD_MESSAGE
//				+ "\":{\"test1\":\"test2\"}}");
//		Thread.yield();
//		assertNull(DummyHandler.latest);
//		assertNotNull(cb1.latest);
//		assertNotNull(cb2.latest);
//		assertEquals("{\"test1\":\"test2\"}", cb1.latest.toString());
//		assertEquals("{\"test1\":\"test2\"}", cb2.latest.toString());
//	}
//
//	@Test
//	public void testDeregisterTopicCallback() {
//
//		DummyTopicCallback cb1 = new DummyTopicCallback();
//		DummyTopicCallback cb2 = new DummyTopicCallback();
//		r1.registerTopicCallback("myTopic", cb1);
//		r1.registerTopicCallback("myTopic", cb2);
//		assertNull(cb1.latest);
//		assertNull(cb2.latest);
//		r1.deregisterTopicCallback("myTopic", cb1);
//
//		r1.onMessage("{\"" + ROSConstants.FIELD_OP + "\":\""
//				+ ROSConstants.OP_CODE_PUBLISH + "\",\"" + ROSConstants.FIELD_TOPIC
//				+ "\":\"myTopic\",\"" + ROSConstants.FIELD_MESSAGE
//				+ "\":{\"test1\":\"test2\"}}");
//		Thread.yield();
//		assertNull(DummyHandler.latest);
//		assertNull(cb1.latest);
//		assertNotNull(cb2.latest);
//		assertEquals("{\"test1\":\"test2\"}", cb2.latest.toString());
//	}
//
//	@Test
//	public void testDeregisterTopicCallbackAll() {
//
//		DummyTopicCallback cb1 = new DummyTopicCallback();
//		DummyTopicCallback cb2 = new DummyTopicCallback();
//		r1.registerTopicCallback("myTopic", cb1);
//		r1.registerTopicCallback("myTopic", cb2);
//		assertNull(cb1.latest);
//		assertNull(cb2.latest);
//		r1.deregisterTopicCallback("myTopic", cb1);
//		r1.deregisterTopicCallback("myTopic", cb2);
//
//		r1.onMessage("{\"" + ROSConstants.FIELD_OP + "\":\""
//				+ ROSConstants.OP_CODE_PUBLISH + "\",\"" + ROSConstants.FIELD_TOPIC
//				+ "\":\"myTopic\",\"" + ROSConstants.FIELD_MESSAGE
//				+ "\":{\"test1\":\"test2\"}}");
//		Thread.yield();
//		assertNull(DummyHandler.latest);
//		assertNull(cb1.latest);
//		assertNull(cb2.latest);
//	}
//
//	@Test
//	public void testDeregisterTopicCallbackInvalidTopic() {
//
//		DummyTopicCallback cb1 = new DummyTopicCallback();
//		DummyTopicCallback cb2 = new DummyTopicCallback();
//		r1.registerTopicCallback("myTopic", cb1);
//		r1.registerTopicCallback("myTopic", cb2);
//		assertNull(cb1.latest);
//		assertNull(cb2.latest);
//		r1.deregisterTopicCallback("myTopic2", cb1);
//		r1.deregisterTopicCallback("myTopic2", cb2);
//
//		r1.onMessage("{\"" + ROSConstants.FIELD_OP + "\":\""
//				+ ROSConstants.OP_CODE_PUBLISH + "\",\"" + ROSConstants.FIELD_TOPIC
//				+ "\":\"myTopic\",\"" + ROSConstants.FIELD_MESSAGE
//				+ "\":{\"test1\":\"test2\"}}");
//		Thread.yield();
//		assertNull(DummyHandler.latest);
//		assertNotNull(cb1.latest);
//		assertNotNull(cb2.latest);
//		assertEquals("{\"test1\":\"test2\"}", cb1.latest.toString());
//		assertEquals("{\"test1\":\"test2\"}", cb2.latest.toString());
//	}
//
//	@Test
//	public void testDeregisterTopicCallbackInvalidCallback() {
//
//		DummyTopicCallback cb1 = new DummyTopicCallback();
//		DummyTopicCallback cb2 = new DummyTopicCallback();
//		r1.registerTopicCallback("myTopic", cb1);
//		r1.registerTopicCallback("myTopic", cb2);
//		assertNull(cb1.latest);
//		assertNull(cb2.latest);
//		r1.deregisterTopicCallback("myTopic", new DummyTopicCallback());
//
//		r1.onMessage("{\"" + ROSConstants.FIELD_OP + "\":\""
//				+ ROSConstants.OP_CODE_PUBLISH + "\",\"" + ROSConstants.FIELD_TOPIC
//				+ "\":\"myTopic\",\"" + ROSConstants.FIELD_MESSAGE
//				+ "\":{\"test1\":\"test2\"}}");
//		Thread.yield();
//		assertNull(DummyHandler.latest);
//		assertNotNull(cb1.latest);
//		assertNotNull(cb2.latest);
//		assertEquals("{\"test1\":\"test2\"}", cb1.latest.toString());
//		assertEquals("{\"test1\":\"test2\"}", cb2.latest.toString());
//	}
//
//	@Test
//	public void testOnMessageNoServiceCallbacks() {
//		r1.onMessage("{\"" + ROSConstants.FIELD_OP + "\":\""
//				+ ROSConstants.OP_CODE_SERVICE_RESPONSE + "\",\""
//				+ ROSConstants.FIELD_ID + "\":\"id123\",\""
//				+ ROSConstants.FIELD_RESULT + "\":true,\""
//				+ ROSConstants.FIELD_MESSAGE + "\":{\"test1\":\"test2\"}}");
//		Thread.yield();
//		assertNull(DummyHandler.latest);
//	}
//
//	@Test
//	public void testOnMessageServiceCallback() {
//
//		DummyServiceCallback cb1 = new DummyServiceCallback();
//		r1.registerServiceCallback("id123", cb1);
//		assertNull(cb1.latest);
//
//		r1.onMessage("{\"" + ROSConstants.FIELD_OP + "\":\""
//				+ ROSConstants.OP_CODE_SERVICE_RESPONSE + "\",\""
//				+ ROSConstants.FIELD_ID + "\":\"id123\",\""
//				+ ROSConstants.FIELD_RESULT + "\":false,\""
//				+ ROSConstants.FIELD_VALUES + "\":{\"test1\":\"test2\"}}");
//		Thread.yield();
//		assertNull(DummyHandler.latest);
//		assertNotNull(cb1.latest);
//		assertEquals("{\"test1\":\"test2\"}", cb1.latest.toString());
//		assertFalse(cb1.latest.getResult());
//	}
//
//	@Test
//	public void testOnMessageServiceCallbackNoResult() {
//
//		DummyServiceCallback cb1 = new DummyServiceCallback();
//		r1.registerServiceCallback("id123", cb1);
//		assertNull(cb1.latest);
//
//		r1.onMessage("{\"" + ROSConstants.FIELD_OP + "\":\""
//				+ ROSConstants.OP_CODE_SERVICE_RESPONSE + "\",\""
//				+ ROSConstants.FIELD_ID + "\":\"id123\",\""
//				+ ROSConstants.FIELD_VALUES + "\":{\"test1\":\"test2\"}}");
//		Thread.yield();
//		assertNull(DummyHandler.latest);
//		assertNotNull(cb1.latest);
//		assertEquals("{\"test1\":\"test2\"}", cb1.latest.toString());
//		assertTrue(cb1.latest.getResult());
//	}
//
//	private static class DummyRosHandler implements RosHandler {
//
//		public boolean connection, disconnection, error;
//
//		public void handleConnection(WebSocketSession session) {
//			this.connection = true;
//		}
//
//		public void handleDisconnection(WebSocketSession session) {
//			this.disconnection = true;
//		}
//
//		public void handleError(WebSocketSession session, Throwable t) {
//			this.error = true;
//		}
//	}
//
//	private static class DummyTopicCallback implements TopicCallback {
//
//		public Message latest = null;
//
//		public void handleMessage(Message message) {
//			latest = message;
//		}
//	}
//
//	private static class DummyServiceCallback implements ServiceCallback {
//
//		public ServiceResponse latest = null;
//
//		public void handleServiceResponse(ServiceResponse response) {
//			this.latest = response;
//		}
//	}
//}
