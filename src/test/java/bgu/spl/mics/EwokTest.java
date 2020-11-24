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
        ewok = new Ewok();
        initialSN = ewok.getSerialNumber();
    }


    @Test
    public void testSerial(){
        // check if serialNumber Valid??
     }

    public void testAcquireAvailable(){
        // check if can acquire
        assertTrue(ewok.isAvailable());
        // check if boolean changed after acquiring
        ewok.acquire();
        assertFalse(ewok.isAvailable());
        // check if serial is the same
        assertEquals(initialSN, ewok.getSerialNumber(), "Expected: "+ initialSN + "Actual: " + ewok.getSerialNumber());
    }

    public void testAcquireUnavailable(){
        // get availability - should get false
        boolean availability = ewok.isAvailable();
        assertFalse(availability, "Availability not as expected");
        // attempt to acquire and check if boolean changed  should not change
        ewok.acquire();
        assertEquals(availability, ewok.isAvailable(),"Expected: " + availability + "Actual: " + ewok.isAvailable());
        // check if serial is the same
        assertEquals(initialSN, ewok.getSerialNumber(), "Expected: "+ initialSN + "Actual: " + ewok.getSerialNumber());
    }

    public void testReleaseUnavailable(){
        // check if can be released
        assertFalse(ewok.isAvailable());
        // check if boolean changed
        ewok.release();
        assertTrue(ewok.isAvailable());
        // check if serial number changed
        assertEquals(initialSN, ewok.getSerialNumber(), "Expected: "+ initialSN + "Actual: " + ewok.getSerialNumber());
    }

    public void testReleaseAvailable(){
        // get availability - should get true
        boolean availability = ewok.isAvailable();
        assertTrue(availability, "Availability not as expected");
        // check if boolean changed - should not change
        ewok.release();
        assertEquals(availability, ewok.isAvailable(),"Expected: " + availability + "Actual: " + ewok.isAvailable());
        // check if serial changed
        assertEquals(initialSN, ewok.getSerialNumber(), "Expected: "+ initialSN + "Actual: " + ewok.getSerialNumber());
    }

}
