package co.com.escuelaing.persistence;

public class GitException extends Exception {

    public GitException(String message, Exception e) {
        super(message, e);
    }

}
