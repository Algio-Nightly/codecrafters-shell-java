import java.util.*;
import java.util.function.*;

public class Main {
    private static Map<String, Function<ArrayList<String>, String>> register = CommandRegister.FUNCTION_REGISTRY;
    private static ArrayList<Job> jobRegister = CommandRegister.JOB_REGISTER;

    
    static{
        register.put("exit", null);
        register.put("echo", CommandRegister::echo);
        register.put("type", CommandRegister::type);
        register.put("pwd", CommandRegister::pwd);
        register.put("cd", CommandRegister::cd);
        register.put("jobs", CommandRegister::jobs);
    }

    
    public static void main(String[] args) throws Exception {
        // Uncomment the code below to pass the first stage
        Scanner sc = new Scanner(System.in);
        
        while (true){
            System.out.print("$ ");
            String command = sc.nextLine();

            
            ArrayList<String> commands = resolve(command);

            if (commands.get(0).equals("exit")){
                break;
            } 
            
            
            String out = parseAndRun(commands);
            if (out!=null){
                out = out.isEmpty()?null:out;
            }

            
            if (out!=null){
                IO.println(out);
            }
            String d = displayDoneJobs();
            if (!d.isEmpty()){
                IO.println(d);
            }

            
        }
        sc.close();
    }

    static String displayDoneJobs(){

        StringBuilder sb = new StringBuilder();
        List<Job> runningJobs = new ArrayList<>();
        for (Job j:jobRegister){
            if (j.isJobDone){
                runningJobs.add(j);
            }
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
                jobRegister.remove(j);
            } else {
                sb.append("[%d]%s  Running                 %s &".formatted(j.jobNo, prefix, String.join(" ", j.command)));
                sb.append("\n");
            }
        }

