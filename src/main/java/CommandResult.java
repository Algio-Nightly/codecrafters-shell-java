public class CommandResult {
    public boolean success;
    public String stdout;
    public String stderr;

    public CommandResult(String output, boolean success) {
        this.success = success;
        this.stdout = output;
        this.stderr = "";
    }
    public CommandResult(String output, String error, boolean success) {
        this.success = success;
        this.stdout = output;
        this.stderr = error;
    }
}