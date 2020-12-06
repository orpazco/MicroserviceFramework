package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.TerminationEvent;
import bgu.spl.mics.application.passiveObjects.Attack;
import bgu.spl.mics.application.passiveObjects.Diary;

import java.util.HashMap;
import java.util.HashSet;

/**
 * LeiaMicroservices Initialized with Attack objects, and sends them as  {@link bgu.spl.mics.application.messages.AttackEvents}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link bgu.spl.mics.application.messages.AttackEvents}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LeiaMicroservice extends MicroService {
	private Attack[] attacks;
	private Diary diary;
	private int resolvedAttacks;
	private HashMap<Attack, Future> attackRecords;
	
    public LeiaMicroservice(Attack[] attacks, Diary diary) {
        super("Leia", diary);
		this.attacks = attacks;
		resolvedAttacks = 0;
    }



    @Override
    protected void initialize() {
    	subscribeBroadcast(TerminationEvent.class, (event)-> {
    	    terminate();
    	    diary.setLeiaTerminate(System.currentTimeMillis());
        });
        for (Attack attackInstructions : attacks){
            AttackEvent currAttack =  new AttackEvent(attackInstructions);
        }
    }
}
