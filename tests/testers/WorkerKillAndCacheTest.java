package testers;

import org.junit.Test;
import os.hw1.ProcessRunner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static os.hw1.Config.*;

public class WorkerKillAndCacheTest extends BaseTester {
    @Override
    public void setUpProcess() throws Exception {
        runningProcess = new ProcessRunner().setWorkerCount(1).setW(3).runProcess();
    }

    @Test(timeout = 10000)
    public void workerKillAndCacheTest() throws InterruptedException, ExecutionException {
        Future<Response> responseFuture1 = executorService.submit(() -> sendRequest(13, 1, 2));
        Thread.sleep(400);
        Future<Response> responseFuture2 = executorService.submit(() -> sendRequest(13, 3, 2));
        Thread.sleep(400);
        int workerNumToKill = 0;
        killWorker(workerNumToKill);
        Thread.sleep(400);
        assertEquals(runningProcess.getWorkers()[0].children().count(), 1);
        BaseTester.Response result1 = responseFuture1.get();
        assertTime(result1.time, WAIT_P2 + WAIT_P1 + 1200);
        assertEquals(result1.output, 0);
        BaseTester.Response result2 = responseFuture2.get();
        assertTime(result2.time, WAIT_P2 + WAIT_P3 + WAIT_P1 + 1200);
        assertEquals(result2.output, 1);
    }
}
