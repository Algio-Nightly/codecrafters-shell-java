import java.util.*;
import java.util.function.*;

public class Main {
    private static Map<String, Function<ArrayList<String>, String>> register = CommandRegister.FUNCTION_REGISTRY;
    private static ArrayList<Job> jobRegister = new ArrayList<>();

    
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
        }
        sc.close();
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
            return result.success?result.stdout:result.stderr;


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
            return parseRedirectionString(commands, background);   
        }
        
    }

    static CommandResult parseRedirectionString(ArrayList<String> commands, boolean background) throws Exception{
        if (commands.contains(">") || commands.contains("1>")) {
            int idx = commands.indexOf(">") != -1 ? commands.indexOf(">") : commands.indexOf("1>");
            return redirection(commands, idx, false, false, background);
            
        } else if (commands.contains(">>") || commands.contains("1>>")) {
            int idx = commands.indexOf(">>") != -1 ? commands.indexOf(">>") : commands.indexOf("1>>");
            return redirection(commands, idx, true, false, background);
            
        } else if (commands.contains("2>")) {
            int idx = commands.indexOf("2>");
            return redirection(commands, idx, false, true, background);
            
        } else if (commands.contains("2>>")) {
            int idx = commands.indexOf("2>>");
            return redirection(commands, idx, true, true, background);
        }
        return background?backgroundExecute(commands):execute(commands);
    }
    static CommandResult execute(ArrayList<String> commands) throws Exception {
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
                return CommandRegister.runner(commands);
            }
        }
        
    }


    static CommandResult backgroundExecute(ArrayList<String> commands) throws Exception{
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
        
    static CommandResult redirection(ArrayList<String> commands, int i, boolean append, boolean error, boolean background) throws Exception{
        ArrayList<String> subcommand = new ArrayList<>(commands.subList(0, i)); 
        String path = commands.get(i + 1);

        CommandResult result = background?backgroundExecute(subcommand):execute(subcommand);

        if (error) {
            CommandRegister.writer(new String[]{result.stderr, path}, append);
            
            return new CommandResult(result.stdout, result.success);
            
        } else {
            CommandRegister.writer(new String[]{result.stdout, path}, append);
            
            return new CommandResult(result.stdout, result.success);
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
