package com.myorg;

import software.constructs.Construct;

import java.util.Arrays;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
// import software.amazon.awscdk.Duration;
// import software.amazon.awscdk.services.sqs.Queue;
import software.amazon.awscdk.services.ec2.Instance;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.MachineImage;
import software.amazon.awscdk.services.ec2.MultipartBody;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetConfiguration;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.UserData;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.iam.Role;

public class InfraStack extends Stack {
    public InfraStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public InfraStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Vpc vpc = Vpc.Builder.create(this, "my-vpc")
                .subnetConfiguration(Arrays.asList(
                        SubnetConfiguration.builder()
                                .subnetType(SubnetType.PUBLIC)
                                .name("public")
                                .build()))
                .restrictDefaultSecurityGroup(false)
                .build();

        SecurityGroup group = SecurityGroup.Builder.create(this, id)
                .securityGroupName("test-security-group-name")
                .vpc(vpc)
                .build();

        group.addIngressRule(Peer.anyIpv4(), Port.tcp(7000));
        group.addIngressRule(Peer.anyIpv4(), Port.tcp(22));

        UserData commandsUserData = UserData.forLinux();
        commandsUserData.addCommands(
                "sudo yum update -y",
                "sudo yum install -y git",
                "cd ~",
                "git clone https://github.com/AYGO-INFRAESTRUCTURE-PROJECT/API-TEMPLATE-GENERATOR.git --branch main",
                "sudo amazon-linux-extras install java-openjdk11 -y",
                "cd 'API-TEMPLATE-GENERATOR'",
                "./gradlew bootRun"
                );

        Instance instance = Instance.Builder.create(this, "my-ec2")
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MICRO))
                .machineImage(MachineImage.latestAmazonLinux2())
                .vpc(vpc)
                .securityGroup(group)
                .role(Role.fromRoleArn(this, "existing-role", "arn:aws:iam::866956573632:role/LabRole"))
                .keyName("project-aygo")
                .userData(commandsUserData)
                .build();
    }
}
