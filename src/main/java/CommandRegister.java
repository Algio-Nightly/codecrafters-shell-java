import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CommandRegister{
    protected static final Map<String, Consumer<ArrayList<String>>> FUNCTION_REGISTRY = new HashMap<>();
    private static Path activeBasedir = Paths.get("").toAbsolutePath();

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
    static void pwd(ArrayList<String> command){
        System.out.println(activeBasedir);
    }
    
    static void cd(ArrayList<String> command){
        String newPath = command.get(1);
        if (newPath.toString().equals("~")){
            String home = null;
            try {
                home = System.getenv("HOME");
            } catch (Exception e) {
                home = System.getProperty("user.home");
            }
            newPath = home;
        }
        if (newPath!=null){
            Path newActivePath = activeBasedir.resolve(Paths.get(newPath)).normalize().toAbsolutePath();
            if (Files.exists(newActivePath)){
                activeBasedir = newActivePath;
            } else {
                IO.println("cd: "+newActivePath.toString()+": No such file or directory");
            }
        }
    }
    
    static void runner(ArrayList<String> command){
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.inheritIO(); 
            Process p = pb.start();

            int exit_code = p.waitFor();
            // System.out.println();

        } catch (Exception e){
            System.out.println("Failed to execute Executable at"+ command.get(0));
            e.printStackTrace();
        }

    }

    static ArrayList<String> checkExecutable(ArrayList<String> command){
        String primary = command.get(0);
        String pathEnv = System.getenv("PATH"); 
        String[] paths = pathEnv.split(":");
        ArrayList<String> coms = (ArrayList<String>) command.clone();


        String full = null;

        for (String x:paths){
            Path fullPath = Paths.get(x, primary);
            if (Files.isExecutable(fullPath)) {
                full = primary;
                break;
            } else {
                full = null;
            }
        }
        coms.set(0, full);
        return coms;
    }


}