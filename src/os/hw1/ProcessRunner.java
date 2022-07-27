package os.hw1;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static os.hw1.Config.CLASS_PATH;
import static os.hw1.Config.JAVA_PATH;

public class ProcessRunner {
    public static final int port = 16543;
    private int workerCount = 2;
    private int w = 5;
    static final String[] commonArgs = {
            "C:/Program Files/Java/jdk-11.0.14/bin/java.exe", // replace with your java path with version 1.8
            "-classpath",
            "out/production/OS/" // replace with your classpath
    };
    static final String[] programs = {
            "programs.Program1 2",
            "programs.Program2 3",
            "programs.Program3 2",
    };

    public ProcessRunner setWorkerCount(int workerCount) {
        this.workerCount = workerCount;
        return this;
    }

    public ProcessRunner setW(int w) {
        this.w = w;
        return this;
    }

    private void transfer(OutputStream outputStream, InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream);
        PrintStream printStream = new PrintStream(outputStream);
        while (scanner.hasNextLine()) {
            printStream.println(scanner.nextLine());
            printStream.flush();
        }
    }

    public RunningProcess runProcess() throws Exception {
        return runProcess(false);
    }

    public RunningProcess runProcess(boolean waitForCache) throws Exception {
        Process process = new ProcessBuilder(
                commonArgs[0], commonArgs[1], commonArgs[2], "os.hw1.master.MasterMain"
        ).start();
        RunningProcess result = new RunningProcess(process, workerCount);
        PrintStream printStream = new PrintStream(process.getOutputStream());
        printStream.println(port);
        printStream.println(workerCount);
        printStream.println(w);
        printStream.println(commonArgs.length);
        Arrays.stream(commonArgs).forEach(printStream::println);
        printStream.println(programs.length);
        Arrays.stream(programs).forEach(printStream::println);
        printStream.flush();
        Scanner scanner = new Scanner(process.getInputStream());
        Pattern pattern = Pattern.compile("(?<componentName>master|cache|worker)(\\s+(?<workerNum>\\d+))?\\s+start\\s+(?<pid>\\d+)\\s+(?<port>\\d+)");
        int i = 0;
        int processCount = waitForCache ? workerCount + 2 : workerCount + 1;
        while (i < processCount) {
            String line = scanner.nextLine();
            System.out.println(line);
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                String componentName = matcher.group("componentName");
                long pid = Long.parseLong(matcher.group("pid"));
                System.out.println(componentName);
                switch (componentName) {
                    case "master":
                        result.setMaster(ProcessHandle.of(pid).orElseThrow());
                        break;
                    case "cache":
                        if (!waitForCache) {
                            i--;
                        }
                        result.setCache(ProcessHandle.of(pid).orElseThrow());
                        break;
                    case "worker":
                        int workerNum = Integer.parseInt(matcher.group("workerNum"));
                        result.setWorker(workerNum, ProcessHandle.of(pid).orElseThrow());
                        break;
                }
                i++;
            }
        }

        if (!waitForCache) {
            Thread.sleep(500);
        }
        new Thread(() -> transfer(System.err, process.getErrorStream())).start();
        return result;
    }
}
