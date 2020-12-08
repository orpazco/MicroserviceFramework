package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.TerminationEvent;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Ewoks;

import java.util.List;


/**
 * C3POMicroservices is in charge of the handling {@link bgu.spl.mics.application.messages.AttackEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link bgu.spl.mics.application.messages.AttackEvent}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class C3POMicroservice extends MicroService {
    private Diary diary;
    private Ewoks ewoks;

    public C3POMicroservice(Diary diary, Ewoks ewoks) {
        super("C3PO");
        this.diary = diary;
        this.ewoks = ewoks;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TerminationEvent.class, (event)-> {
            terminate();
        });
        subscribeEvent(AttackEvent.class, (event)->{
            // ask for resources
            List<Integer> serials = event.getSerials();
            ewoks.acquireResources(serials);
            try {
                Thread.sleep(event.getDuration());
                complete(event, true);
                // call to diary and send finish- a timestamp indicating when C3PO finished the execution of all his attacks.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void finish() {
        diary.setC3POTerminate(System.currentTimeMillis());
    }
}
