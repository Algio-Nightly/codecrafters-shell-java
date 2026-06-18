public class CommandResult {
    public boolean success;
    public String output;
    public String error;

    public CommandResult(String output, boolean success) {
        this.success = success;
        this.output = output;
    }
}