package co.com.escuelaing.model.resources;

import co.com.escuelaing.model.Resource;

public class LambdaResource extends Resource {
    
    public Boolean prewarm = false;
    public Runtime runtime;
    public String handler;
    
    public static enum Runtime {
        GO,
        JAVA,
        PYTHON,
    }
}
