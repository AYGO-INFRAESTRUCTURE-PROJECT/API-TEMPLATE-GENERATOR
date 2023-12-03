package co.com.escuelaing.services.impl;

import java.util.Arrays;
import java.util.function.Function;

import co.com.escuelaing.model.Resource;
import co.com.escuelaing.model.resources.EC2Resource;
import co.com.escuelaing.model.resources.S3Resource;
import co.com.escuelaing.model.resources.EC2Resource.OS;
import co.com.escuelaing.services.exceptions.TemplateException;
import software.amazon.awscdk.core.IResource;
import software.amazon.awscdk.core.RemovalPolicy;
import software.amazon.awscdk.core.Stack;
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
import software.amazon.awscdk.services.s3.Bucket;

public class ResourceFactory {

    public static IResource CreateResource(Stack stack, Resource resource) throws TemplateException {
        switch (resource.type) {
            case S3:
                return CreateS3Resource(stack, resource.name, (S3Resource) resource);
            case EC2:
                return CreateEC2Resource(stack, resource.name, (EC2Resource) resource);
            default:
                throw new TemplateException("Resource type not found", null);
        }
    }

    private static IResource CreateS3Resource(Stack stack, String name, S3Resource resource) {
        Bucket bucket = Bucket.Builder.create(stack, name)
                .bucketName(name)
                .versioned(resource.versioned)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
        return bucket;
    }

    private static IResource CreateEC2Resource(Stack stack, String name, EC2Resource resource) {
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

        IRole existingRole = Role.fromRoleArn(stack, "exsiting-role", "arn:aws:iam:::123456789012:role/LabRole");

        Instance instance = Instance.Builder.create(stack, name)
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.valueOf(resource.instance_type.toString())))
                .machineImage(osResolver.apply(resource.os))
                .vpc(vpc)
                .role(existingRole)
                .build();

        return instance;
    }
}
