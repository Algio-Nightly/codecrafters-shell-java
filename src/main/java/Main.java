import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // Uncomment the code below to pass the first stage
        Scanner sc = new Scanner(System.in);
        System.out.print("$ ");
        String command = sc.next();
        System.out.println(command+": command not found");
    }
}
