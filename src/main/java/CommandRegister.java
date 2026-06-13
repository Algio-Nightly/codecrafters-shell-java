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
            System.out.println(primary + " is "+ full);
        } else {
            System.out.println(primary+": not found");
        }
    }

    static void runner(ArrayList<String> command){
        String primary = command.get(0);
        String pathEnv = System.getenv("PATH"); 
        String[] paths = pathEnv.split(":");

        String full = null;

        for (String x:paths){
            Path fullPath = Paths.get(x, primary);
            if (Files.isExecutable(fullPath)) {
                full = fullPath.toAbsolutePath().toString();
            }
        }
        command.set(0, full);

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            Process p = pb.start();

            int exit_code = p.waitFor();
            // System.out.println();

        } catch (Exception e){
            System.out.println("Failed to execute Executable at"+ command.get(0));
            e.printStackTrace();
        }


    }

}