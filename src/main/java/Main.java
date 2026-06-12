import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // Uncomment the code below to pass the first stage
        Scanner sc = new Scanner(System.in);
        
        while (true){
            System.out.print("$ ");
            String command = sc.nextLine();
            System.out.println(command.split(" ")[0]+": command not found");
        }
    }
}
