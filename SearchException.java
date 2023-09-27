public class SearchException extends Exception{
    private final Throwable cause;

    public SearchException(String message) {
        super(message);
        this.cause = null;
    }
    public SearchException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }
}

