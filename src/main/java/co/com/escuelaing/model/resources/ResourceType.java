package co.com.escuelaing.model.resources;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT) 
public enum ResourceType {
    S3,
    LAMBDA,
    API_GATEWAY,
    EC2,
    DYNAMO_DB,
}
