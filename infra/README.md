# Welcome to your CDK Java project!

You need to have the AWS CLI configured with your aws account.

First generate your templates

```bash
cdk synth > template.yaml
```

Now deploy your stack.

```bash
aws cloudformation deploy --stack-name template-generator --template-file template.yaml --capabilities CAPABILITY_IAM
```

Now you can check in cloud formation for the ec2 instance that has the service running.