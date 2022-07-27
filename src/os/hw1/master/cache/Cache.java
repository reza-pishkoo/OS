package os.hw1.master.cache;

import os.hw1.master.Logger;
import os.hw1.master.MasterMain;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

public class Cache {

    Socket socket;
    Scanner scanner;
    HashMap<Integer, Integer> saved_values = new HashMap<>();
    PrintStream printStream;

    public Cache() throws IOException {
        socket = new Socket(InetAddress.getLocalHost(), MasterMain.cache_port);
        printStream = new PrintStream(socket.getOutputStream());
    }

    public static void main(String[] args) throws IOException {
        Cache instance = new Cache();

        instance.listen_for_req();
    }

    private void listen_for_req() {
        try {
            scanner = new Scanner(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true) {
            String input = scanner.nextLine();
            var input_split = input.split(" ");
            if(input_split.length == 3){
                saved_values.put(Objects.hash(input_split[0], input_split[1]), Integer.parseInt(input_split[2]));
            }
            else if(input_split.length == 2){
                if(saved_values.containsKey(Objects.hash(input_split[0], input_split[1]))){
                    printStream.println(saved_values.get(Objects.hash(input_split[0], input_split[1])));
                }else
                    printStream.println("null");
                printStream.flush();
            }
        }
    }

}
