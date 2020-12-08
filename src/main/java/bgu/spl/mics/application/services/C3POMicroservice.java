package bgu.spl.mics.application.services;

import bgu.spl.mics.application.messages.FinishAttackEvent;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Ewoks;

/**
 * C3POMicroservices is in charge of the handling {@link bgu.spl.mics.application.messages.AttackEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link bgu.spl.mics.application.messages.AttackEvent}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class C3POMicroservice extends AttackMicroservice {

    public C3POMicroservice(Diary diary, Ewoks ewoks) {
        super("C3PO", diary, ewoks);
    }

    @Override
    protected void initialize() {
        subscribeEvent(FinishAttackEvent.class, (event)-> diary.incTotalAttacks());
        subscribeToAttackEvent();
    }

    @Override
    protected void finish() {
        diary.setC3POTerminate(System.currentTimeMillis());
    }
}
