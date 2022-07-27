package os.hw1.master;

import os.hw1.master.cache.CacheHandler;
import os.hw1.master.query.Query;
import os.hw1.master.worker.JobDistributar;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class MasterMain {

    public static final int cache_port = 3950;
    public static final int worker_port = 2950;
    private int port;
    private int number_of_workers;
    private int weight;
    private int number_of_queries = 0;

    private List<String> arguments = new ArrayList<>();
    private int number_of_programs;
    private List<String> class_names = new ArrayList<>();
    private List<Integer> class_weights = new ArrayList<>();

    CacheHandler cacheHandler;

    JobDistributar jobDistributar;

    private ServerSocket server_socket;

    private List<Socket> user_sockets = new ArrayList<>();

    private TreeSet<Query> queries = new TreeSet<>();

    private List<Query> processing_queries = new ArrayList<>();

    public Object weight_lock = new Object();

    public List<Query> getProcessing_queries() {
        return processing_queries;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public int getNumber_of_workers() {
        return number_of_workers;
    }

    public int getWeight() {
        return weight;
    }

    public TreeSet<Query> getQueries() {
        return queries;
    }

    public List<String> getClass_names() {
        return class_names;
    }

    public List<Integer> getClass_weights() {
        return class_weights;
    }

    public static void main(String[] args) {

        MasterMain instance = new MasterMain();

        try {
            instance.main_method();
        } catch (Exception e) {
            Logger.getInstance().write_log(e);
            System.exit(-1);
        }

    }

    public void main_method() throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try {
                server_socket.close();
                cacheHandler.end();
                jobDistributar.end();
                System.out.println("master stop " + ManagementFactory.getRuntimeMXBean().getPid() + " " + port);
            } catch (IOException e) {
                Logger.getInstance().write_log(e);
            }
        }));
        get_input();
        jobDistributar = new JobDistributar(this);

        cacheHandler = new CacheHandler(this);

        establish_server();
        System.out.println("master start " + ManagementFactory.getRuntimeMXBean().getPid() + " " + port);
        jobDistributar.make_all_worker_processes();
        cacheHandler.make_cache_process();
//        master_logger.write_log("cache process has been made");
        jobDistributar.find_worker_for_query();

        listen_for_user_req();
    }
    private void get_input(){
        Scanner input = new Scanner(System.in);
        port = Integer.parseInt(input.nextLine());
        number_of_workers = Integer.parseInt(input.nextLine());
        weight = Integer.parseInt(input.nextLine());
        int number_of_args = Integer.parseInt(input.nextLine());
        for (int i = 0; i < number_of_args; i++) {
            arguments.add(input.nextLine());
        }
        number_of_programs = Integer.parseInt(input.nextLine());
        for (int i = 0; i < number_of_programs; i++) {
            String line = input.nextLine();
            var split_line = line.split(" ");
            class_names.add(split_line[0]);
            class_weights.add(Integer.parseInt(split_line[1]));
        }
    }


    private void establish_server() throws IOException {
        server_socket = new ServerSocket(port);
    }

//    private void stop(){
//        try {
//            server_socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

    private void listen_for_user_req() throws Exception {
        while(true){
            Socket socket = server_socket.accept();
            user_sockets.add(socket);
            Scanner scanner = new Scanner(socket.getInputStream());
            String query_string = scanner.nextLine();
            Query query = new Query(number_of_queries, query_string);
            number_of_queries += 1;
            Logger.getInstance().write_log("query arrived");
            synchronized (queries){
                queries.add(query);
                queries.notifyAll();
            }
        }
    }

    public void send_result_to_user(Query query){
        Socket socket = user_sockets.get(query.getPriority());
        try {
            PrintStream printStream = new PrintStream(socket.getOutputStream());
            printStream.println(query.getValue());
            printStream.flush();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send_result_to_cache(String program_number, int input_value, int result_value){
        cacheHandler.save_in_cache(program_number, input_value, result_value);
    }

    public Integer exist_in_cache(String program, int value){
        return cacheHandler.exist_in_cache(program, value);
    }
}
