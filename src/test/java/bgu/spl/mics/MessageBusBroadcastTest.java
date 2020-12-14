package bgu.spl.mics;

import org.junit.jupiter.api.*;
import  bgu.spl.mics.application.messages.*;
import  bgu.spl.mics.application.services.*;
import static org.junit.jupiter.api.Assertions.*;


public class MessageBusBroadcastTest {
    private TestBCastEvent1 bCastEvent1;
    private TestBCastEvent2 bCastEvent2;
    private TestMic1 bCastMic1;
    private TestMic2 bCastMic2;
    private static MessageBus messageBus;

    @BeforeAll
    public static void setUp(){
        messageBus = MessageBusImpl.getInstance();
    }

    @BeforeEach
    public void preTest(){
        bCastEvent1 = new TestBCastEvent1();
        bCastEvent2 = new TestBCastEvent2();
        bCastMic1 = new TestMic1();
        bCastMic2 = new TestMic2();
        messageBus.register(bCastMic1);
        messageBus.register(bCastMic2);
    }

    @Test
    public void testBCastSendAndReceive() {
        // tests if subscribers receive the right messages
        messageBus.subscribeBroadcast(bCastEvent1.getClass(), bCastMic1); // bcastmic1 subscribes to bcastevent1
        messageBus.sendBroadcast(bCastEvent1); // a bcastevent1 broadcast is sent to all subscribers
        Message b = null;
        try {
            //attempt to retrieve a message from bm1's queue
            b = messageBus.awaitMessage(bCastMic1);
            assertEquals(b , bCastEvent1, "Expected message type: " + bCastEvent1.getClass() + "Actual: " + b.getClass());
        }
        catch (IllegalStateException | InterruptedException e){ // if bm1 is unregistered or interrupted exception is thrown
            fail("Exception thrown: " + e.getMessage() + "\nPossibly because of unregistered micro-service or test interruption");
        }
    }

    @Test
    public void testBCastmult(){
        // test content duplication - multiple subscribers get the same message
        messageBus.subscribeBroadcast(bCastEvent1.getClass(), bCastMic1); // bcastmic1 subscribes to bcastevent1
        messageBus.subscribeBroadcast(bCastEvent1.getClass(), bCastMic2); // bcastmic2 subscribes to bcastevent1
        messageBus.sendBroadcast(bCastEvent1); // a bcastevent1 broadcast is sent to all subscribers
        Message a = null;
        Message b = null;
        try {
            //attempt to retrieve a message from both bm's queue
            a = messageBus.awaitMessage(bCastMic2);
            b = messageBus.awaitMessage(bCastMic1);
            assertEquals(b , bCastEvent1, "Expected message type: " + bCastEvent1.getClass() + "Actual: " + b.getClass());
            assertEquals(a , bCastEvent1, "Expected message type: " + bCastEvent1.getClass() + "Actual: " + a.getClass());
        }
        catch (IllegalStateException | InterruptedException e){ // if bm1 is unregistered or interrupted exception is thrown
            fail("Exception thrown: " + e.getMessage() + "\nPossibly because of unregistered micro-service or test interruption");
        }
    }

    @Test
    public void testBCastMultiSubAndNoneSub(){
        // tests if multi-subscribers (more than one subscription) get correct ones, and that non-subscribers are excluded
        messageBus.subscribeBroadcast(bCastEvent1.getClass(), bCastMic1); // bm1 subscribes to be1
        messageBus.subscribeBroadcast(bCastEvent1.getClass(), bCastMic2); // bm2 subscribes to be1
        messageBus.subscribeBroadcast(bCastEvent2.getClass(), bCastMic2); // bm2 subscribes to be2 - now bm1 is subscribed to be1 and bm2 to be1 and be2
        messageBus.sendBroadcast(bCastEvent2);
        messageBus.sendBroadcast(bCastEvent1);
        Message a = null;
        Message b = null;
        // in the first inspection, we check that both messages pulled from the queues of bm1, and bm2 are different events
        try {
            a = messageBus.awaitMessage(bCastMic1);
            b = messageBus.awaitMessage(bCastMic2);
        }
        catch (IllegalStateException | InterruptedException e){
            fail("Exception thrown: " + e.getMessage() + "\nPossibly because of unregistered micro-service or test interruption");
        }
        // expecting to compare a message of type be1 and be2
        assertNotEquals(a ,b);
        // in the second inspection bm1 should have be1 only in its queue and bm2 should have be2,be1
        try {
            // pull the second message from bm2's queue - a be2 type message
            b = messageBus.awaitMessage(bCastMic2);
            // now bm2 should have only be1 in its queue as well
            assertEquals(a ,b);
        }
        catch (IllegalStateException | InterruptedException e){
            fail("Exception thrown: " + e.getMessage() + "\nPossibly because of unregistered micro-service or test interruption");
        }
    }

    @Test
    public void testBCastUnregisteredAwait() {
        //tests that an unregistered waiter throws an exception
        messageBus.unregister(bCastMic1); // bm1 was registered ot the message bus and now is removed
        try {
            messageBus.awaitMessage(bCastMic1);
            fail("Excpected IllegalStateException and got no exception");
        } catch (IllegalStateException e) {
            // test passes if the correct exception is thrown
            assertTrue(true);
        } catch (Exception e) {
            fail("Expecting IllegalStateException and got: " + e.getMessage());
        }
    }

    @Test
    public void testBCastNoSubscribers(){
        // tests that a message that was sent while no one was subscribing to it is deleted (no one receives it after subscribing)
        messageBus.sendBroadcast(bCastEvent1); // send a be1 type broadcast while no one is subscribed to it
        messageBus.subscribeBroadcast(bCastEvent1.getClass(), bCastMic1); // bm1 subscribes to be1
        messageBus.subscribeBroadcast(bCastEvent2.getClass(), bCastMic1); // bm1 subscribes to be2
        messageBus.sendBroadcast(bCastEvent2); // a be2 is broadcasted
        Message a = null;
        // inspects bm1's queue for messages - expecting it only to contain the be2 message
        try{
            a = messageBus.awaitMessage(bCastMic1);
            // checks if the message received is the first one sent or the second, if got the second one - that means first message was not saved
            assertEquals(a, bCastEvent2, "Expected message type: " + bCastEvent1.getClass() + "Actual: " + a.getClass());
        }
        catch (IllegalStateException | InterruptedException e){
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown(){
        messageBus.unregister(bCastMic1);
        messageBus.unregister(bCastMic2);
    }
}
