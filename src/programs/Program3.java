package programs;

import java.util.Scanner;

import static os.hw1.Config.WAIT_P3;


public class Program3 {
    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        Thread.sleep(WAIT_P3 - 200);
        System.out.println(scanner.nextInt());
    }
}
