package programs;

import os.hw1.master.Logger;

import java.util.Scanner;

import static os.hw1.Config.WAIT_P1;


public class Program1 {
    public static void main(String[] args) throws InterruptedException {
//        Logger.getInstance().write_log("debug >> before scanner");
        Scanner scanner = new Scanner(System.in);
//        Logger.getInstance().write_log("debug >> before sleep");
        Thread.sleep(WAIT_P1 - 200);
//        Logger.getInstance().write_log("debug >> program sent");
        System.out.println(scanner.nextInt() - 1);
    }
}
