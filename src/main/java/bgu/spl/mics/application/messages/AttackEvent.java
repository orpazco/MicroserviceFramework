package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.Attack;

import java.util.List;

public class AttackEvent implements Event<Boolean> {
	private Attack attackData;

	public AttackEvent(Attack attackData){
	    this.attackData = attackData;
    }

    public Attack getAttackData() {
        return attackData;
    }

    public List<Integer> getSerials(){
	    return attackData.getSerials();
    }

    public int getDuration(){
	    return attackData.getDuration();
    }

}
