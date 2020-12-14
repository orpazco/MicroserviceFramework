package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.NoMoreAttacks;
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
            // TODO LOG DEBUG
            System.out.println("service: " + getName() + " tried to acquire:  " + serials.toString() + " at: " + System.currentTimeMillis() );
            // TODO LOG DEBUG
            ewoks.acquireResources(serials);
            try {
                // TODO LOG DEBUG
                System.out.println("service: " + getName() + " started attack: " + serials.toString() + " at: " + System.currentTimeMillis() );
                // TODO LOG DEBUG
                Thread.sleep(event.getDuration());
                complete(event, true);
                // TODO LOG DEBUG
                System.out.println("service: " + getName() + " completed attack: " + serials.toString() + " at: " + System.currentTimeMillis() );
                // TODO LOG DEBUG
                // record the attack
                diary.incTotalAttacks();
                // TODO: call to diary and send finish- a timestamp indicating when C3PO/hansolo finished the execution of all his attacks.
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
