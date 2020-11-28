package bgu.spl.mics;

import bgu.spl.mics.application.messages.TestEvent;
import bgu.spl.mics.application.services.TestMic1;
import bgu.spl.mics.application.services.TestMic2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MessageBusTest {

    private TestMic1 service1;
    private TestMic2 service2;
    private TestEvent testEvent1;
    private TestEvent testEvent2;
    private static MessageBusImpl messageBus;

    @BeforeAll
    public static void setUp(){
        messageBus = MessageBusImpl.getInstance();
    }

    @BeforeEach
    public void preTest(){
        service1 = new TestMic1();
        service2 = new TestMic2();
        testEvent1 = new TestEvent();
        testEvent2 = new TestEvent();

        messageBus.register(service1);
        messageBus.register(service2);
    }

    @Test
    public void testBSingleton(){
        // test singleton
        MessageBus messageBusOther = MessageBusImpl.getInstance(); // assign a message bus to a different reference
        assertSame(messageBus, messageBusOther, "Not the same instance"); // check if both are the same instance
    }

    @Test
    public void testSubscribeSendAwait(){
        // Check if message bus sends the events to the subscribers, and check if the await function returns the
        // sent event
        messageBus.subscribeEvent(TestEvent.class, service1);
        messageBus.sendEvent(testEvent1);
        try {
            Message message = messageBus.awaitMessage(service1);
            assertEquals(testEvent1, message, "received unexpected event from messageBus await");
        } catch (IllegalStateException | InterruptedException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSend2EventsSameMS(){
        // Check if the message bus sends the events to the service that subscribes to this type of event
        messageBus.subscribeEvent(TestEvent.class, service1);
        messageBus.sendEvent(testEvent1);
        messageBus.sendEvent(testEvent2);
        try {
            Message message1 = messageBus.awaitMessage(service1);
            Message message2 = messageBus.awaitMessage(service1);
            assertEquals(testEvent1, message1, "received unexpected event from messageBus to service");
            assertEquals(testEvent2, message2, "received unexpected event from messageBus to service");
        } catch (IllegalStateException | InterruptedException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCompleteEvent(){
        // Check if the message bus updated the event result in the corresponding future object
        String expectedRes = "some result";
        messageBus.subscribeEvent(TestEvent.class, service1);
        Future<String> result = messageBus.sendEvent(testEvent1);
        messageBus.complete(testEvent1, expectedRes);
        assertEquals(expectedRes, result.get(), "didn't receive the expected result from the complete event");
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
        assertEquals(expectedRes1, resultEvent1.get(), "didn't receive the expected result. expected: " + expectedRes1 + ", actual: " + resultEvent1.get());
        assertEquals(expectedRes2, resultEvent2.get(), "didn't receive the expected result. expected: " + expectedRes2 + ", actual: " + resultEvent2.get());
    }

    @Test
    public void testUnregister(){
        // subscribe to test event and than unregister from message bus and send test event
        messageBus.subscribeEvent(TestEvent.class, service1);
        messageBus.unregister(service1);
        messageBus.sendEvent(testEvent1);

        // register and subscribe again, and send another test event
        messageBus.register(service1);
        messageBus.subscribeEvent(TestEvent.class, service1);
        messageBus.sendEvent(testEvent2);

        // check if the message bus sends the last event that was sent after the service subscribed again
        try {
            Message message = messageBus.awaitMessage(service2);
            assertEquals(testEvent2, message, "received unexpected event from messageBus");
        } catch (IllegalStateException | InterruptedException e) {
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
            Message message1 = messageBus.awaitMessage(service1);
            Message message2 = messageBus.awaitMessage(service2);
            Message message3 = messageBus.awaitMessage(service1);
            assertEquals(testEvent1, message1, "received unexpected event 1 from messageBus");
            assertEquals(testEvent2, message2, "received unexpected event 2 from messageBus");
            assertEquals(testEvent3, message3, "received unexpected event 3 from messageBus");
        } catch (IllegalStateException | InterruptedException e) {
            fail(e.getMessage());
        }
    }

    @AfterEach
    public void tearDown(){
        messageBus.unregister(service1);
        messageBus.unregister(service2);
    }

}
