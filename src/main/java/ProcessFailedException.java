public class ProcessFailedException extends Exception {
    private final String stdoutData;

    public ProcessFailedException(String errorMsg, String stdoutData) {
        super(errorMsg);
        this.stdoutData = stdoutData;
    }

    public String getStdoutData() {
        return stdoutData;
    }
}