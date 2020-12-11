package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.Attack;
import bgu.spl.mics.application.passiveObjects.Diary;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * LeiaMicroservices Initialized with Attack objects, and sends them as  {@link bgu.spl.mics.application.messages.AttackEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link bgu.spl.mics.application.messages.AttackEvent}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LeiaMicroservice extends MicroService {
    private Attack[] attacks;
    private Diary diary;
    private HashMap<AttackEvent, Future<Boolean>> attackRecords;
    private int bufferMultiplier;


    public LeiaMicroservice(Attack[] attacks, Diary diary) {
        super("Leia");
        this.attacks = attacks;
        this.diary = diary;
        bufferMultiplier = 2; // how long of a buffer to take from the longest message //TODO reconsider blocking instead of waiting
    }

    public void orchestrateAttacks() {
        for (Attack attackInstructions : attacks) {
            AttackEvent currAttack = new AttackEvent(attackInstructions); // create a new attack event
            Future<Boolean> currFuture = sendEvent(currAttack);
            attackRecords.put(currAttack, currFuture);// map attacks to futures
        }
        sendBroadcast(new NoMoreAttacks()); // tell attackers no more attack will be sent
        for (AttackEvent attack : attackRecords.keySet()) { // wait for each attack to finish (event to be resolved)
            attackRecords.get(attack).get(); // wait until the attack is finished
        }
    }


    @Override
    protected void initialize() {
        subscribeBroadcast(TerminationEvent.class, (event) -> {
            terminate();
            diary.setLeiaTerminate(System.currentTimeMillis());
        });
        // wait for everyone to go online TODO check-in messages - with timeout
        orchestrateAttacks();
        Future<Boolean> deactivation = sendEvent(new DeactivationEvent()); // after resolving all attack events - send deactivation event
        deactivation.get(); // wait for R2D2 to finish deactivation
        sendEvent(new BombDestroyerEvent());
    }
}

