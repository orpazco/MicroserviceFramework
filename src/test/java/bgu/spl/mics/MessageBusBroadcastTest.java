package bgu.spl.mics;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import  bgu.spl.mics.application.messages.*;
import  bgu.spl.mics.application.services.*;
import static org.junit.jupiter.api.Assertions.*;


public class MessageBusBroadcastTest {
    dummyBroadcastEvent1 be1;
    dummyBroadcastEvent2 be2;
    dummyBCastMicroservice1 bm1;
    dummyBCastMicroservice1 bm3;
    dummyBCastMicroservice2 bm2;
    MessageBus messageBus;

    @BeforeEach
    public void Setup(){
        be1 = new dummyBroadcastEvent1();
        be2 = new dummyBroadcastEvent2();
        bm1 = new dummyBCastMicroservice1();
        bm2 = new dummyBCastMicroservice2();
        bm3 = new dummyBCastMicroservice1(); //sender
        messageBus = new MessageBusImpl();
        messageBus.register(bm1);
        messageBus.register(bm2);
        messageBus.register(bm3);
    }

    @Test
    public void testBSingleton(){
    // test singleton
    }

    public void testBCastSendAndReceive() {
        // tests if subscribers receive the right messages
        messageBus.subscribeBroadcast(be1.getClass(), bm1);
        messageBus.sendBroadcast(be1);
        Message b = null;
        try {
            b = messageBus.awaitMessage(bm1);
        }
        catch (IllegalStateException | InterruptedException e){
            // do something
        }
        assertEquals(b ,be1 );
    }
    public void testBCastMultiSubAndNoneSub(){
        // tests if multisubscribers (more than one subscription) get correct ones, and that non-subscribers are excluded
        messageBus.subscribeBroadcast(be1.getClass(), bm1);
        messageBus.subscribeBroadcast(be1.getClass(), bm2);
        messageBus.subscribeBroadcast(be2.getClass(), bm2);
        messageBus.sendBroadcast(be2);
        messageBus.sendBroadcast(be1);
        Message a = null;
        Message b = null;
        try {
            a = messageBus.awaitMessage(bm1);
            b = messageBus.awaitMessage(bm2);
        }
        catch (IllegalStateException | InterruptedException e){
            // do something
        }
        assertNotEquals(a ,b ); //bm1 should have be1 only in its queue and bm2 should have be2,be1
        try {
            b = messageBus.awaitMessage(bm2);
        }
        catch (IllegalStateException | InterruptedException e){
            // do something
        }
        assertEquals(a ,b ); // now bm2 should have only be1 in its queue as well


    }

    public void testBCastUnregisteredAweait() throws InterruptedException {
        //tests that an unregistered waiter throws an exception
        messageBus.unregister(bm1);
        try{
            messageBus.awaitMessage(bm1);
        }
        catch (IllegalStateException e) {
            assert true;
        }
        assert false;
    }

    public void testBcastNoSubscribers(){
        // tests that a message that was sent while no one was subscribing to it is deleted (no one receives it after subscribing)
        messageBus.sendBroadcast(be1);
        messageBus.subscribeBroadcast(be1.getClass(),bm1);
        messageBus.subscribeBroadcast(be2.getClass(),bm1);
        messageBus.sendBroadcast(be2);
        Message a = null;
        try{
            a = messageBus.awaitMessage(bm1);
        }
        catch (IllegalStateException | InterruptedException e){
            assert false;
        }
        assertEquals(a, be2); // checks if the message received is of type 1 or 2 - should get 2 , that means first message was not saved

    }


}
