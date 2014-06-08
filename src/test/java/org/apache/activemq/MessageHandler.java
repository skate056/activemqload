package org.apache.activemq;

import java.util.concurrent.atomic.AtomicInteger;

public class MessageHandler {

    private static final AtomicInteger NUMBER_OF_MESSAGES_RECIEVED_ATOM =  new AtomicInteger();
    private static final AtomicInteger NUMBER_OF_ERRORS_ATOM =  new AtomicInteger();
    private static final AtomicInteger NUMBER_OF_SUCCESS_ATOM =  new AtomicInteger();

    public static int NUMBER_OF_MESSAGES_RECIEVED = 0;
    public static int NUMBER_OF_ERRORS = 0;
    public static int NUMBER_OF_SUCCESS = 0;

    public void handleMessage(final String failBooleanString) {
        NUMBER_OF_MESSAGES_RECIEVED = NUMBER_OF_MESSAGES_RECIEVED_ATOM.incrementAndGet();

        Boolean fail = Boolean.valueOf(failBooleanString);
        if(fail){
            NUMBER_OF_ERRORS = NUMBER_OF_ERRORS_ATOM.incrementAndGet();
            throw new NullPointerException("OUR NPE!!!!");
        } else{
            NUMBER_OF_SUCCESS = NUMBER_OF_SUCCESS_ATOM.incrementAndGet();
        }
    }


}
