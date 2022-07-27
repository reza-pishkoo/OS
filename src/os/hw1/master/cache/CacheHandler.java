package os.hw1.master.cache;

import os.hw1.master.MasterMain;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class CacheHandler {

    MasterMain masterMain;
    private ServerSocket cache_server_socket = new ServerSocket(masterMain.cache_port);
    private Socket socket;
    Scanner scanner;
    PrintStream printStream;
    Process process;

    public CacheHandler(MasterMain masterMain) throws IOException {
        this.masterMain = masterMain;
    }

    public void end(){
        try {
            System.out.println("cache stop " + process.pid() + " " + socket.getPort());
            cache_server_socket.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        process.destroy();
    }

    public void make_cache_process() {
        String[] cmd = new String[masterMain.getArguments().size() + 1];
        for (int i = 0; i < masterMain.getArguments().size() + 1; i++) {
            if(i < masterMain.getArguments().size())
                cmd[i] = masterMain.getArguments().get(i);
            else
                cmd[i] = "os.hw1.master.cache.Cache";
        }
        try {
            process = new ProcessBuilder(cmd).start();
            socket = cache_server_socket.accept();

            System.out.println("cache start " + process.pid() + " " + socket.getPort());

            scanner = new Scanner(socket.getInputStream());
            printStream = new PrintStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public synchronized void save_in_cache(String program_number, int input_value, int result_value){
        printStream.println(program_number + " " + input_value + " " + result_value);
        printStream.flush();
    }

    public Integer exist_in_cache(String program, int value){
        printStream.println(program + " " + value);
        printStream.flush();
        String res = "null";
        try {
            res = scanner.nextLine();
        }catch (Exception e){
            make_cache_process();
        }

        if(res.equalsIgnoreCase("null")){
            return null;
        }
        return Integer.parseInt(res);
    }

}
