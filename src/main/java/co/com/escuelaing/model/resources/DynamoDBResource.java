package co.com.escuelaing.model.resources;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.com.escuelaing.model.Resource;

public class DynamoDBResource extends Resource {

    @JsonProperty(value = "partition_key")
    public String partitionKey;
}
