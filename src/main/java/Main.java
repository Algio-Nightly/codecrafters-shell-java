import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // Uncomment the code below to pass the first stage
        Scanner sc = new Scanner(System.in);
        
        while (true){

            System.out.print("$ ");
            String command = sc.nextLine();
            String[] commands = command.split(" ");
            if (commands[0].equals("exit")){
                break;
            }
            System.out.println(commands[0]+": command not found");
        }
        sc.close();
    }
}
