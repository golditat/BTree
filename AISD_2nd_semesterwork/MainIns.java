import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class MainIns {
    public static void main(String[] args) throws FileNotFoundException {
        BTree bt = new BTree(8);
        Scanner sc = new Scanner(new File("data.txt"));
        int number = sc.nextInt();
        while(number != 0){
            long endTime = System.nanoTime();
            int ins = bt.insert(number);
            long toTime =System.nanoTime();
            number = sc.nextInt();
            System.out.println( ins);
        }
    }
}
