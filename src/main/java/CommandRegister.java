import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;


public class CommandRegister{
    class CommandException extends Exception{
        CommandException(String message){
            super(message);
        }
    }
    protected static final Map<String, Function<ArrayList<String>, String>> FUNCTION_REGISTRY = new HashMap<>();
    private static Path activeBasedir = Paths.get("").toAbsolutePath();

    static void set(){
        
    }
    static String echo(ArrayList<String> command){
        command.remove(0);
        String out = (String.join(" ", command));

        return out;
    }

    static String type(ArrayList<String> command){
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
        
        String out = null;
        
        if (FUNCTION_REGISTRY.containsKey(primary)){
            out = (primary + " is a shell builtin");
        } else if (full!=null){
            out = (primary + " is "+ full);
        } else {
            out = (primary+": not found");
        }

        return out;
    }
    static String pwd(ArrayList<String> command){
        String out = (activeBasedir.toString());
        return out;
    }
    
    static String cd(ArrayList<String> command){
        if (command.size()<2){
            return "cd missing path argument";
        }
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
        String out = null;
        if (newPath!=null){
            Path newActivePath = activeBasedir.resolve(Paths.get(newPath)).normalize().toAbsolutePath();
            if (Files.exists(newActivePath)){
                activeBasedir = newActivePath;
            } else {
                out = "cd: "+newActivePath.toString()+": No such file or directory";
            }
        }
        return out;
    }

    static String reader(ArrayList<String> command){
        Path filePath = Paths.get(command.get(1));
        
        String out = null;
        try (BufferedReader bf = Files.newBufferedReader(filePath)){

        } catch (Exception e){

        }
        return out;
    }

    static void writer(String[] command){
        String in = command[0];
        String fPath = command[1];
        Path filePath = getResolvedPath(fPath);


        try (BufferedWriter bw = Files.newBufferedWriter(filePath)) {
            bw.write(in);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Write error: " + e.getMessage());
        }

    }

    static Path getResolvedPath(String rawPathString){
        Path rawPath = Paths.get(rawPathString);
    
        Path resolvedPath = activeBasedir.resolve(rawPath).toAbsolutePath().normalize();

        return resolvedPath;
    }
    
    static String runner(ArrayList<String> command){
        String out = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            // pb.inheritIO(); 
            Process p = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))){
                String output = reader.lines().collect(Collectors.joining("\n"));
                int exitCode = p.waitFor();

                if (exitCode!=0){
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                    String errorMsg = errorReader.lines().collect(Collectors.joining("\n"));
                    
                    if (errorMsg.isEmpty()) {
                        errorMsg = "Command failed with exit code " + exitCode;
                    }
                    
                    throw new Exception(errorMsg);
                    } 

                }

                out = output;

            } catch (Exception e){
                System.err.println("Failed to capture command output: " + e.getMessage());
                // return "";
            }


        } catch (Exception e){
            out = ("Failed to execute Executable at"+ command.get(0));
            // e.printStackTrace();
        }

        return out;
    }

    @SuppressWarnings("unchecked")
    static ArrayList<String> checkExecutable(ArrayList<String> command){
        String primary = command.get(0);
        String pathEnv = System.getenv("PATH"); 
        String[] paths = pathEnv.split(":");
        ArrayList<String> coms = (ArrayList<String>) command.clone();


        String full = null;
        Path primaryPath = Paths.get(primary);
        if (Files.isExecutable(primaryPath)){
            coms.set(0, primaryPath.toAbsolutePath().toString());
            return coms;
        }
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