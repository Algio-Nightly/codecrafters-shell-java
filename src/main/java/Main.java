import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static Map<String, Consumer<ArrayList<String>>> register = CommandRegister.FUNCTION_REGISTRY;
    
    static{
        register.put("exit", null);
        register.put("echo", CommandRegister::echo);
        register.put("type", CommandRegister::type);
        register.put("pwd", CommandRegister::pwd);
        register.put("cd", CommandRegister::cd);
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
            if (register.containsKey(commands.get(0))){
                register.get(commands.get(0)).accept(commands);
            } else {
                if (CommandRegister.checkExecutable(commands).get(0)==null){
                    System.out.println(commands.get(0)+": command not found");
                } else {
                    CommandRegister.runner(commands);
                }
            }
        }
        sc.close();
    }

    static ArrayList<String> resolve(String command){
        ArrayList<String> resolvedCommand = new ArrayList<>(); 
        // command = command.replaceAll("\"\"|\'\'", "");
        // command = command.replaceAll("'\\S+'", " ");
        // Pattern p = Pattern.compile("\"([^\"]*)\"|\'([^\']*)\'|(\\S+)");
        // Matcher m = p.matcher(command);
        // while (m.find()){
        //     if (m.group(1)!=null){
        //         resolvedCommand.add(m.group(1));
        //     } else if (m.group(2)!=null){
        //         resolvedCommand.add(m.group(2));
        //     } else { 
        //         resolvedCommand.add(m.group());
        //     }
        // }

        StringBuilder currentToken = new StringBuilder();

        boolean inDoubleQuotes = false;
        boolean inSingleQuotes = false;
        boolean isEscaped = false;

        for (char c:command.toCharArray()){
            if (c=='\\'){
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
