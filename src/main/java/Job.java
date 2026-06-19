import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Job {
    private static Map<String, Function<ArrayList<String>, String>> register = CommandRegister.FUNCTION_REGISTRY;
    int jobNo;
    long pid;
    String out;
    String err;
    ArrayList<String> command;
    boolean success;
    boolean isJobDone;


    Job(int jobNo, ArrayList<String> command){
        this.jobNo = jobNo;
        this.command = command;
    }

    public void startJob() throws Exception{
        
        ProcessBuilder pb = new ProcessBuilder(command);
        // pb.inheritIO(); 
        Process p = pb.start();
        pb.redirectErrorStream(true);
        pid = p.pid();
            
        Thread backgroundProcessThread = new Thread(() ->{


            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))){
                String output = reader.lines().collect(Collectors.joining("\n"));
                int exitCode = p.waitFor();
                isJobDone = true;

                if (exitCode!=0){
                    if (output.isEmpty()) {
                        err = "Command failed with exit code " + exitCode;
                    } else {
                        IO.println(output);
                    }
                } else {
                    if (output.isEmpty()) {
                        // err = "Command failed with exit code " + exitCode;
                    } else {
                        IO.println(output);
                    }
                    
                }
            }  catch (Exception e) {
                err = e.getMessage();
            }
      }); 

        backgroundProcessThread.start();
    }  


    static String execute(ArrayList<String> commands) throws Exception{
        String out = null;
        
        if (register.containsKey(commands.get(0))){
            out = register.get(commands.get(0)).apply(commands);
        } else {
            if (CommandRegister.checkExecutable(commands).get(0)==null){
                out = (commands.get(0)+": command not found");
                throw new Exception(out);
            } else {
                out = CommandRegister.runner(commands).stdout;
            }
        }
        return out;
    }
}