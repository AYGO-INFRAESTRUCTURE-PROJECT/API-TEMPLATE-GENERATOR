package co.com.escuelaing.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import co.com.escuelaing.model.resources.ApiGatewayResource;
import co.com.escuelaing.model.resources.DynamoDBResource;
import co.com.escuelaing.model.resources.EC2Resource;
import co.com.escuelaing.model.resources.LambdaResource;
import co.com.escuelaing.model.resources.ResourceType;
import co.com.escuelaing.model.resources.S3Resource;

@JsonTypeInfo(use = Id.NAME, property = "type", include = As.EXISTING_PROPERTY, visible = true)
@JsonSubTypes(value = { 
        @JsonSubTypes.Type(value = S3Resource.class, name = "S3"),
        @JsonSubTypes.Type(value = EC2Resource.class, name = "EC2"),
        @JsonSubTypes.Type(value = ApiGatewayResource.class, name = "API_GATEWAY"),
        @JsonSubTypes.Type(value = LambdaResource.class, name = "LAMBDA"),
        @JsonSubTypes.Type(value = DynamoDBResource.class, name = "DYNAMO_DB") 
})
public abstract class Resource  {

    public String name;
    public ResourceType type;
}