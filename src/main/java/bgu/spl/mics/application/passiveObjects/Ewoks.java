package bgu.spl.mics.application.passiveObjects;

import java.util.*;

/**
 * Passive object representing the resource manager.
 * <p>
 * This class must be implemented as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private methods and fields to this class.
 */
public class Ewoks {
    // save and handle all the ewoks created in this array
    private Ewok[] ewoksArr;
    private int ewoksAmount;

    public Ewoks(int ewoksAmount){
        this.ewoksAmount = ewoksAmount;
        this.ewoksArr = new Ewok[ewoksAmount+1];
        createEwoks();
    }

    /**
     * create the necessary ewoks and save each ewok in the array according to the ewok serial number
     * no use in index 0
     */
    private void createEwoks() {
        ewoksArr[0] = null;
        for (int i = 1; i <= ewoksAmount; i++) {
            Ewok ewok = new Ewok(i);
            ewoksArr[i] = ewok;
        }
    }

    public synchronized void acquireResources(Collection<Integer> ewoks) {
        // check if all the needed ewoks is available, if not wait until release & notify
        while (!isAvailable(ewoks)){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // if there are enough resources than acquire them
        for (int ewokSerial : ewoks) {
            ewoksArr[ewokSerial].acquire();
        }
    }

    public synchronized void releaseResources(Collection<Integer> ewoks){
        // release all the given resources and notify
        for (int ewokSerial : ewoks) {
            ewoksArr[ewokSerial].release();
        }
        notify();
    }

    /**
     * for each ewok check if it is available to acquire
     * @param ewoks serials of all the needed ewoks
     * @return true if all of them are available, false otherwise
     */
    private boolean isAvailable(Collection<Integer> ewoks){
        for (int ewokSerial : ewoks) {
            if (!ewoksArr[ewokSerial].isAvailable()) return false;
        }
        return true;
    }
}
