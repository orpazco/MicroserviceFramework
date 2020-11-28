package bgu.spl.mics;

import bgu.spl.mics.application.messages.TestEvent;
import bgu.spl.mics.application.services.TestMic1;
import bgu.spl.mics.application.services.TestMic2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MessageBusTest {

    private TestMic1 service1;
    private TestMic2 service2;
    private TestEvent testEvent1;
    private TestEvent testEvent2;
    private MessageBusImpl messageBus;

    @BeforeEach
    public void setUp(){
        service1 = new TestMic1();
        service2 = new TestMic2();
        testEvent1 = new TestEvent();
        testEvent2 = new TestEvent();
        messageBus = new MessageBusImpl();

        messageBus.register(service1);
        messageBus.register(service2);
    }

    /**
     * in this test scenario service 1 register & subscribe to TestEvent, then a TestEvent is sent
     * and service 1 await for this event to return
     */
    @Test
    public void testSubscribeEventAndSend(){
        // Check if message bus sends the events to the subscribers
        messageBus.subscribeEvent(TestEvent.class, service1);
        messageBus.sendEvent(testEvent1);
        try {
            Message message = messageBus.awaitMessage(service1);
            assertEquals(testEvent1, message, "received unexpected event from messageBus await");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSendEvent2MS(){
        // Check if the message bus sends the events to all services that subscribes to this type of event
        messageBus.subscribeEvent(TestEvent.class, service1);
        messageBus.subscribeEvent(TestEvent.class, service2);
        messageBus.sendEvent(testEvent1);
        messageBus.sendEvent(testEvent2);
        try {
            Message message1 = messageBus.awaitMessage(service1);
            Message message2 = messageBus.awaitMessage(service1);
            assertEquals(testEvent1, message1, "received unexpected event from messageBus to service 1");
            assertEquals(testEvent2, message2, "received unexpected event from messageBus to service 2");
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testAwaitMessage(){//?
        messageBus.subscribeEvent(TestEvent.class, service1);
        messageBus.sendEvent(testEvent1);
        try {
            Message message1 = messageBus.awaitMessage(service1);
            assertEquals(testEvent1, message1, "received unexpected event from messageBus await");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCompleteEvent(){
        // Check is the message bud updated the event result in the corresponding future object
        String expectedRes = "some result";
        messageBus.subscribeEvent(TestEvent.class, service1);
        Future<String> result = messageBus.sendEvent(testEvent1);
        messageBus.complete(testEvent1, expectedRes);
        assertEquals(expectedRes, result.get(), "didn't received the expected result from the complete event");
    }

    @Test
    public void testCompleteMoreThanOneEvent() {
        // Check is the message bud updated each event result in the corresponding future object
        String expectedRes1 = "some result event 1";
        String expectedRes2 = "some different result event 2";
        messageBus.subscribeEvent(TestEvent.class, service1);
        messageBus.subscribeEvent(TestEvent.class, service2);

        // send two events and complete them with a different result
        Future<String> resultEvent1 = messageBus.sendEvent(testEvent1);
        Future<String> resultEvent2 = messageBus.sendEvent(testEvent2);
        messageBus.complete(testEvent1, expectedRes1);
        messageBus.complete(testEvent2, expectedRes2);

        // check if each future updated according to the right event
        assertEquals(expectedRes1, resultEvent1.get(), "didn't received the expected result. expected: " + expectedRes1 + ", actual: " + resultEvent1.get());
        assertEquals(expectedRes2, resultEvent2.get(), "didn't received the expected result. expected: " + expectedRes2 + ", actual: " + resultEvent2.get());
    }

    @Test
    public void testUnregister(){
        // subscribe to test event and than unregister from message bus and send test event
        messageBus.subscribeEvent(TestEvent.class, service1);
        messageBus.unregister(service1);
        messageBus.sendEvent(testEvent1);

        // reregister and subscribe and send another test event
        messageBus.register(service1);
        messageBus.subscribeEvent(TestEvent.class, service1);
        messageBus.sendEvent(testEvent2);

        // check if the message bus sends the last events that was send after the service subscribe again
        try {
            Message message = messageBus.awaitMessage(service2);
            assertEquals(testEvent2, message, "received unexpected event from messageBus");
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testRoundRobin(){
        // send 3 events to message bus and check if the services received the messages by round robin
        messageBus.subscribeEvent(TestEvent.class, service1);
        messageBus.subscribeEvent(TestEvent.class, service2);
        TestEvent testEvent3 = new TestEvent();
        messageBus.sendEvent(testEvent1);
        messageBus.sendEvent(testEvent2);
        messageBus.sendEvent(testEvent3);
        try {
            Message message2 = messageBus.awaitMessage(service2);
            Message message1 = messageBus.awaitMessage(service1);
            Message message3 = messageBus.awaitMessage(service1);
            assertEquals(testEvent1, message1, "received unexpected event from messageBus");
            assertEquals(testEvent2, message2, "received unexpected event from messageBus");
            assertEquals(testEvent3, message3, "received unexpected event from messageBus");
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

}
