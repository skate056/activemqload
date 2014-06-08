package org.apache.activemq;

import static org.apache.activemq.MessageHandler.NUMBER_OF_ERRORS;
import static org.apache.activemq.MessageHandler.NUMBER_OF_MESSAGES_RECIEVED;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StopWatch;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:activemq-connection.xml")
public class LoadTest {

    private static final int TOTAL_NUMBER_OF_MESSAGES = 1000000;
    private static final int ERRORS_FOR_EVERY = 3;
    private static final int NUMBER_OF_RETRY_ATTEMPTS = 6;
    private static final String RESULT_STRING_WITH_SUCCESS = "NUMBER_OF_MESSAGES_RECIEVED: %s, TOTAL_NUMBER_OF_MESSAGES: %s, NUMBER_OF_ERRORS: %s, NUMBER_OF_SUCCESS: %s\n";
    private static final String RESULT_STRING = "NUMBER_OF_MESSAGES_RECIEVED: %s, TOTAL_NUMBER_OF_MESSAGES: %s, NUMBER_OF_ERRORS: %s\n";
    private static final int EXPECTED_COUNT;
    private StopWatch stopWatch = new StopWatch(this.getClass().getSimpleName());

    static {
        if (TOTAL_NUMBER_OF_MESSAGES % ERRORS_FOR_EVERY == 0) {
            EXPECTED_COUNT = ((TOTAL_NUMBER_OF_MESSAGES / ERRORS_FOR_EVERY) * NUMBER_OF_RETRY_ATTEMPTS) + TOTAL_NUMBER_OF_MESSAGES;
        } else {
            EXPECTED_COUNT = TOTAL_NUMBER_OF_MESSAGES + ((TOTAL_NUMBER_OF_MESSAGES / ERRORS_FOR_EVERY) * NUMBER_OF_RETRY_ATTEMPTS)
                    + NUMBER_OF_RETRY_ATTEMPTS;
        }
    }

    @Produce(uri = "ref:eventLoadConsumerEndpoint")
    protected ProducerTemplate template;

    @Test
    public void run() throws InterruptedException {
        stopWatch.start("TotalTime");
        ExecutorService executor = Executors.newFixedThreadPool(20);
        for (int i = 0; i < TOTAL_NUMBER_OF_MESSAGES; i++) {
            String fail = (i % ERRORS_FOR_EVERY == 0) ? "true" : "false";
            addMessage(executor, fail);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        while (NUMBER_OF_MESSAGES_RECIEVED < EXPECTED_COUNT) {
            System.out.printf(RESULT_STRING_WITH_SUCCESS, NUMBER_OF_MESSAGES_RECIEVED, TOTAL_NUMBER_OF_MESSAGES, NUMBER_OF_ERRORS,
                    MessageHandler.NUMBER_OF_SUCCESS);
            Thread.sleep(1000L);
        }
        System.out.printf(RESULT_STRING, NUMBER_OF_MESSAGES_RECIEVED, TOTAL_NUMBER_OF_MESSAGES, NUMBER_OF_ERRORS);
        stopWatch.stop();
        System.out.printf(stopWatch.shortSummary());
        System.out.printf(stopWatch.prettyPrint());
    }

    private void addMessage(ExecutorService executor, final String fail) {
        FutureTask<String> future = new FutureTask<String>(new Callable<String>() {

            public String call() {
                return sendMessage(fail);
            }
        });
        executor.execute(future);
    }

    private String sendMessage(final String fail) {
        template.sendBody(fail);
        return fail;
    }

}
