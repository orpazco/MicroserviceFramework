package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.DeactivationEvent;
import bgu.spl.mics.application.messages.NoMoreAttacks;
import bgu.spl.mics.application.messages.TerminationEvent;
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
    private int resolvedAttacks;
    private HashMap<AttackEvent, Future<Boolean>> attackRecords;
    private int longestAttackTime;
    private int bufferMultiplier;
    private int R2D2Time;


    public LeiaMicroservice(Attack[] attacks, Diary diary) {
        super("Leia");
        this.attacks = attacks;
        this.diary = diary;
        resolvedAttacks = 0;
        longestAttackTime = attacks[0].getDuration();
        bufferMultiplier=2; // how long of a buffer to take from the longest message
        R2D2Time = 0; // TODO get r2d2's timer
    }

    public int getResolvedAttacks() {
        return resolvedAttacks;
    }

    private void findLongestAttackTime(){
        for (Attack attackInstructions : attacks) {
            if (attackInstructions.getDuration()>longestAttackTime)
                longestAttackTime = attackInstructions.getDuration();
        }
    }

    public void orchestrateAttacks() {
        for (Attack attackInstructions : attacks) {
            AttackEvent currAttack = new AttackEvent(attackInstructions); // create a new attack event
            Future<Boolean> currFuture = sendEvent(currAttack);
            attackRecords.put(currAttack, currFuture);// map attacks to futures
        }
        sendBroadcast(new NoMoreAttacks()); // tell attackers no more attack will be sent
        for (AttackEvent attack : attackRecords.keySet()) { // wait for each attack to finish (event to be resolved)
            if(attackRecords.get(attack).get(longestAttackTime*bufferMultiplier, TimeUnit.MILLISECONDS)) // wait 5 seconds for failed get
                resolvedAttacks++;
        }
    }


    @Override
    protected void initialize() {
        subscribeBroadcast(TerminationEvent.class, (event) -> {
            terminate();
            diary.setLeiaTerminate(System.currentTimeMillis());
        });
        // wait for everyone to go online TODO check-in messages
        orchestrateAttacks();
        if (resolvedAttacks==attacks.length){ // check if all attacks were successful
            Future<Boolean> deactivation = sendEvent(new DeactivationEvent());
            deactivation.get(R2D2Time*bufferMultiplier,TimeUnit.MILLISECONDS);
        }
        // TODO decide on what to do if not all attacks were successful
    }
}
