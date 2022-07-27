package os.hw1.master.worker;

import os.hw1.master.Logger;
import os.hw1.master.MasterMain;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Worker {

    Socket socket = new Socket(InetAddress.getLocalHost(), MasterMain.worker_port);
    Scanner scanner;
    PrintStream printStream;
    public List<Process> programs = new ArrayList<>();

    public Worker() throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            for (int i = 0; i < programs.size(); i++) {
                programs.get(i).destroy();
            }
        }));
    }

    public static void main(String[] args) throws IOException {
        Worker instance = new Worker();
        instance.listen_for_req();
    }

    private void listen_for_req() {
        try {
            scanner = new Scanner(socket.getInputStream());
            printStream = new PrintStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            while(true) {
                String input = scanner.nextLine();
                Logger.getInstance().write_log("I got request : " + input);
                var input_split = input.split("&");
                Process process = make_program_progress(input_split[0]);
                send_req_to_program(process, input_split[1]);
                listen_for_program_response(process, input_split[2]);
            }
        }catch (Exception e){
            e.printStackTrace(printStream);
            printStream.flush();
        }
    }

    private Process make_program_progress(String process_input) throws IOException {
        Process process = new ProcessBuilder(process_input.split("=")).start();
//        System.err.println("debug >> created process. My Pid : " + ManagementFactory.getRuntimeMXBean().getPid() + " processPid : " + process.pid());
//        System.err.println("debug >> my children number : " + ProcessHandle.of(ManagementFactory.getRuntimeMXBean().getPid()).orElseThrow().children().collect(Collectors.toList()));
        programs.add(process);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Scanner sc = new Scanner(process.getErrorStream());
                while (true)
                    Logger.getInstance().write_log(sc.nextLine());
            }
        }).start();
        return process;
    }

    private void send_req_to_program(Process process, String value_input){
        PrintStream printStream = new PrintStream(process.getOutputStream());
        printStream.println(value_input);
        printStream.flush();
    }

    private void listen_for_program_response(Process process, String priority){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Scanner program_scanner = new Scanner(process.getInputStream());
                Logger.getInstance().write_log("debug >> start of thread");
                String program_result = program_scanner.nextLine();
                Logger.getInstance().write_log("debug >> response is sent");
                synchronized (printStream){
                    printStream.println(program_result + " " + priority);
                    printStream.flush();
                }
            }
        });
        thread.start();
    }


}
