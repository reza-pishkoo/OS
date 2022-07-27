package testers;

import org.junit.Test;
import os.hw1.ProcessRunner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static os.hw1.Config.WAIT_P1;
import static os.hw1.Config.WAIT_P2;

public class WorkerWeights2Test extends BaseTester {
    @Override
    public void setUpProcess() throws Exception {
        runningProcess = new ProcessRunner().setW(4).runProcess();
    }

    @Test(timeout = 10000)
    public void workerWeights2Test() throws InterruptedException, ExecutionException {
        Future<Response> responseFuture1 = executorService.submit(() -> sendRequest(10, 1));
        Future<Response> responseFuture2 = executorService.submit(() -> sendRequest(13, 1));
        Thread.sleep(500);
        Future<Response> responseFuture3 = executorService.submit(() -> sendRequest(17, 2));
        Response result1 = responseFuture1.get();
        assertTime(result1.time, WAIT_P1);
        assertEquals(result1.output, 9);
        Response result2 = responseFuture2.get();
        assertTime(result2.time, WAIT_P1);
        assertEquals(result2.output, 12);
        Response result3 = responseFuture3.get();
        assertTime(result3.time, WAIT_P1 + WAIT_P2 - 500);
        assertEquals(result3.output, 3);
    }
}
