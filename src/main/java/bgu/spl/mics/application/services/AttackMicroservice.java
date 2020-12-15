package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.TerminationEvent;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Ewoks;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public abstract class AttackMicroservice extends MicroService {

    protected Diary diary;
    private Ewoks ewoks;
    private CountDownLatch latch;

    /**
     * @param name the micro-service name (used mainly for debugging purposes -
     *             does not have to be unique)
     */
    public AttackMicroservice(String name, Diary diary, Ewoks ewoks, CountDownLatch latch) {
        super(name);
        this.diary = diary;
        this.ewoks = ewoks;
        this.latch = latch;
    }

    protected abstract void finish();
    protected abstract void subscribeToNoMoreAttacksEvent();

    protected void initialize(){
        subscribeToTerminationEvent();
        subscribeToAttackEvent();
        subscribeToNoMoreAttacksEvent();
        latch.countDown();

    }

    protected void subscribeToAttackEvent(){
        subscribeEvent(AttackEvent.class, (event)->{
            // ask for resources
            List<Integer> serials = event.getSerials();
            ewoks.acquireResources(serials);
            try {
                Thread.sleep(event.getDuration());
                complete(event, true);
                // record the attack
                diary.incTotalAttacks();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                ewoks.releaseResources(serials);
            }
        });
    }

    protected void subscribeToTerminationEvent(){
        subscribeBroadcast(TerminationEvent.class, (event)-> terminate());
    }
}
