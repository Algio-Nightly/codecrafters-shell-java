import java.util.Scanner;
import java.util.*;

public class Main {

    // static String[] tokenizer(){

    //     return new String[5];
    // }
    public static void main(String[] args) throws Exception {
        // Uncomment the code below to pass the first stage
        Scanner sc = new Scanner(System.in);
        
        while (true){

            System.out.print("$ ");
            String command = sc.nextLine();
            ArrayList<String> commands = new ArrayList<>(Arrays.asList(command.split(" ")));
            if (commands.get(0).equals("exit")){
                break;
            }
            else if (commands.get(0).equals("echo")){
                echo(commands);
            } else{
                System.out.println(commands.get(0)+": command not found");
            }
        }
        sc.close();
    }

    static void echo(ArrayList<String> command){
        command.remove(0);
        System.out.println(String.join(" ", command));
    }
}
