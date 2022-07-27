package testers;

import org.junit.Test;
import os.hw1.ProcessRunner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static os.hw1.Config.*;

public class Cache2Test extends BaseTester {
    @Override
    public void setUpProcess() throws Exception {
        runningProcess = new ProcessRunner().setW(3).runProcess();
    }

    @Test(timeout = 10000)
    public void cache2Test() throws InterruptedException, ExecutionException {
        Future<Response> r1 = executorService.submit(() -> sendRequest(50, 1,1,1));
        Thread.sleep(WAIT_P1 + 500);
        Future<Response> r2 = executorService.submit(() -> sendRequest(50, 2,1));
        Future<Response> r3 = executorService.submit(() -> sendRequest(50, 1));
        Response result1 = r1.get();
        assertTime(result1.time, 3 * WAIT_P1);
        assertEquals(result1.output, 50 - 3);
        Response result2 = r2.get();
        assertTime(result2.time, WAIT_P2);
        assertEquals(result2.output, 4);
        Response result3 = r3.get();
        assertTime(result3.time, 0);
        assertEquals(result3.output, 50 - 1);
        Future<Response> r1ResponseFuture = executorService.submit(() -> sendRequest(50, 3, 3, 3));
        Response result1Response = r1ResponseFuture.get();
        assertTime(result1Response.time, WAIT_P3);
        assertEquals(result1Response.output, 50);
    }
}
