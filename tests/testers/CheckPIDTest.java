package testers;

import org.junit.Test;
import os.hw1.ProcessRunner;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static os.hw1.Config.WAIT_P2;

public class CheckPIDTest extends BaseTester {
    @Override
    public void setUpProcess() throws Exception {
    }

    @Test(timeout = 10000)
    public void checkPIDTest() throws Exception {
        runningProcess = new ProcessRunner().runProcess(true);
        assertTrue(ProcessHandle.current().children().anyMatch(processHandle -> runningProcess.getMaster().equals(processHandle)));
        List<ProcessHandle> materChildren = runningProcess.getMaster().children().collect(Collectors.toList());
        assertEquals(materChildren.size(), runningProcess.getWorkers().length + 1);
        assertTrue(materChildren.contains(runningProcess.getCache()));
        for (ProcessHandle workerProcess : runningProcess.getWorkers()) {
            assertTrue(materChildren.contains(workerProcess));
        }
        Future<Response> responseFuture1 = executorService.submit(() -> sendRequest(10, 2));
        Future<Response> responseFuture2 = executorService.submit(() -> sendRequest(13, 2));
        Thread.sleep(500);
        List<ProcessHandle> worker1ChildrenPid = runningProcess.getWorkers()[0].children().collect(Collectors.toList());
        assertEquals(worker1ChildrenPid.size(), 1);
        List<ProcessHandle> worker2ChildrenPid = runningProcess.getWorkers()[1].children().collect(Collectors.toList());
        assertEquals(worker2ChildrenPid.size(), 1);
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
        BaseTester.Response result1 = responseFuture1.get();
        assertTime(result1.time, WAIT_P2);
        assertEquals(result1.output, 0);
        BaseTester.Response result2 = responseFuture2.get();
        assertTime(result2.time, WAIT_P2);
        assertEquals(result2.output, 1);
    }
}
