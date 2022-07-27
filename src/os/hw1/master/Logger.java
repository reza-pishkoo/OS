package os.hw1.master;

import java.io.*;
import java.sql.Timestamp;

public class Logger {
    PrintStream printStream;
    boolean write = true;
    private static Logger instance;
    private String name;

    private Logger(){

    }

    public static Logger getInstance(){
        if(instance==null)
            instance = new Logger();
        return instance;
    }

    public void setName(String name){
        this.name = name;
    }

//    public Logger(String file) {
//        if(write) {
//            try {
//                this.printStream = new PrintStream(
//                        new FileOutputStream(file, true));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            printStream.println("------------------");
//        }
//    }

    public synchronized void write_log(String input){
        if(write)
            System.err.println(name + " >> " + input + " >> TIME : " + new Timestamp(System.currentTimeMillis()).toString());
    }

    public void write_log(Exception e) {
        if(write) {
            e.printStackTrace(printStream);
            printStream.flush();
        }
    }
}
