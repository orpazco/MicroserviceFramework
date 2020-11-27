package bgu.spl.mics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;


public class FutureTest {

    private Future<String> future;
    private String expectedRes;

    @BeforeEach
    public void setUp(){
        future = new Future<>();
        expectedRes = "some Result";
    }

    @Test
    public void testResolve(){
        future.resolve(expectedRes);
        assertTrue(future.isDone());
        String actualRes = future.get();
        assertEquals(expectedRes, actualRes, "expected to different result. expected: " + expectedRes +
                ", actual: " + actualRes);
    }

    /**
     * test if future is done after resolve
     */
    @Test
    public void testIsDone(){
        assertFalse(future.isDone(), "parameter 'isDone' = true, not as expected");
        future.resolve(expectedRes);
        assertTrue(future.isDone(), "parameter 'isDone' = false, not as expected");
    }

    /**
     * test if future return the expected result
     */
    @Test
    public void testGetResult(){
        assertFalse(future.isDone(),"parameter 'isDone' = true, not as expected");
        future.resolve(expectedRes);
        String actualRes = future.get();
        assertEquals(expectedRes, actualRes, "expected to different result. expected: " + expectedRes +
                ", actual: " + actualRes);
    }

    /**
     * test if the future return the expected result
     */
    @Test
    public void testGetResultTimeout(){
        assertFalse(future.isDone(), "parameter 'isDone' = true, not as expected");
        future.resolve(expectedRes);
        String actualRes = future.get(300, TimeUnit.MILLISECONDS);
        assertEquals(expectedRes, actualRes,"expected to different result. expected: " + expectedRes +
                ", actual: " + actualRes);
    }

    /**
     * test if the future return null if there is no result after some time
     */
    @Test
    public void testGetResultTimeoutNoResolve() {
        String actualRes = future.get(500, TimeUnit.MILLISECONDS);
        assertNull(actualRes, "future get return different result than null: " + actualRes);
        assertFalse(future.isDone(), "parameter 'isDone' = true, not as expected");
    }

    /***
     * in this test we create future object and call to get result with timeout.
     * while the func waits, resolve the future result
     */
    @Test
    public void testGetResultBeforeResolve() {
        Thread resolve = new Thread(() -> {
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(1));
                future.resolve(expectedRes);
            } catch (InterruptedException e) {
                fail("InterruptedException was thrown from resolve thread");
            }
        });
        resolve.start();
        String actualRes = future.get(2, TimeUnit.SECONDS);
        assertTrue(future.isDone(),"parameter 'isDone' = false, not as expected");
        assertEquals(expectedRes, actualRes,"expected to different result. expected: " + expectedRes +
                ", actual: " + actualRes);
    }

    /***
     * resolve the future with result=null and check if the future return the expected result
     * and mark is done as true
     */
    @Test
    public void testResolveNull(){
        assertFalse(future.isDone(), "parameter 'isDone' = true, not as expected");
        future.resolve(null);
        String actualRes = future.get();
        assertNull(actualRes, "future get return different result than null: " + actualRes);
        assertTrue(future.isDone(),"parameter 'isDone' = false, not as expected");
    }
}