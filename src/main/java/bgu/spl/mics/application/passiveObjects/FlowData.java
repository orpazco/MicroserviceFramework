package bgu.spl.mics.application.passiveObjects;

import java.util.List;

public class FlowData {
    private List<Attack> attacks;
    private long R2D2;
    private long Lando;
    private int Ewoks;

    public FlowData() {
    }

    public FlowData(List<Attack> attacks, long R2D2, long Lando, int Ewoks) {
        this.attacks = attacks;
        this.R2D2 = R2D2;
        this.Lando = Lando;
        this.Ewoks = Ewoks;
    }

    public List<Attack> getAttacks() {
        return attacks;
    }

    public long getR2D2() {
        return R2D2;
    }

    public long getLando() {
        return Lando;
    }

    public int getEwoks() {
        return Ewoks;
    }
}