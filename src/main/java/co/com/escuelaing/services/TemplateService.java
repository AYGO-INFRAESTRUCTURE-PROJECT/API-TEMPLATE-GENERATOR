package co.com.escuelaing.services;

import co.com.escuelaing.model.WebStack;
import co.com.escuelaing.services.exceptions.TemplateException;

public interface TemplateService {
    public String synthStack(WebStack stack) throws TemplateException;

    public String synthAndDeploy(WebStack stack) throws TemplateException;
}
