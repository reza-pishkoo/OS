package testers;

import org.junit.Test;
import os.hw1.ProcessRunner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

public class MultiTimeKillTest extends BaseTester {
    @Override
    public void setUpProcess() throws Exception {
        runningProcess = new ProcessRunner().setWorkerCount(1).runProcess();
    }

    @Test(timeout = 10000)
    public void multiTimeKillTest() throws InterruptedException, ExecutionException {
        Future<Response> responseFuture1 = executorService.submit(() -> sendRequest(10, 1));
        Thread.sleep(400);
        for (int i = 0; i < 3; i++) {
            killWorker(0);
            Thread.sleep(400);
            assertEquals(runningProcess.getWorkers()[0].children().count(), 1 + 1);
        }
        Response result1 = responseFuture1.get();
        assertEquals(result1.output, 9);
    }
}
