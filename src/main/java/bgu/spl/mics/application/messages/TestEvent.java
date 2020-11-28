package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

/***
 * TestEvent is used for the {@link bgu.spl.mics.MessageBusImpl} unit tests
 */
public class TestEvent implements Event<String> {
    private static int index = 1;
    private int testIndex;

    public TestEvent(){
        testIndex = index;
        index++;
    }

    public int getTestIndex() {
        return testIndex;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TestEvent){
            return ((TestEvent) obj).testIndex == testIndex;
        }
        return false;
    }
}
