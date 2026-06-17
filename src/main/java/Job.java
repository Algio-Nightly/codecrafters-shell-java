import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class Job {
    int jobNo;
    long pid;
    String out;
    String err;
    ArrayList<String> command;


    Job(int jobNo, ArrayList<String> command){
        this.jobNo = jobNo;
        this.command = command;
    }

    public void startJob() throws Exception{
        ProcessBuilder pb = new ProcessBuilder(command);
        // pb.inheritIO(); 
        pb.redirectErrorStream(true);
        Process p = pb.start();
        pid = p.pid();

        Thread backgroundProcessThread = new Thread(() ->{

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))){
                String output = reader.lines().collect(Collectors.joining("\n"));
                int exitCode = p.waitFor();

                if (exitCode!=0){
                    if (output.isEmpty()) {
                        err = "Command failed with exit code " + exitCode;
                    } else {
                        err = output;
                    }
                } else {
                    out = output;
                }
            }  catch (Exception e) {
                err = e.getMessage();
            }
      }); 

      backgroundProcessThread.start();
    }  
}