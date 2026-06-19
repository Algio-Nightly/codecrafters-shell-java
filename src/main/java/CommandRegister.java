import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.List;


public class CommandRegister{
    class CommandException extends Exception{
        CommandException(String message){
            super(message);
        }
    }
    protected static final Map<String, Function<ArrayList<String>, String>> FUNCTION_REGISTRY = new HashMap<>();
    protected static ArrayList<Job> JOB_REGISTER = new ArrayList<>();

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

    static String jobs (ArrayList<String> command){
        StringBuilder sb = new StringBuilder();
        List<Job> runningJobs = new ArrayList<>();
        for (Job j:JOB_REGISTER){
                runningJobs.add(j);
        }
        int numJobs = runningJobs.size();
        for (int i=0; i<numJobs; i++){
            Job j = runningJobs.get(i);
            String prefix;
            if (i==numJobs-1){
                prefix = "+";
            } else if (i==numJobs-2){
                prefix = "-";
            } else {
                prefix = " ";
            }
            if (j.isJobDone){
                sb.append("[%d]%s  Done                    %s".formatted(j.jobNo, prefix, String.join(" ", j.command)));
                // sb.append("[1 ] +  Done                    sleep 1".formatted(j.jobNo, prefix, String.join(" ", j.command)));
                sb.append("\n");
                JOB_REGISTER.remove(i);
            } else {
                sb.append("[%d]%s  Running                 %s &".formatted(j.jobNo, prefix, String.join(" ", j.command)));
                sb.append("\n");
            }
        }

        return sb.toString().trim();
    }


    static void writer(String[] command, boolean append){
        String in = command[0];
        String fPath = command[1];
        Path filePath = getResolvedPath(fPath);


        try (BufferedWriter bw = append?Files.newBufferedWriter(filePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND):Files.newBufferedWriter(filePath)) {
            if (in != null && !in.trim().isEmpty()) {
                bw.write(in);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Write error: " + e.getMessage());
        }

    }

    static Path getResolvedPath(String rawPathString){
        Path rawPath = Paths.get(rawPathString);
    
        Path resolvedPath = activeBasedir.resolve(rawPath).toAbsolutePath().normalize();

        return resolvedPath;
    }
    
    static CommandResult runner(ArrayList<String> command, String stdin) throws Exception{
        ProcessBuilder pb = new ProcessBuilder(command);
        // pb.inheritIO(); 
        Process p = pb.start();

        if (stdin != null && !stdin.isEmpty()) {
            java.io.OutputStream os = p.getOutputStream();
            os.write(stdin.getBytes());
            os.flush();
            os.close();
        }


        BufferedReader bfo = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String stdout = bfo.lines().collect(Collectors.joining("\n"));
        bfo.close();
                            
        BufferedReader bfe = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String stderr = bfe.lines().collect(Collectors.joining("\n"));
        bfo.close();

        int exitCode = p.waitFor();
    

        return new CommandResult(stdout, stderr, exitCode==0);
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

