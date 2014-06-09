package org.apache.activemq;

import static org.apache.activemq.MessageHandler.NUMBER_OF_ERRORS;
import static org.apache.activemq.MessageHandler.NUMBER_OF_MESSAGES_RECIEVED;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
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

    private static final long TOTAL_NUMBER_OF_MESSAGES = 1000000;
    private static final long ERRORS_FOR_EVERY = 1000000;
    private static final long NUMBER_OF_RETRY_ATTEMPTS = 6;
    private static final String RESULT_STRING_WITH_SUCCESS = "NUMBER_OF_MESSAGES_RECIEVED: %s, TOTAL_NUMBER_OF_MESSAGES: %s, NUMBER_OF_ERRORS: %s, NUMBER_OF_SUCCESS: %s\n";
    private static final String RESULT_STRING = "NUMBER_OF_MESSAGES_RECIEVED: %s, TOTAL_NUMBER_OF_MESSAGES: %s, NUMBER_OF_ERRORS: %s\n";
    private static final long EXPECTED_COUNT;
    private Stopwatch stopWatch = new Stopwatch();

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
        stopWatch.start();
        ExecutorService executor = Executors.newFixedThreadPool(20);
        for (int i = 0; i < TOTAL_NUMBER_OF_MESSAGES; i++) {
            String fail = (i % ERRORS_FOR_EVERY == 0) ? "true" : "false";
            addMessage(executor, fail);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        long enqueueTime = stopWatch.elapsed(TimeUnit.MILLISECONDS);

        while (NUMBER_OF_MESSAGES_RECIEVED < EXPECTED_COUNT) {
            System.out.printf(RESULT_STRING_WITH_SUCCESS, NUMBER_OF_MESSAGES_RECIEVED, TOTAL_NUMBER_OF_MESSAGES, NUMBER_OF_ERRORS,
                    MessageHandler.NUMBER_OF_SUCCESS);
            Thread.sleep(1000L);
        }
        System.out.printf(RESULT_STRING, NUMBER_OF_MESSAGES_RECIEVED, TOTAL_NUMBER_OF_MESSAGES, NUMBER_OF_ERRORS);
        stopWatch.stop();

        System.out.println(TOTAL_NUMBER_OF_MESSAGES + " messages processed");
        System.out.println(enqueueTime + " seconds elapsed (enqueue)");
        final long dequeueTime = stopWatch.elapsed(TimeUnit.MILLISECONDS);
        System.out.println(dequeueTime + " seconds elapsed (dequeue)");
        System.out.println(enqueueTime + dequeueTime + " seconds elapsed (total)");

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
