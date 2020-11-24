package bgu.spl.mics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


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
        String str = "someResult";
        future.resolve(str);
        assertTrue(future.isDone());
        assertEquals(str, future.get());
    }

    /**
     * test if future is done after resolve
     */
    @Test
    public void testIsDone(){
        future.resolve(expectedRes);
        assertTrue(future.isDone());
    }

    /**
     * test if future return the expected result
     */
    @Test
    public void testGetResult(){
        future.resolve(expectedRes);
        assertTrue(future.isDone());
        assertEquals(expectedRes, future.get());
    }

    /**
     * test if the future return the expected result
     */
    @Test
    public void testGetResultTimeout(){
        future.resolve(expectedRes);
        assertTrue(future.isDone());
        assertEquals(expectedRes, future.get(3, TimeUnit.SECONDS));
    }

    /**
     * test if the future throw exception if there is no result after some time
     */
    @Test
    public void testGetResultNoResolve(){
        String actualRes = null;
        try {
            actualRes = future.get();
        }
        catch (Exception e){
            assertEquals(actualRes, null);
        }
        // if no exception thrown the tests fails
        assertTrue(false);
    }

    /**
     * test if the future throw exception if there is no result after some time
     */
    @Test
    public void testGetResultTimeoutNoRes(){
        String actualRes = null;
        long startTime = 0;
        long estimatedTime;
        int maxTimeout = 3;
        try {
            startTime = System.currentTimeMillis();
            actualRes = future.get(maxTimeout, TimeUnit.SECONDS);
        }
        catch (Exception e){
            assertEquals(actualRes, null);
            estimatedTime = System.currentTimeMillis() - startTime;
            assertTrue(TimeUnit.MILLISECONDS.toSeconds(estimatedTime) <= maxTimeout);
        }
        // if no exception thrown the tests fails
        assertTrue(false);
    }

    /***
     * in this test we create future object and call to get result with timeout.
     * while the func waits, resolve the future result
     */
    @Test
    public void testGetResultBeforeResolve(){
        try{
            Thread resolve = new Thread(() -> {
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(3));
                    future.resolve(expectedRes);
                } catch (InterruptedException e) {
                    assertTrue(false, "InterruptedException was thrown from resolve thread");
                }
            });
            resolve.start();
            future.get(5, TimeUnit.SECONDS);
        }
        catch (Exception e){
            // if exception is throw the test fails
            assertTrue(false);
        }
    }
}