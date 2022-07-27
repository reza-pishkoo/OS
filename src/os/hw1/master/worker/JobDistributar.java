package os.hw1.master.worker;

import os.hw1.master.Logger;
import os.hw1.master.MasterMain;
import os.hw1.master.query.Query;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JobDistributar {

    MasterMain masterMain;
    private ServerSocket worker_server_socket = new ServerSocket(masterMain.worker_port);
    private List<WorkerHandler> worker_handlers= new ArrayList<>();
    public Object lock = new Object();
    public Object dead_worker_lock = new Object();


    public JobDistributar(MasterMain masterMain) throws IOException {
        this.masterMain = masterMain;
    }

    public void end(){
        try {
            worker_server_socket.close();
        } catch (IOException e) {
            Logger.getInstance().write_log(e);
        }
        for (WorkerHandler w:worker_handlers) {
            w.end();
        }
    }
    public void make_all_worker_processes() throws IOException {
        for (int i = 0; i < masterMain.getNumber_of_workers(); i++) {
            WorkerHandler worker_handler = new WorkerHandler(i, masterMain, this, worker_server_socket, lock);
            worker_handlers.add(worker_handler);
        }
    }

    private WorkerHandler get_best_worker_handler(int weight){
        WorkerHandler res = null;
        synchronized (masterMain.weight_lock) {
            String str = "";
            for (WorkerHandler wh:worker_handlers) {
                str += "worker " + wh.worker_id + " weight : " + wh.getQuery_weight() + " / ";
            }
            Logger.getInstance().write_log("debug >> " + str);
            for (WorkerHandler worker_handler :
                    worker_handlers) {
                if (res == null) {
                    if (worker_handler.getQuery_weight() >= weight) {
                        res = worker_handler;
                    }
                } else {
                    if (res.getQuery_weight() < worker_handler.getQuery_weight()) {
                        res = worker_handler;
                    }
                }
            }
        }
        return res;

    }

    public void find_worker_for_query(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    if(masterMain.getQueries().size()==0){
                        synchronized (masterMain.getQueries()){
                            try {
                                masterMain.getQueries().wait();
                                continue;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    Query first_query = masterMain.getQueries().first();
                    if(first_query.getPrograms_row().size() == 0){
                        masterMain.send_result_to_user(first_query);
                        synchronized (masterMain.getQueries()){
                            masterMain.getQueries().remove(first_query);
                        }
                        continue;
                    }

                    Integer return_value_from_cache = masterMain.exist_in_cache(first_query.getPrograms_row().peek(), first_query.getValue());
                    if(return_value_from_cache != null){
                        first_query.getPrograms_row().remove();
                        first_query.setValue(return_value_from_cache);
                        synchronized (masterMain.getQueries()){
                            masterMain.getQueries().add(first_query);
                        }
                        continue;
                    }

                    int program = Integer.parseInt(first_query.getPrograms_row().peek()) - 1;
                    int weight = masterMain.getClass_weights().get(program);
                    WorkerHandler current_worker_handler = get_best_worker_handler(weight);
                    if(current_worker_handler == null){
                        synchronized (dead_worker_lock) {
                            dead_worker_lock.notifyAll();
                        }
                        synchronized (lock){
                            try {
                                lock.wait();
                                continue;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        continue;
                    }

                    current_worker_handler.setQuery_weight(current_worker_handler.getQuery_weight() - weight);
                    current_worker_handler.send_req(first_query);
                    synchronized (dead_worker_lock) {
                        dead_worker_lock.notifyAll();
                    }
                }
            }
        });
        thread.start();
    }
}
