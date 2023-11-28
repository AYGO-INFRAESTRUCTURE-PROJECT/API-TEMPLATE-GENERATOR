package co.com.escuelaing.services.impl;

import java.io.BufferedReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import co.com.escuelaing.cloud.AWSApp;
import co.com.escuelaing.cloud.AWSStack;
import co.com.escuelaing.model.WebStack;
import co.com.escuelaing.persistence.TemplateRepository;
import co.com.escuelaing.services.TemplateService;
import software.amazon.awscdk.core.App;
import software.amazon.awscdk.core.Environment;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.core.StageSynthesisOptions;
import software.amazon.awscdk.cxapi.CloudAssembly;
import software.amazon.awscdk.cxapi.CloudFormationStackArtifact;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.CfnBucket.S3KeyFilterProperty;

@Service
public class TemplateServiceImpl implements TemplateService {


    @Autowired
    private TemplateRepository templateRepository;

    @Override
    public String synthStack(WebStack stack) throws Exception {
        AWSApp app = new AWSApp(stack.stackName);

        AWSStack newStack = new AWSStack(
            app.awsApp,
            stack.stackName,
            StackProps.builder()
            .env(
                Environment.builder()
                .account(System.getenv("AWS_ACCOUNT"))
                .region("us-east-1")
                .build()
            )
            .build()
        );

        Bucket bucket = Bucket.Builder.create(newStack.stack, "test-bucket")
            .bucketName("test-bucket")
            .build();

        ObjectMapper mapper =  new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        CloudAssembly assembly = app.awsApp.synth();
        CloudFormationStackArtifact artifact = assembly.getStackArtifact(newStack.stack.getArtifactId());

        String template = artifact.getTemplateFile();
        Object e = artifact.getTemplate();
        try {
            String template_string = mapper.writeValueAsString(e);
            System.out.println(template_string);

            templateRepository.saveTemplate(template_string, stack.stackName);

            return template_string;
        } catch (JsonProcessingException e1) {
            e1.printStackTrace();
        }
        System.out.println(template);

        throw new Exception();
    } 
    
}
