import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Data {
        public static void create() {
            int count = 12600;
            int maxNumber = 1_000;
            try {
                FileWriter writer = new FileWriter("data.txt");
                Random rand = new Random();
                for (int i = 0; i < count; i++) {
                    System.out.println(i);
                    writer.write(rand.nextInt(maxNumber) + 1 + "\n") ;

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public static void read() throws FileNotFoundException {
            Scanner scan = new Scanner(new File("data.txt"));

            ArrayList<Integer> a =new ArrayList<>() ;
            int number;
            int i =0 ;
            while (scan.hasNextLine()){
                System.out.println(i++);
                a.add(Integer.parseInt(scan.nextLine()));
            }
            System.out.println(a.size());
        }

        public static void main(String[] args) throws FileNotFoundException {
            create();
            read();
        }

    }

