package programs;

import java.util.Scanner;

import static os.hw1.Config.WAIT_P2;


public class Program2 {
    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        Thread.sleep(WAIT_P2 - 200);
        System.out.println((scanner.nextInt() / 2) % 5);
    }
}
