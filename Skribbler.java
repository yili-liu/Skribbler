import java.io.*;

public class Skribbler {

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Welcome to Skribbler");
        while (true){
            String s = br.readLine();
            if (s.length() == 0) {
                System.out.println("Exiting Skribbler");
                break;
            }
            String query = "https://pixabay.com/api/?key=&q=&image_type=photo";
            System.out.println(s);
        }
    }
}