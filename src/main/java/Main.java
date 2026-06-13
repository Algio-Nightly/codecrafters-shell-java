import java.util.*;
import java.util.function.Consumer;

public class Main {
    private static Map<String, Consumer<ArrayList<String>>> register = CommandRegister.FUNCTION_REGISTRY;
    
    static{
        register.put("exit", null);
        register.put("echo", CommandRegister::echo);
        register.put("type", CommandRegister::type);
        register.put("pwd", CommandRegister::pwd);
        register.put("cd", CommandRegister::cd);
    }

    
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
            if (register.containsKey(commands.get(0))){
                register.get(commands.get(0)).accept(commands);
            } else {
                if (CommandRegister.checkExecutable(commands).get(0)==null){
                    System.out.println(commands.get(0)+": command not found");
                } else {
                    CommandRegister.runner(commands);
                }
            }
        }
        sc.close();
    }


}
