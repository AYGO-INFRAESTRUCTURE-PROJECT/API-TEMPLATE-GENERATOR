package co.com.escuelaing.services.exceptions;

public class TemplateException extends Exception {

    public TemplateException(String message, Exception e) {
        super(message, e);
    }

}
