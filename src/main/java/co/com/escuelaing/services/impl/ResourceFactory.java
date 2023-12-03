package co.com.escuelaing.services.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import co.com.escuelaing.model.Resource;
import co.com.escuelaing.model.resources.ApiGatewayResource;
import co.com.escuelaing.model.resources.DynamoDBResource;
import co.com.escuelaing.model.resources.EC2Resource;
import co.com.escuelaing.model.resources.LambdaResource;
import co.com.escuelaing.model.resources.S3Resource;
import co.com.escuelaing.model.resources.ApiGatewayResource.ApiResource;
import co.com.escuelaing.model.resources.EC2Resource.OS;
import co.com.escuelaing.services.exceptions.TemplateException;
import software.amazon.awscdk.core.IResource;
import software.amazon.awscdk.core.RemovalPolicy;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.services.apigateway.IRestApi;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.ITable;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.ec2.IInstance;
import software.amazon.awscdk.services.ec2.IMachineImage;
import software.amazon.awscdk.services.ec2.Instance;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.MachineImage;
import software.amazon.awscdk.services.ec2.SubnetConfiguration;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.WindowsVersion;
import software.amazon.awscdk.services.iam.IRole;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.IFunction;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.IBucket;

public class ResourceFactory {

    public static IResource CreateResource(Stack stack, Resource resource) throws TemplateException {
        String uniqueRoleName = "LabRole" + System.currentTimeMillis();
        IRole role = Role.fromRoleArn(stack, uniqueRoleName, "arn:aws:iam:::123456789012:role/LabRole");

        switch (resource.type) {
            case LAMBDA:
                return CreateLambdaResource(stack, resource.name, (LambdaResource) resource, role);
            case S3:
                return CreateS3Resource(stack, resource.name, (S3Resource) resource);
            case EC2:
                return CreateEC2Resource(stack, resource.name, (EC2Resource) resource, role);
            case API_GATEWAY:
                return CreateAPIGatewayResource(stack, resource.name, (ApiGatewayResource) resource);
            case DYNAMO_DB:
                return CrateDynamoDBResource(stack, resource.name, (DynamoDBResource) resource);
            default:
                throw new TemplateException("Resource type not found", null);
        }
    }

    private static IBucket CreateS3Resource(Stack stack, String name, S3Resource resource) {
        Bucket bucket = Bucket.Builder.create(stack, name)
                .bucketName(name)
                .versioned(resource.versioned)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
        return bucket;
    }

    private static IInstance CreateEC2Resource(Stack stack, String name, EC2Resource resource, IRole role) {
        Vpc vpc = Vpc.Builder.create(stack, resource.vpc.name)
                .subnetConfiguration(Arrays.asList(
                        SubnetConfiguration.builder()
                                .subnetType(SubnetType.PUBLIC)
                                .name("public")
                                .build()))
                .build();

        Function<OS, IMachineImage> osResolver = (o) -> {
            if(o.equals(OS.AMAZON_LINUX)) {
                return MachineImage.latestAmazonLinux();
            }
            return MachineImage.latestWindows(WindowsVersion.WINDOWS_SERVER_2019_ENGLISH_CORE_BASE);
        };



        Instance instance = Instance.Builder.create(stack, name)
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.valueOf(resource.instance_type.toString())))
                .machineImage(osResolver.apply(resource.os))
                .vpc(vpc)
                .role(role)
                .build();

        return instance;
    }


    private static IRestApi CreateAPIGatewayResource(Stack stack, String name, ApiGatewayResource resource) {
        RestApi restApi = RestApi.Builder.create(stack, name)
            .restApiName(name)
            .cloudWatchRole(false)
            .build();

        software.amazon.awscdk.services.apigateway.IResource root = restApi.getRoot();

        for(ApiResource r: resource.resources) {
            software.amazon.awscdk.services.apigateway.Resource path = root.addResource(r.name);
            path.addMethod(r.method.toString());
            addResources(r, path);
        }

        return restApi;
    }

    private static void addResources(ApiResource apiResource, software.amazon.awscdk.services.apigateway.IResource parent) {
        if (apiResource.resources!=null) {
            for(ApiResource r: apiResource.resources) {
                software.amazon.awscdk.services.apigateway.Resource path = parent.addResource(r.name);
                path.addMethod(r.method.toString());
                addResources(r, path);
            }
        }
    }

    private static ITable CrateDynamoDBResource(Stack stack, String name, DynamoDBResource resource) {
        Table table = Table.Builder.create(stack, name)
            .tableName(name)
            .partitionKey(Attribute.builder().name(resource.partitionKey).type(AttributeType.STRING).build())
            .billingMode(BillingMode.PROVISIONED)
            .removalPolicy(RemovalPolicy.DESTROY)
            .build();

        return table;
    }

    private static IFunction CreateLambdaResource(Stack stack, String name, LambdaResource resource, IRole role) {
        Map<LambdaResource.Runtime, Runtime> map = new HashMap<>(){{
            put(LambdaResource.Runtime.GO, Runtime.GO_1_X);
            put(LambdaResource.Runtime.JAVA, Runtime.JAVA_11);
            put(LambdaResource.Runtime.PYTHON, Runtime.PYTHON_3_8);
        }};

        software.amazon.awscdk.services.lambda.Function function = software.amazon.awscdk.services.lambda.Function.Builder.create(stack, name)
            .functionName(name)
            .runtime(map.get(resource.runtime))
            .code(Code.fromAsset("."))
            .handler(resource.handler)
            .role(role)
            .build();

        return function;
    }
}
