package co.com.escuelaing.persistence;

public interface TemplateRepository {

    public void saveTemplate(String template, String name) throws GitException;

}
