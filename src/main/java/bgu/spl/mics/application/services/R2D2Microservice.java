package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.DeactivationEvent;
import bgu.spl.mics.application.messages.TerminationEvent;
import bgu.spl.mics.application.passiveObjects.Diary;

import java.util.concurrent.CountDownLatch;

/**
 * R2D2Microservices is in charge of the handling {@link DeactivationEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link DeactivationEvent}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class R2D2Microservice extends MicroService {

    private Diary diary;
    private long deactivationSleepDuration;
    private CountDownLatch latch;

    public R2D2Microservice(long duration, Diary diary, CountDownLatch latch) {
        super("R2D2");
        this.diary = diary;
        this.deactivationSleepDuration = duration;
        this.latch = latch;
    }

    private long getDeactivationSleepDuration() {
        return deactivationSleepDuration;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TerminationEvent.class, (event) -> terminate());
        subscribeToDeactivation();
        latch.countDown();
    }

    private void  subscribeToDeactivation() {
        subscribeEvent(DeactivationEvent.class, (event) -> {
                    try {
                        Thread.sleep(getDeactivationSleepDuration());
                        diary.setR2D2Deactivate(System.currentTimeMillis()); // log finish time before notifying
                        complete(event, true);
                    } catch (InterruptedException e) { // print interruption log
                        e.printStackTrace();
                    }
                }
        );
    }

    @Override
    protected void finish() {
        diary.setR2D2Terminate(System.currentTimeMillis());
    }
}
