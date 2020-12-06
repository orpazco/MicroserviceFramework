package bgu.spl.mics;

import bgu.spl.mics.application.passiveObjects.Ewok;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EwokTest {
    private Ewok ewok;
    private int initialSN;

    @BeforeEach
    public void setUp(){
        ewok = new Ewok(3);
        initialSN = ewok.getSerialNumber();
    }

    @Test
    public void testSerial(){
        // check if serialNumber is not null
        assertNotNull(initialSN);
    }
     @Test
     public void testInitialAvailability(){
        // check if initialized as available
        assertTrue(ewok.isAvailable(), "Expected: " + true + "Actual:" + ewok.isAvailable());
    }
    @Test
    public void testAcquireAvailable(){
        // check if boolean changed after acquiring
        ewok.acquire();
        assertFalse(ewok.isAvailable());
        // check if serial is the same
        assertEquals(initialSN, ewok.getSerialNumber(), "Expected SN: "+ initialSN + "Actual: " + ewok.getSerialNumber());
    }
    @Test
    public void testAcquireUnavailable(){
        // get availability - should get false
        ewok.acquire();
        boolean availability = ewok.isAvailable();
        // attempt to acquire and check if boolean changed  should not change
        ewok.acquire();
        assertEquals(availability, ewok.isAvailable(),"Expected: " + availability + "Actual: " + ewok.isAvailable());
        // check if serial is the same
        assertEquals(initialSN, ewok.getSerialNumber(), "Expected SN: "+ initialSN + "Actual: " + ewok.getSerialNumber());
    }
    @Test
    public void testReleaseUnavailable(){
        // set Ewok as unavailable
        ewok.acquire();
        // check if boolean changed after releasing
        ewok.release();
        assertTrue(ewok.isAvailable());
        // check if serial number changed
        assertEquals(initialSN, ewok.getSerialNumber(), "Expected SN: "+ initialSN + "Actual: " + ewok.getSerialNumber());
    }
    @Test
    public void testReleaseAvailable(){
        // get availability - should get true
        boolean availability = ewok.isAvailable();
        // check if boolean changed - should not change
        ewok.release();
        assertEquals(availability, ewok.isAvailable(),"Expected: " + availability + "Actual: " + ewok.isAvailable());
        // check if serial changed
        assertEquals(initialSN, ewok.getSerialNumber(), "Expected SN: "+ initialSN + "Actual: " + ewok.getSerialNumber());
    }

}
