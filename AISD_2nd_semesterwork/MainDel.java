import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class MainDel {
    public static void main(String[] args) throws FileNotFoundException {
        BTree bt = new BTree(8);
        Scanner sc = new Scanner(new File("data.txt"));
        int number = sc.nextInt();
        ArrayList<Integer> arr = new ArrayList<>();
        arr.add(number);
        while(number != 0){
            int ins = bt.insert(number);
            number = sc.nextInt();
            arr.add(number);
        }
        int [] a = new int [100];
        int i =0 ;
        while(i != 100){
            Random r = new Random();
            int ind = r.nextInt(arr.size());
            a[i] = arr.get(ind);
            i++;
        }
        for(int j = 0; j < a.length; j++){
            long toTime = System.nanoTime();
            int it = bt.remove(a[j]);
            long endTime = System.nanoTime();
            System.out.println(it );
        }
    }
}
