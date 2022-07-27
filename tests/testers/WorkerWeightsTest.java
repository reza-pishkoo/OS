package testers;

import org.junit.Test;
import os.hw1.ProcessRunner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static os.hw1.Config.WAIT_P1;
import static os.hw1.Config.WAIT_P2;

public class WorkerWeightsTest extends BaseTester {
    @Override
    public void setUpProcess() throws Exception {
        runningProcess = new ProcessRunner().setW(4).runProcess();
    }

    @Test(timeout = 10000)
    public void workerWeightsTest() throws InterruptedException, ExecutionException {
        Future<Response> responseFuture1 = executorService.submit(() -> sendRequest(10, 1, 2, 1));
        Future<Response> responseFuture2 = executorService.submit(() -> sendRequest(13, 2, 1, 2));
        Thread.sleep(500);
        Response result1 = responseFuture1.get();
        assertTime(result1.time, 2 * WAIT_P1 + WAIT_P2);
        assertEquals(result1.output, 3);
        Response result2 = responseFuture2.get();
        assertTime(result2.time, WAIT_P1 + 2 * WAIT_P2);
        assertEquals(result2.output, 0);
    }
}
