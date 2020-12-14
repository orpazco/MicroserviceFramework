package bgu.spl.mics.application.services;


import bgu.spl.mics.application.messages.NoMoreAttacks;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Ewoks;

/**
 * HanSoloMicroservices is in charge of the handling {@link bgu.spl.mics.application.messages.AttackEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link bgu.spl.mics.application.messages.AttackEvent}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class HanSoloMicroservice extends AttackMicroservice {

    public HanSoloMicroservice(Diary diary, Ewoks ewoks) {
        super("Han", diary, ewoks);
    }

    @Override
    protected void finish() {
        diary.setHanSoloTerminate(System.currentTimeMillis());
    }

    @Override
    protected void subscribeToNoMoreAttacksEvent() {
        subscribeBroadcast(NoMoreAttacks.class, (e)-> diary.setHanSoloFinish(System.currentTimeMillis()));
    }
}
