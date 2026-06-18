public class CommandResult {
    public boolean success;
    public String output;
    public String anotherOut;

    public CommandResult(String output, boolean success) {
        this.success = success;
        this.output = output;
    }
    public CommandResult(String error, String anotherOut2, boolean success) {
        this.success = success;
        this.output = error;
        this.anotherOut = anotherOut2;
    }
}