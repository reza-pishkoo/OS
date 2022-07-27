package testers;

import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static os.hw1.Config.WAIT_P1;

public class Cache1Test extends BaseTester {
    @Test(timeout = 10000)
    public void cache1Test() throws InterruptedException, ExecutionException {
        System.out.println(1);
        int a = 2;
        int[] programs = new int[a];
        Arrays.fill(programs, 1);
        Future<Response> r1 = executorService.submit(() -> sendRequest(50, programs));
        Thread.sleep(400);
        Future<Response> r2 = executorService.submit(() -> sendRequest(50, programs));
        Thread.sleep(400);
        Future<Response> r3 = executorService.submit(() -> sendRequest(50, programs));
        Response result1 = r1.get();
        assertTime(result1.time, a * WAIT_P1);
        assertEquals(result1.output, 50 - a);
        Response result2 = r2.get();
        assertTime(result2.time, a * WAIT_P1 - 400);
        assertEquals(result2.output, 50 - a);
        Response result3 = r3.get();
        assertTime(result3.time, a * WAIT_P1 - 800);
        assertEquals(result3.output, 50 - a);
    }
}
