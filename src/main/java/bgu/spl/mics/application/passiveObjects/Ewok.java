package bgu.spl.mics.application.passiveObjects;

/**
 * Passive data-object representing a forest creature summoned when HanSolo and C3PO receive AttackEvents.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You may add fields and methods to this class as you see fit (including public methods).
 */
public class Ewok {
	private int serialNumber;
	private boolean available;

	public Ewok(int serialNumber){
	    this.serialNumber = serialNumber;
	    this.available = true;
    }
  
    /**
     * Acquires an Ewok
     */
    public void acquire() {
        if (!available) throw new IllegalStateException("attempting to acquire unavailable resource. ewok number " + serialNumber);
		this.available = false;
    }

    /**
     * release an Ewok
     */
    public void release() {
    	this.available = true;
    }

    public int getSerialNumber(){
        return serialNumber;
    }

    public boolean isAvailable() {
        return available;
    }
}
