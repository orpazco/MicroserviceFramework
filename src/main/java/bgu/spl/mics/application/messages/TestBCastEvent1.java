package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TestBCastEvent1 implements Broadcast {
    public int content;
    public TestBCastEvent1(){
        content=1;
    }
    public TestBCastEvent1(int input) {
        content = input;
    }


    public int getContent() {
        return content;
    }
}
