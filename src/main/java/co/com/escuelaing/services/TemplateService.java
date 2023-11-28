package co.com.escuelaing.services;

import java.io.BufferedReader;

import co.com.escuelaing.model.WebStack;

public interface TemplateService {
    public String synthStack(WebStack stack) throws Exception;
}
