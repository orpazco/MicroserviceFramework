package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.passiveObjects.Diary;


/**
 * C3POMicroservices is in charge of the handling {@link bgu.spl.mics.application.messages.AttackEvents}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link bgu.spl.mics.application.messages.AttackEvents}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class C3POMicroservice extends MicroService {
	
    public C3POMicroservice(Diary diary) {
        super("C3PO", diary);
    }

    @Override
    protected void initialize() {

    }
}
