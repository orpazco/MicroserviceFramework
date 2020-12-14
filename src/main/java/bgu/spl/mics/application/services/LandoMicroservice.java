package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.BombDestroyerEvent;
import bgu.spl.mics.application.messages.TerminationEvent;
import bgu.spl.mics.application.passiveObjects.Diary;

/**
 * LandoMicroservice
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LandoMicroservice  extends MicroService {
    private Diary diary;
    private long duration;

    public LandoMicroservice(long duration, Diary diary) {
        super("Lando");
        this.diary = diary;
        this.duration = duration;
    }

    @Override
    protected void initialize() {
        subscribeEvent(BombDestroyerEvent.class, (event) -> {
            try {
                Thread.sleep(duration);
                complete(event, true);
                sendBroadcast(new TerminationEvent());
                terminate();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void finish() {
        diary.setLandoTerminate(System.currentTimeMillis());
    }
}
