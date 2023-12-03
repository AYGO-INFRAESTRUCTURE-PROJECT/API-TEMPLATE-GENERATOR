package co.com.escuelaing.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import co.com.escuelaing.cloud.AWSApp;
import co.com.escuelaing.cloud.AWSStack;
import co.com.escuelaing.model.Resource;
import co.com.escuelaing.model.WebStack;
import co.com.escuelaing.persistence.GitException;
import co.com.escuelaing.persistence.TemplateRepository;
import co.com.escuelaing.services.TemplateService;
import co.com.escuelaing.services.exceptions.TemplateException;
import software.amazon.awscdk.core.Environment;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.cxapi.CloudAssembly;
import software.amazon.awscdk.cxapi.CloudFormationStackArtifact;

@Service
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    private TemplateRepository templateRepository;

    @Override
    public String synthStack(WebStack stack) throws TemplateException {
        return this.synthetizeTemplate(stack);
    }

    @Override
    public String synthAndDeploy(WebStack stack) throws TemplateException {
        String template = synthetizeTemplate(stack);

        try {
            templateRepository.saveTemplate(template, stack.stackName);
        } catch (GitException e) {
            e.printStackTrace();
            throw new TemplateException(e.getMessage(), e);
        }

        return template;
    }

    private String synthetizeTemplate(WebStack stack) throws TemplateException {
        AWSApp app = new AWSApp(stack.stackName);

        AWSStack newStack = new AWSStack(
                app.awsApp,
                stack.stackName,
                StackProps.builder()
                        .env(
                                Environment.builder()
                                        .account(System.getenv("AWS_ACCOUNT"))
                                        .region(stack.region)
                                        .build())
                        .build());

        for(Resource r: stack.resources) {
            ResourceFactory.CreateResource(newStack.stack, r);
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        CloudAssembly assembly = app.awsApp.synth();
        CloudFormationStackArtifact artifact = assembly.getStackArtifact(newStack.stack.getArtifactId());

        Object e = artifact.getTemplate();
        try {
            String template_string = mapper.writeValueAsString(e);
            return template_string;
        } catch (JsonProcessingException e1) {
            throw new TemplateException(e.toString(), e1);
        }
    }
}
