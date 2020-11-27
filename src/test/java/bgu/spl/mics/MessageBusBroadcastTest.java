package bgu.spl.mics;

import org.junit.jupiter.api.*;
import  bgu.spl.mics.application.messages.*;
import  bgu.spl.mics.application.services.*;
import static org.junit.jupiter.api.Assertions.*;


public class MessageBusBroadcastTest {
    TestBCastEvent1 be1;
    TestBCastEvent2 be2;
    TestMic1 bm1;
    TestMic1 bm3;
    TestMic2 bm2;
    MessageBus messageBus;

    @BeforeAll
    public void setUp(){
        messageBus = MessageBusImpl.getInstance();
    }

    @BeforeEach
    public void preTest(){
        be1 = new TestBCastEvent1();
        be2 = new TestBCastEvent2();
        bm1 = new TestMic1();
        bm2 = new TestMic2();
        messageBus.register(bm1);
        messageBus.register(bm2);
    }

    @Test
    public void testBSingleton(){
        // test singleton
        MessageBus messageBusOther = MessageBusImpl.getInstance(); // assign a message bus to a different reference
        assertSame(messageBus, messageBusOther, "Not the same instance"); // check if both are the same instance
    }
    @Test
    public void testBCastSendAndReceive() {
        // tests if subscribers receive the right messages
        messageBus.subscribeBroadcast(be1.getClass(), bm1); // bm1 subscribes to be1
        messageBus.sendBroadcast(be1); // a be1 broadcast is sent to all subscribers
        Message b = null;
        try {
            //attempt to retrieve a message from bm1's queue
            b = messageBus.awaitMessage(bm1);
        }
        catch (IllegalStateException | InterruptedException e){ // if bm1 is unregistered or interrupted exception is thrown
            fail("Exception thrown: " + e + "\nPossibly because of unregistered micro-service or test interruption");
        }
        assertEquals(b ,be1, "Expected message type: " + be1.getClass() + "Actual: " + b.getClass());
    }
    @Test
    public void testBCastMultiSubAndNoneSub(){
        // tests if multi-subscribers (more than one subscription) get correct ones, and that non-subscribers are excluded
        messageBus.subscribeBroadcast(be1.getClass(), bm1); // bm1 subscribes to be1
        messageBus.subscribeBroadcast(be1.getClass(), bm2); // bm2 subscribes to be1
        messageBus.subscribeBroadcast(be2.getClass(), bm2); // bm2 subscribes to be2 - now bm1 is subscribed to be1 and bm2 to be1 and be2
        messageBus.sendBroadcast(be2);
        messageBus.sendBroadcast(be1);
        Message a = null;
        Message b = null;
        // in the first inspection, we check that both messages pulled from the queues of bm1, and bm2 are different events
        try {
            a = messageBus.awaitMessage(bm1);
            b = messageBus.awaitMessage(bm2);
        }
        catch (IllegalStateException | InterruptedException e){
            fail("Exception thrown: " + e + "\nPossibly because of unregistered micro-service or test interruption");
        }
        // expecting to compare a message of type be1 and be2
        assertNotEquals(a ,b);
        // in the second inspection bm1 should have be1 only in its queue and bm2 should have be2,be1
        try {
            // pull the second message from bm2's queue - a be2 type message
            b = messageBus.awaitMessage(bm2);
        }
        catch (IllegalStateException | InterruptedException e){
            fail("Exception thrown: " + e + "\nPossibly because of unregistered micro-service or test interruption");
        }
        // now bm2 should have only be1 in its queue as well
        assertEquals(a ,b);


    }
    @Test
    public void testBCastUnregisteredAwait() {
        //tests that an unregistered waiter throws an exception
        messageBus.unregister(bm1); // bm1 was registered ot the message bus and now is removed
        try{
            messageBus.awaitMessage(bm1);
        }
        catch (IllegalStateException e) {
            // test passes if the correct exception is thrown
            assertTrue(true);
        }
        catch (Exception e){
        fail("Expecting IllegalStateException and got: " + e);

        }
        fail("Excpected IllegalStateException and got no exception");
    }
    @Test
    public void testBCastNoSubscribers(){
        // tests that a message that was sent while no one was subscribing to it is deleted (no one receives it after subscribing)
        messageBus.sendBroadcast(be1); // send a be1 type broadcast while no one is subscribed to it
        messageBus.subscribeBroadcast(be1.getClass(),bm1); // bm1 subscribes to be1
        messageBus.subscribeBroadcast(be2.getClass(),bm1); // bm1 subscribes to be2
        messageBus.sendBroadcast(be2); // a be2 is broadcasted
        Message a = null;
        // inspects bm1's queue for messages - expecting it only to contain the be2 message
        try{
            a = messageBus.awaitMessage(bm1);
        }
        catch (IllegalStateException | InterruptedException e){
            fail("Exception thrown: " + e);
        }
        // checks if the message received is the first one sent or the second, if got the second one - that means first message was not saved
        assertEquals(a, be2, "Expected message type: " + be1.getClass() + "Actual: " + a.getClass());

    }
    @AfterEach
    public void tearDown(){
        messageBus.unregister(bm1);
        messageBus.unregister(bm2);
    }


}
