package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.TerminationEvent;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Ewoks;

import java.util.List;

public abstract class AttackMicroservice extends MicroService {

    protected Diary diary;
    private Ewoks ewoks;

    /**
     * @param name the micro-service name (used mainly for debugging purposes -
     *             does not have to be unique)
     */
    public AttackMicroservice(String name, Diary diary, Ewoks ewoks) {
        super(name);
        this.diary = diary;
        this.ewoks = ewoks;
    }

    protected abstract void finish();

    protected void initialize(){
        subscribeToTerminationEvent();
        subscribeToAttackEvent();
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
                // TODO: call to diary and send finish- a timestamp indicating when C3PO/hansolo finished the execution of all his attacks.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    protected void subscribeToTerminationEvent(){
        subscribeBroadcast(TerminationEvent.class, (event)-> terminate());
    }
}