        return (sb.toString().trim());
    }


    static String parseAndRun(ArrayList<String> commands){
        try{
            String token = commands.get(commands.size()-1);
            CommandResult result;
            if (token.equals("&")){
                ArrayList<String> subcommand = new ArrayList<>(commands.subList(0, commands.size()-1));
                 result = parseLogical(subcommand, true);
            } else {
                result = parseLogical(commands, false);
            }
            StringBuilder combinedOut = new StringBuilder();
            
            if (result.stdout != null && !result.stdout.isEmpty()) {
                combinedOut.append(result.stdout);
            }
            
            if (result.stderr != null && !result.stderr.isEmpty()) {
                if (combinedOut.length() > 0) combinedOut.append("\n"); 
                combinedOut.append(result.stderr);
            }
            
            return combinedOut.toString();


        } catch (Exception e){
            return e.getMessage();
        }
    }

    
    static CommandResult parseLogical(ArrayList<String> commands, boolean background) throws Exception{
        int index = -1;
        CommandResult out;
        if (commands.contains("&&")){
            index = commands.indexOf("&&");
            out = parseLogical(new ArrayList<>(commands.subList(0, index)), background);
            if (out.success){
                return parseLogical(new ArrayList<>(commands.subList(index+1, commands.size())), background);
            } else {
                return out;
            }
        } else if (commands.contains("||")){
            index = commands.indexOf("||");
            out = parseLogical(new ArrayList<>(commands.subList(0, index)), background);
            if (!out.success){
                return parseLogical(new ArrayList<>(commands.subList(index+1, commands.size())),background);
            } else {
                return out;
            }
        } else {
            return parsePipeline(commands,"", background);   
        }
        
    }
    
    static CommandResult parsePipeline(ArrayList<String> commands, String stdin, boolean background) throws Exception{
        if (commands.contains("|")){
            int index = commands.indexOf("|");
            ArrayList<String> subcommand =  new ArrayList<>(commands.subList(0, index));
            String stdout = parseRedirectionString(subcommand, stdin, background).stdout;
            ArrayList<String> subcommand2 =  new ArrayList<>(commands.subList(index+1, commands.size()));
            subcommand2.add(1, stdout);
            return parsePipeline(subcommand2, stdout, background);
            
        } else {
            return parseRedirectionString(commands, stdin,background);
        }
        
    }

    static CommandResult parseRedirectionString(ArrayList<String> commands, String stdin, boolean background) throws Exception{
        if (commands.contains(">") || commands.contains("1>")) {
            int idx = commands.indexOf(">") != -1 ? commands.indexOf(">") : commands.indexOf("1>");
            return redirection(commands, idx, false, false, background, stdin);
            
        } else if (commands.contains(">>") || commands.contains("1>>")) {
            int idx = commands.indexOf(">>") != -1 ? commands.indexOf(">>") : commands.indexOf("1>>");
            return redirection(commands, idx, true, false, background, stdin);
            
        } else if (commands.contains("2>")) {
            int idx = commands.indexOf("2>");
            return redirection(commands, idx, false, true, background, stdin);
            
        } else if (commands.contains("2>>")) {
            int idx = commands.indexOf("2>>");
            return redirection(commands, idx, true, true, background, stdin);
        }
        return background?backgroundExecute(commands, stdin):execute(commands, stdin);
    }

    static CommandResult execute(ArrayList<String> commands, String stdin) throws Exception {
        if (register.containsKey(commands.get(0))){
            try {
                    return new CommandResult(register.get(commands.get(0)).apply(commands), true) ;
            } catch (Exception e){
                    return new CommandResult(e.getMessage(), false) ;
            }
        } else {
            if (CommandRegister.checkExecutable(commands).get(0)==null){
                return new CommandResult("", commands.get(0)+": command not found", false);
                
            } else {
                return CommandRegister.runner(commands, stdin);
            }
        }
        
    }


    static CommandResult backgroundExecute(ArrayList<String> commands, String stdin) throws Exception{
        String out = null;
        boolean success = true;

        int newJobNo = jobRegister.size()+1;
        try {
            Job j = new Job(newJobNo, commands);
            jobRegister.add(j);
            j.startJob();
            out = "["+j.jobNo+"] "+j.pid;
        } catch (Exception e) {
            success = false;
            out = e.getMessage();
        }


        return new CommandResult(out, success);
    }
        
    static CommandResult redirection(ArrayList<String> commands, int i, boolean append, boolean error, boolean background, String stdin) throws Exception{
        ArrayList<String> subcommand = new ArrayList<>(commands.subList(0, i)); 
        String path = commands.get(i + 1);

        CommandResult result = background?backgroundExecute(subcommand, stdin):execute(subcommand, stdin);

        if (error) {
            CommandRegister.writer(new String[]{result.stderr, path}, append);
            
            return new CommandResult(result.stdout, result.success);
            
        } else {
            CommandRegister.writer(new String[]{result.stdout, path}, append);
            
            return new CommandResult("", result.stderr, result.success);
        }
    }
        
    
            
    static ArrayList<String> resolve(String command){
        ArrayList<String> resolvedCommand = new ArrayList<>(); 

        StringBuilder currentToken = new StringBuilder();

        boolean inDoubleQuotes = false;
        boolean inSingleQuotes = false;
        boolean isEscaped = false;

        for (char c:command.toCharArray()){
            if (c=='\\' && !isEscaped && !inSingleQuotes){
                isEscaped = true;
                continue;
            }
            if (isEscaped) {
                currentToken.append(c);
                isEscaped = false;
                continue;
            }
            if (c == '\'' && !inDoubleQuotes) {
                inSingleQuotes = !inSingleQuotes;
                continue;
            }
        
            if (c == '"' && !inSingleQuotes) {
                inDoubleQuotes = !inDoubleQuotes;
                continue;
            }

            if (Character.isWhitespace(c) && !inDoubleQuotes && !inSingleQuotes){
                if (currentToken.length() > 0) {
                    resolvedCommand.add(currentToken.toString());
                    currentToken.setLength(0);
                }
            }
            else {
                currentToken.append(c);
            }
            
        }
        if (currentToken.length() > 0) {
            resolvedCommand.add(currentToken.toString());
        }
        return resolvedCommand;


    
    }

}
