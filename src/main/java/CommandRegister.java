import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        // This retrieves the exact list of executable directories from the host environment
        String pathEnv = System.getenv("PATH"); 
        String[] paths = pathEnv.split(":");

        String full = null;

        for (String x:paths){
            Path fullPath = Paths.get(x, primary);
            if (Files.isExecutable(fullPath)) {
                full = fullPath.toAbsolutePath().toString();
            }
        }
            

        if (FUNCTION_REGISTRY.containsKey(primary)){
            System.out.println(primary + " is a shell builtin");
        } else if (full!=null){
            System.out.println(primary + " is a "+ full);
        } else {
            System.out.println(primary+": not found");
        }
    }

}