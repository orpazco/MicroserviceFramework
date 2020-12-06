package bgu.spl.mics.application.passiveObjects;

public class AttackData {
    private long duration;
    private int[] serials;

    public AttackData() {
    }

    public AttackData(long duration, int[] serials) {
        this.duration = duration;
        this.serials = serials;
    }

    public long getDuration() {
        return duration;
    }

    public int[] getSerials() {
        return serials;
    }
}