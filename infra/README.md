# Welcome to your CDK Java project!

First generate your templates

```bash
cdk synth > template.yaml
```

Now deploy your stack.

```bash
aws cloudformation deploy --stack-name template-generator --template-file template.yaml --capabilities CAPABILITY_IAM
```