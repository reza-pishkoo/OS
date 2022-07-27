package testers;

import org.junit.After;
import org.junit.Before;
import os.hw1.ProcessRunner;
import os.hw1.RunningProcess;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static os.hw1.Config.SAFA_MARGIN;

public class BaseTester {
    protected RunningProcess runningProcess;
    protected ExecutorService executorService;

    protected static class Response {
        protected final long time;
        protected final int output;

        Response(long time, int output) {
            this.time = time;
            this.output = output;
        }
    }

    protected Response sendRequest(int input, int... programs) {
        String request = Arrays.stream(programs).mapToObj(String::valueOf).collect(Collectors.joining("|")) + " " + input;
        try {
            long start = System.currentTimeMillis();
            Socket socket = new Socket(InetAddress.getLocalHost(), ProcessRunner.port);
            Scanner scanner = new Scanner(socket.getInputStream());
            PrintStream printStream = new PrintStream(socket.getOutputStream());
            printStream.println(request);
            int response = scanner.nextInt();
            socket.close();
            return new Response(System.currentTimeMillis() - start, response);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    protected void killWorker(int workerNumToKill) {
        ProcessHandle workerToKill = runningProcess.getWorkers()[workerNumToKill];
        workerToKill.destroy();
        Scanner scanner = new Scanner(runningProcess.getProcess().getInputStream());
        Pattern patternStop = Pattern.compile("worker (?<workerNum>\\d+) stop (?<pid>\\d+) (?<port>\\d+)");
        while (true) {
            String line = scanner.nextLine();
            System.out.println(line);
            Matcher matcher = patternStop.matcher(line);
            if (matcher.matches()) {
                int workerNum = Integer.parseInt(matcher.group("workerNum"));
                assertEquals(workerNum, workerNumToKill);
                assertEquals(Long.parseLong(matcher.group("pid")), workerToKill.pid());
                break;
            }
        }
        Pattern patternStart = Pattern.compile("worker (?<workerNum>\\d+) start (?<pid>\\d+) (?<port>\\d+)");
        while (true) {
            String line = scanner.nextLine();
            System.out.println(line);
            Matcher matcher = patternStart.matcher(line);
            if (matcher.matches()) {
                int workerNum = Integer.parseInt(matcher.group("workerNum"));
                assertEquals(workerNum, workerNumToKill);
                long pid = Long.parseLong(matcher.group("pid"));
                ProcessHandle newWorker = ProcessHandle.of(pid).orElseThrow();
                runningProcess.setWorker(workerNum, newWorker);
                assertEquals(newWorker.parent().orElseThrow(), runningProcess.getMaster());
                break;
            }
        }
    }

    protected void assertTime(long actual, long expected) {
        System.out.println(actual - expected);
        if (expected - SAFA_MARGIN > actual || actual > expected + SAFA_MARGIN) {
            fail(expected - actual + "");
        }
    }

    @Before
    public void setUpExecutorService() {
        executorService = Executors.newCachedThreadPool();
    }

    @Before
    public void setUpProcess() throws Exception {
        runningProcess = new ProcessRunner().runProcess();
    }

    @After
    public void tearDown() throws Exception {
        runningProcess.getProcess().destroy();
        runningProcess.getProcess().waitFor();
        executorService.shutdown();
        runningProcess = null;
        executorService = null;
        System.gc();
    }
}