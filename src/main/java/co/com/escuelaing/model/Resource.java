package co.com.escuelaing.model;

import java.util.Map;

public class Resource {

    public String name;

    public ResourceType type;

    public Map<String, Object> data;

    public enum ResourceType {
        S3,
        LAMBDA,
        API_GATEWAY,
    }
}
