import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CommandRegister{
    protected static final Map<String, Consumer<ArrayList<String>>> FUNCTION_REGISTRY = new HashMap<>();

    static void set(){
        
    }
    static void echo(ArrayList<String> command){
        command.remove(0);
        System.out.println(String.join(" ", command));
    }

    static void type(ArrayList<String> command){
        String primary = command.get(1);
        if (FUNCTION_REGISTRY.containsKey(primary)){
            System.out.println(primary + " is a shell builtin");
        } else {
            System.out.println(primary+": not found");
        }
    }

}