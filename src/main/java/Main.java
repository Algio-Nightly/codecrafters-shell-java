import java.rmi.server.ExportException;
import java.util.*;
import java.util.function.*;

public class Main {
    private static Map<String, Function<ArrayList<String>, String>> register = CommandRegister.FUNCTION_REGISTRY;
    private static Map<String, Function<ArrayList<String>, String>> operatorRegister = new HashMap<>();
    
    static{
        register.put("exit", null);
        register.put("echo", CommandRegister::echo);
        register.put("type", CommandRegister::type);
        register.put("pwd", CommandRegister::pwd);
        register.put("cd", CommandRegister::cd);
    }

    static {
        // operatorRegister.put(">", );

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
            
            
            String out = checkAndRun(commands); 
            
            if (out!=null){
                IO.println(out);
            } 
        }
        sc.close();
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
                out = CommandRegister.runner(commands);
            }
        }
        return out;
    }

    static String checkAndRun(ArrayList<String> commands){
        String out = null;
        String err = "";
        try{
            int found = -1;
            for (int i = 1; i < commands.size(); i++) {
                    String token = commands.get(i);
                    
                    if (token.equals(">") || token.equals("1>")) {
                        found = 1;
                        try {
                            ArrayList<String> subcommand = new ArrayList<>(commands.subList(0, i)); 
                            out = execute(subcommand);

                        } catch (ProcessFailedException p){
                            out = p.getStdoutData();
                            err = p.getMessage();
                        } finally {
                            String path = commands.get(i+1);
                            CommandRegister.writer(new String[]{out,path}, false);
                            out = err!=null?err:null;
                        }
                        return out.isEmpty()?null:out;
                    } else if (token.equals(">>") || token.equals("1>>")) {
                        found = 1;
                        try {
                            ArrayList<String> subcommand = new ArrayList<>(commands.subList(0, i)); 
                            out = execute(subcommand);

                        } catch (ProcessFailedException p){
                            out = p.getStdoutData();
                            err = p.getMessage();
                        } finally {
                            String path = commands.get(i+1);
                            CommandRegister.writer(new String[]{out,path}, true);
                            out = err!=null?err:null;
                        }
                        return out.isEmpty()?null:out;
                    } else if (token.equals("2>")){
                        found = 1;
                        try {
                            ArrayList<String> subcommand = new ArrayList<>(commands.subList(0, i)); 
                            out = execute(subcommand);

                        } catch (ProcessFailedException p){
                            out = p.getStdoutData();
                            err = p.getMessage();
                        } finally {
                            String path = commands.get(i+1);
                            CommandRegister.writer(new String[]{err,path}, false);
                        }
                        return out.isEmpty()?null:out;
                        // break;
                    } else if (token.equals("2>>")){
                        found = 1;
                        try {
                            ArrayList<String> subcommand = new ArrayList<>(commands.subList(0, i)); 
                            out = execute(subcommand);

                        } catch (ProcessFailedException p){
                            out = p.getStdoutData();
                            err = p.getMessage();
                        } finally {
                            String path = commands.get(i+1);
                            CommandRegister.writer(new String[]{err,path}, true);
                        }
                        return out.isEmpty()?null:out;
                        // break;
                    }    


            }
            if (found==-1){
                    out =  execute(commands);
            }

        }
        catch (Exception e){
            out = e.getMessage();
            // e.printStackTrace();
        }
        return out;
            
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
