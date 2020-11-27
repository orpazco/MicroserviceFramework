package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TestBCastEvent1 implements Broadcast {
    public int content = 1;

    public int getContent() {
        return content;
    }
}
