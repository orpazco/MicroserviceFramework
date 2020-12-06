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
       subscribeBroadcast(TerminationEvent.class, (event) -> {
           terminate();
           diary.setLandoTerminate(System.currentTimeMillis());
       });
       subscribeEvent(BombDestroyerEvent.class, (event)->{
           try {
               Thread.sleep(duration);
               complete(event, true);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
       });
    }
}
