package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class dummyBroadcastEvent2 implements Broadcast {
    public int content = 2;

    public int getContent() {
        return content;
    }
}
