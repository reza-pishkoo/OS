package testers;

import org.junit.Test;
import os.hw1.ProcessRunner;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WorkerKillTest extends BaseTester {
    @Override
    public void setUpProcess() throws Exception {
        runningProcess = new ProcessRunner().setW(6).runProcess();
    }

    @Test(timeout = 10000)
    public void workerKillTest() throws InterruptedException, IOException, ExecutionException {
        Future<Response> responseFuture1 = executorService.submit(() -> sendRequest(10, 2));
        Future<Response> responseFuture2 = executorService.submit(() -> sendRequest(13, 2));
        Thread.sleep(500);
        List<ProcessHandle> worker1ChildrenPid = runningProcess.getWorkers()[0].children().collect(Collectors.toList());
        System.out.println(worker1ChildrenPid);
        assertEquals(worker1ChildrenPid.size(), 1 + 1);
        List<ProcessHandle> worker2ChildrenPid = runningProcess.getWorkers()[1].children().collect(Collectors.toList());
        assertEquals(worker2ChildrenPid.size(), 1 + 1);
        Process process = new ProcessBuilder("jps").start();
        Scanner scannerJps = new Scanner(process.getInputStream());
        while (scannerJps.hasNextLine()) {
            String line = scannerJps.nextLine();
            Scanner scannerLine = new Scanner(line);
            long pid = scannerLine.nextLong();
            String className = scannerLine.next();
            if (className.equals("Program2")) {
                assertTrue(
                        worker1ChildrenPid.contains(ProcessHandle.of(pid).orElseThrow()) ||
                                worker2ChildrenPid.contains(ProcessHandle.of(pid).orElseThrow())
                );
            }
        }
        int workerNumToKill = 0;
        killWorker(workerNumToKill);
        Thread.sleep(400);
        assertEquals(runningProcess.getWorkers()[1].children().count(), 2 + 1);
        assertEquals(runningProcess.getWorkers()[0].children().count(), 0 + 1);
        BaseTester.Response result1 = responseFuture1.get();
        assertEquals(result1.output, 0);
        BaseTester.Response result2 = responseFuture2.get();
        assertEquals(result2.output, 1);
    }
}
