package co.com.escuelaing.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


public class WebStack {
   
    @JsonProperty(value = "stack_name")
    public String stackName;

    @JsonProperty(value = "resources")
    public List<Resource> resources;
}
