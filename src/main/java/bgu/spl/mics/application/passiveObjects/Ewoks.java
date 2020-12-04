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
        // check if the amount of ewoks to require is available or the thread ask for will need
        // to wait until resources will be released
        Iterator<Integer> iterator = ewoks.iterator();
        while (iterator.hasNext()){
            int ewokSerial = iterator.next();
            if (!ewoksArr[ewokSerial].isAvailable()){
                try {
                    wait();
                    // initialize the iterator again to check the resources from the beginning
                    iterator = ewoks.iterator();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        // if there are enough resources than acquire them
        iterator = ewoks.iterator();
        while (iterator.hasNext()) {
            int ewokSerial = iterator.next();
            ewoksArr[ewokSerial].acquire();
        }
    }

    public synchronized void releaseResources(Collection<Integer> ewoks){

    }
}
