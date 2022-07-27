package testers;

import org.junit.Test;
import os.hw1.ProcessRunner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static os.hw1.Config.WAIT_P1;
import static os.hw1.Config.WAIT_P2;

public class WorkerWeights3Test extends BaseTester {
    @Override
    public void setUpProcess() throws Exception {
        runningProcess = new ProcessRunner().setWorkerCount(1).setW(3).runProcess();
    }

    @Test(timeout = 10000)
    public void workerWeights3Test() throws InterruptedException, ExecutionException {
        Future<Response> responseFuture1 = executorService.submit(() -> sendRequest(16, 2, 2, 2));
        Thread.sleep(500);
        Future<Response> responseFuture2 = executorService.submit(() -> sendRequest(13, 1, 1, 1));
        Response result1 = responseFuture1.get();
        assertTime(result1.time, 3 * WAIT_P2);
        assertEquals(result1.output, 0);
        Response result2 = responseFuture2.get();
        assertTime(result2.time, 3 * WAIT_P1 + 3 * WAIT_P2 - 500);
        assertEquals(result2.output, 10);
    }
}
