package os.hw1.master.worker;

import os.hw1.master.Logger;
import os.hw1.master.MasterMain;
import os.hw1.master.query.Query;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class WorkerHandler {
    Socket socket;
    MasterMain masterMain;
    ServerSocket worker_server_socket;
    public int worker_id;
    private int query_weight;
    Object lock;
    private JobDistributar jobDistributar;

    Scanner scanner;
    PrintStream printStream;
    Process process;

    private List<Query> worker_tasks = new ArrayList<>();

    public int getQuery_weight() {
        return query_weight;
    }

    public void setQuery_weight(int query_weight) {
        synchronized (masterMain.weight_lock) {
            this.query_weight = query_weight;
        }
    }

    public void end(){
        System.out.println("worker " + worker_id + " stop " + process.pid() + " " + socket.getPort());
        process.destroy();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public WorkerHandler(int id, MasterMain masterMain, JobDistributar jobDistributar, ServerSocket worker_server_socket, Object lock) {
        this.lock = lock;
        this.masterMain = masterMain;
        this.jobDistributar = jobDistributar;
        this.worker_server_socket = worker_server_socket;
        worker_id = id;
        make_worker_process();
        wait_for_worker_response();
        query_weight = masterMain.getWeight();
    }


    private void make_worker_process() {
        try {
            String[] cmd = new String[masterMain.getArguments().size() + 1];
            for (int i = 0; i < masterMain.getArguments().size() + 1; i++) {
                if(i < masterMain.getArguments().size())
                   cmd[i] = masterMain.getArguments().get(i);
                else
                    cmd[i] = "os.hw1.master.worker.Worker";
            }
            process = new ProcessBuilder(cmd).start();
            socket = worker_server_socket.accept();

            System.out.println("worker " + worker_id + " start " + process.pid() + " " + socket.getPort());
            scanner = new Scanner(socket.getInputStream());
            printStream = new PrintStream(socket.getOutputStream());
            synchronized (masterMain.weight_lock) {
                query_weight = masterMain.getWeight();
            }
            synchronized (jobDistributar.lock) {
                jobDistributar.lock.notifyAll();
            }

            Scanner sca = new Scanner(process.getErrorStream());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        Logger.getInstance().write_log("worker " + worker_id + " : " + sca.nextLine());
                    }
                }
            }).start();
        } catch (IOException e) {
            Logger.getInstance().write_log(e);
            e.printStackTrace();
        }
    }

    public void send_req(Query query){
        synchronized (masterMain.getQueries()){
            worker_tasks.add(query);
            masterMain.getProcessing_queries().add(query);
            masterMain.getQueries().remove(query);
        }
        Logger.getInstance().write_log("debug >> assignment " + "worker " + worker_id + "/ weight : " + query_weight);
        String current_program = query.getPrograms_row().peek();
        int input_number = query.getValue();
        String program_name = masterMain.getClass_names().get(Integer.parseInt(current_program) - 1);
        String proc_inp = String.join("=", masterMain.getArguments()) + "=" + program_name;
        printStream.println(proc_inp + "&" + input_number + "&" + query.getPriority());
        printStream.flush();
    }

    public void wait_for_worker_response(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        listen();
                    } catch (Exception e) {
                        System.out.println("worker " + worker_id + " stop " + process.pid() + " " + socket.getPort());
                        return_tasks();
                        make_worker_process();
                    }
                }
            }
        });
        thread.start();
    }

    private void return_tasks(){
        this.setQuery_weight(0);
        for (int i = 0; i < worker_tasks.size(); i++) {
            synchronized (masterMain.getQueries()) {
                Query query = worker_tasks.get(i);
                masterMain.getProcessing_queries().remove(query);
                masterMain.getQueries().add(query);
                worker_tasks.remove(query);
                masterMain.getQueries().notifyAll();
            }
            synchronized (jobDistributar.dead_worker_lock) {
                try {
                    jobDistributar.dead_worker_lock.wait();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void listen(){
        while(true){
            String response = scanner.nextLine();
            Logger.getInstance().write_log("worker " + worker_id + " responded " + response);
            var split_response = response.split(" ");
            String priority = split_response[1];
            String result_value = split_response[0];
            for (Query query:masterMain.getProcessing_queries()) {
                if(query.getPriority() == Integer.parseInt(priority)){

                    int program = Integer.parseInt(query.getPrograms_row().peek()) - 1;
                    this.setQuery_weight(this.getQuery_weight() + masterMain.getClass_weights().get(program));

                    masterMain.send_result_to_cache(query.getPrograms_row().peek(), query.getValue(), Integer.parseInt(result_value));
                    synchronized (masterMain.getQueries()){
                        masterMain.getProcessing_queries().remove(query);
                        worker_tasks.remove(query);
                        query.getPrograms_row().remove();
                        query.setValue(Integer.parseInt(result_value));
                        masterMain.getQueries().add(query);
                        masterMain.getQueries().notifyAll();
                        break;
                    }
                }
            }
            synchronized (lock){
                lock.notifyAll();
            }
        }
    }
}
