package co.com.escuelaing.model.resources;

import java.util.List;

import co.com.escuelaing.model.Resource;

public class ApiGatewayResource extends Resource {
    
    public List<ApiResource> resources;

    public static class ApiResource {
        public Method method;
        public String name;
        public List<ApiResource> resources;
    }

    public static enum Method {
        GET,
        POST,
        DELETE,
        PUT,
        PATCH,
    }
}
