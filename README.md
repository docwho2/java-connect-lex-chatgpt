# AWS Connect ChatGPT via Lex in Java

## Background

This project demonstrates a full working model with UI that shows how to configure all of the resources to support calling Java [Lambda functions from AWS RDS Postgress](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/PostgreSQL-Lambda.html).  If you're already a lambda guru and just want to see how to build some kind of payload in psql then simply take a look at the [Trigger function](src/main/resources/scripts/LambdaTriggerFuction.sql).

Why even use this feature?  If you have many code bases that are writing data to Postgres, then how do you ensure that downline processing is done correctly on all data.  We typically fire off lambdas directly in code or push the id of the record to SNS or SQS.  This all works fine if all the places you insert data call the correct downline function.  But sometimes you simply need to edit or update the data directly in Postico or even just plain SQL.  By having Postgres call the Lambda you completely decouple the downline logic from the part of the appplication that inserts/updates/deletes the data.

Things to consider:
- If you fire off lambdas for row updates that themselves update the DB then you can quickly exaust connection and lambda resources.  If you update 1000 rows then you could have hundreds of lambdas fire and connect to the DB bringing everything to a grinding halt.  Therefore you should set [concurrency limits](https://aws.amazon.com/blogs/compute/managing-aws-lambda-function-concurrency/) on these lambda functions in most cases.
- When you exceed the limit, you get a throttle on the lambda, and these will delay processing.  In some cases this is fine.  In the Geo coding use case we don't need the Geo coding to complete immediately, so it's OK.  In testing 1K+ row updates for Geo coding some row updates took over 1 minute to complete because of the retry back off.
- If you need smoother execution of the events, then consider using a simple NodeJS Lambda (that won't be throttled) that takes the event payload and puts it on a SQS queue that another Lambda will process from the queue.  See a NodeJS example of this in the [ForwardToSQS.js](ForwardToSQS.js) file.

Three use cases are covered in this demo:
- A Lambda([PostgresAddressTrigger.java](src/main/java/demo/PostgresAddressTrigger.java)) that will update the same row that the trigger is firing on.  In this case, we have an address table, and when addresses are inserted or updated, they will be geo-coded by the [AWS Location API](https://docs.aws.amazon.com/location/latest/APIReference/API_SearchPlaceIndexForText.html).  Special care is needed in this case to prevent recursive triggering of the function.
- A Lambda ([PostgresAuditLogTrigger.java](src/main/java/demo/PostgresAuditLogTrigger.java)) that will simply log all actions on the address table to an audit_log table.  As shown in some AWS examples, you could then simply put the payload onto a SNS Topic or SQS Queue for downline processing.
- A Lambda ([PostgresAuditLogTriggerSQS.java](src/main/java/demo/PostgresAuditLogTriggerSQS.java)) that will simply log all actions on the address table to an audit_log_sqs table.  This shows how to process the same event above from a SQS Queue.

Other goals of the project:
- SAM CloudFormation example for all the components in play (`sam build` and then `sam deploy`) for simple deployment of the project.
- Managed RDS Secret for connecting to the DB and use of the [AWS JDBC Driver](https://docs.aws.amazon.com/secretsmanager/latest/userguide/retrieving-secrets_jdbc.html).
- [Custom Resource](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/template-custom-resources.html) to initialize the DB after creation ([CloudFormationCustomResource.java](src/main/java/demo/CloudFormationCustomResource.java)).  Namely to enable the lambda extensions and create all the SQL resources necessary in Postgres.
- Nested Stacks.
- To simply provide a full working example with Java and AWS RDS Postgres (what I use day to day).  The Demo on the AWS Website is MySQL with NodeJS and there was nothing I could find that really showed a full use case in Java.

## High Level Architecture
![Architecture Diagram](arch.jpg)

## Contents
This project contains source code and supporting files for a serverless application that you can deploy with the SAM CLI. It includes the following files and folders.

- [/src/main/java/demo](src/main/java/demo) - Java Lambda Functions
- [/src/main/resources/scripts](src/main/resources/scripts) - SQL Scripts used to initialize the DB from the [Custom Resource](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/template-custom-resources.html) ([CloudFormationCustomResource.java](src/main/java/demo/CloudFormationCustomResource.java)) in CloudFormation.
- CloudFormation scripts for all AWS resources
	- [vpc.yaml](vpc.yaml) - Creates simple VPC with 2 public subnets
	- [postgres.yaml](postgres.yaml) - Creates Auora Postgres Cluster with single serverlessV2 node and permissions to execute lambda functions.
	- [template.yaml](template.yaml) - Creates all the SAM lambda functions and associated AWS resources.


## Deploy the Project

The Serverless Application Model Command Line Interface (SAM CLI) is an extension of the AWS CLI that adds functionality for building and testing Lambda applications.  
Before proceeding, it is assumed you have valid AWS credentials setup with the AWS CLI and permissions to perform CloudFormation stack operations.

To use the SAM CLI, you need the following tools.

* SAM CLI - [Install the SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html)
* Java11 - [Install the Java 11](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/downloads-list.html)
* Maven - [Install Maven](https://maven.apache.org/install.html)

If you have brew installed then
```bash
brew install aws-sam-cli
brew install corretto11
brew install maven
```

To build and deploy, run the following in your shell after you have cloned the repo:

```bash
java-connect-lex-chatgpt$ ./init.bash
java-connect-lex-chatgpt$ sam build
java-connect-lex-chatgpt$ sam deploy
```

The first command will will setup some required components like the V4 Java Events library that is not published yet (this is a sub-module) and install the parent POM used by Lambda functions.
The second command will build the source of the application. 
The third command will package and deploy the project to AWS as a CloudFormation Stack.  
You will see the progress as the stack deploys.


`Do not forget to delete the stack or you will continue to incure AWS charges for the resources`.  




## Associate Phone number to the Connect Flow and place calls in

![Demo User Interface](UI.png)

When you open the API GW Endpoint URL in your browser you will see the above UI.  When the DB initializes, one address row is inserted for you, so you should see 1 row with an address Geo encoded and 2 entries in the `audit_log` tables.  The UI displays all the rows in both `address` and `audit_log` tables.  There are four actions to perform in the demo:

- Add Row to Address Table - This adds Apple's HQ address to the table.  Normally after the refresh you would see the row without geo encoding.  If you then hit `Refresh`, you should see the Geo data populated into the row.
- Add 5 Rows to Address Table - This adds 5 different addresses to the table.  Normally after the refresh you would see the rows without geo encoding.  If you then hit `Refresh`, you should see the Geo data populated into the rows.  Due to throttling this last one might be delayed a little.
- Delete Last Address - This deletes the last address row from the `address` table.  You should see the last row go away and an `audit_log` row for the delete action.
- Refresh - Does a simple refresh of the page (which reads all the tables again)
- Clear Audit Log - Truncates the `audit_log` tables.

## Fetch, tail, and filter Lambda function logs

To simplify troubleshooting, SAM CLI has a command called `sam logs`. `sam logs` lets you fetch logs generated by the deployed Lambda functions from the command line. In addition to printing the logs on the terminal, this command has several nifty features to help you quickly see what's going on with the demo.


```bash
java-connect-lex-chatgpt$ sam logs --tail
```

You can find more information and examples about filtering Lambda function logs in the [SAM CLI Documentation](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-logging.html).


## Cleanup

To delete the demo, use the SAM CLI. `DO NOT FORGET TO RUN THIS OR YOU WILL CONTINUE TO BE CHARGED FOR AWS RESOURCES`.  
Prior to deleting the stack, you should ensure you have disassociated any phone numbers pointing to the Connect Flow.

You can run the following:

```bash
java-connect-lex-chatgpt$ sam delete
```

## Sample Deploy Output
```bash
java-connect-lex-chatgpt$ sam deploy

		Managed S3 bucket: aws-sam-cli-managed-default-samclisourcebucket
		A different default S3 bucket can be set in samconfig.toml
		Or by specifying --s3-bucket explicitly.
File with same data already exists at 80aa0fed5827b7a80fa780734e9c4c09, skipping upload                                                                                                            
File with same data already exists at 8a3b643e9487598224c935024ab7de90, skipping upload                                                                                                            
File with same data already exists at d6f1fe447c5e6b528ef821e8612cc5c3, skipping upload                                                                                                            

	Deploying with following values
	===============================
	Stack name                   : connect-chatgpt
	Region                       : us-east-1
	Confirm changeset            : True
	Disable rollback             : False
	Deployment s3 bucket         : aws-sam-cli-managed-default-samclisourcebucket
	Capabilities                 : ["CAPABILITY_IAM"]
	Parameter overrides          : {}
	Signing Profiles             : {}

Initiating deployment
=====================

	Uploading to f837836f7f1a4ab21d28677fcf6980e3.template  36058 / 36058  (100.00%)


Waiting for changeset to be created..

CloudFormation stack changeset
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
Operation                                       LogicalResourceId                               ResourceType                                    Replacement                                   
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
+ Add                                           BotAlias                                        AWS::Lex::BotAlias                              N/A                                           
+ Add                                           BotRuntimeRole                                  AWS::IAM::Role                                  N/A                                           
+ Add                                           BotVersion                                      AWS::Lex::BotVersion                            N/A                                           
+ Add                                           BucketKey                                       AWS::KMS::Key                                   N/A                                           
+ Add                                           BucketPolicy                                    AWS::S3::BucketPolicy                           N/A                                           
+ Add                                           CallTable                                       AWS::DynamoDB::Table                            N/A                                           
+ Add                                           ChatGPTAliasSNAPSTART                           AWS::Lambda::Alias                              N/A                                           
+ Add                                           ChatGPTRole                                     AWS::IAM::Role                                  N/A                                           
+ Add                                           ChatGPTVersion5e83da0577                        AWS::Lambda::Version                            N/A                                           
+ Add                                           ChatGPT                                         AWS::Lambda::Function                           N/A                                           
+ Add                                           ClosingPromptEnglish                            Custom::PromptCreator                           N/A                                           
+ Add                                           ClosingPromptSpanish                            Custom::PromptCreator                           N/A                                           
+ Add                                           ConnectFlow                                     AWS::Connect::ContactFlow                       N/A                                           
+ Add                                           ContactUpdatePolicy                             AWS::IAM::ManagedPolicy                         N/A                                           
+ Add                                           ErrorPromptEnglish                              Custom::PromptCreator                           N/A                                           
+ Add                                           ErrorPromptSpanish                              Custom::PromptCreator                           N/A                                           
+ Add                                           HelpPromptEnglish                               Custom::PromptCreator                           N/A                                           
+ Add                                           HelpPromptSpanish                               Custom::PromptCreator                           N/A                                           
+ Add                                           LexBot                                          AWS::Lex::Bot                                   N/A                                           
+ Add                                           LexPromptEnglish                                Custom::PromptCreator                           N/A                                           
+ Add                                           LexPromptSpanish                                Custom::PromptCreator                           N/A                                           
+ Add                                           LexToChatGPTPerm                                AWS::Lambda::Permission                         N/A                                           
+ Add                                           LexToChatGPTSnapPerm                            AWS::Lambda::Permission                         N/A                                           
+ Add                                           LexV2ConnectIntegration                         AWS::Connect::IntegrationAssociation            N/A                                           
+ Add                                           MainPrompt                                      Custom::PromptCreator                           N/A                                           
+ Add                                           NewCallLookupAliasSNAPSTART                     AWS::Lambda::Alias                              N/A                                           
+ Add                                           NewCallLookupRole                               AWS::IAM::Role                                  N/A                                           
+ Add                                           NewCallLookupSNSTriggerPermission               AWS::Lambda::Permission                         N/A                                           
+ Add                                           NewCallLookupSNSTrigger                         AWS::SNS::Subscription                          N/A                                           
+ Add                                           NewCallLookupVersion23a3112bb1                  AWS::Lambda::Version                            N/A                                           
+ Add                                           NewCallLookup                                   AWS::Lambda::Function                           N/A                                           
+ Add                                           NewCallTopic                                    AWS::SNS::Topic                                 N/A                                           
+ Add                                           PromptBucket                                    AWS::S3::Bucket                                 N/A                                           
+ Add                                           PromptCreatorRole                               AWS::IAM::Role                                  N/A                                           
+ Add                                           PromptCreator                                   AWS::Lambda::Function                           N/A                                           
+ Add                                           SendToSNSConnectIntegration                     AWS::Connect::IntegrationAssociation            N/A                                           
+ Add                                           SendToSNSRole                                   AWS::IAM::Role                                  N/A                                           
+ Add                                           SendToSNS                                       AWS::Lambda::Function                           N/A                                           
+ Add                                           SessionTable                                    AWS::DynamoDB::Table                            N/A                                           
+ Add                                           SpanishPrompt                                   Custom::PromptCreator                           N/A                                           
+ Add                                           TransferPromptEnglish                           Custom::PromptCreator                           N/A                                           
+ Add                                           TransferPromptSpanish                           Custom::PromptCreator                           N/A                                           
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


Changeset created successfully. arn:aws:cloudformation:us-east-1::changeSet/samcli-deploy1685703701/579fda5c-92d1-4c8a-9032-547725a47612


Previewing CloudFormation changeset before deployment
======================================================
Deploy this changeset? [y/N]: y

2023-06-02 06:02:07 - Waiting for stack create/update to complete

CloudFormation events from stack operations (refresh every 5.0 seconds)
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
ResourceStatus                                  ResourceType                                    LogicalResourceId                               ResourceStatusReason                          
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
CREATE_IN_PROGRESS                              AWS::IAM::ManagedPolicy                         ContactUpdatePolicy                             -                                             
CREATE_IN_PROGRESS                              AWS::KMS::Key                                   BucketKey                                       -                                             
CREATE_IN_PROGRESS                              AWS::DynamoDB::Table                            SessionTable                                    -                                             
CREATE_IN_PROGRESS                              AWS::DynamoDB::Table                            CallTable                                       -                                             
CREATE_IN_PROGRESS                              AWS::IAM::Role                                  BotRuntimeRole                                  -                                             
CREATE_IN_PROGRESS                              AWS::SNS::Topic                                 NewCallTopic                                    -                                             
CREATE_IN_PROGRESS                              AWS::IAM::ManagedPolicy                         ContactUpdatePolicy                             Resource creation Initiated                   
CREATE_IN_PROGRESS                              AWS::IAM::Role                                  BotRuntimeRole                                  Resource creation Initiated                   
CREATE_IN_PROGRESS                              AWS::DynamoDB::Table                            SessionTable                                    Resource creation Initiated                   
CREATE_IN_PROGRESS                              AWS::DynamoDB::Table                            CallTable                                       Resource creation Initiated                   
CREATE_IN_PROGRESS                              AWS::KMS::Key                                   BucketKey                                       Resource creation Initiated                   
CREATE_IN_PROGRESS                              AWS::SNS::Topic                                 NewCallTopic                                    Resource creation Initiated                   
CREATE_COMPLETE                                 AWS::SNS::Topic                                 NewCallTopic                                    -                                             
CREATE_IN_PROGRESS                              AWS::IAM::Role                                  SendToSNSRole                                   -                                             
CREATE_IN_PROGRESS                              AWS::IAM::Role                                  SendToSNSRole                                   Resource creation Initiated                   
CREATE_COMPLETE                                 AWS::DynamoDB::Table                            SessionTable                                    -                                             
CREATE_COMPLETE                                 AWS::DynamoDB::Table                            CallTable                                       -                                             
CREATE_COMPLETE                                 AWS::IAM::ManagedPolicy                         ContactUpdatePolicy                             -                                             
CREATE_IN_PROGRESS                              AWS::IAM::Role                                  ChatGPTRole                                     -                                             
CREATE_IN_PROGRESS                              AWS::IAM::Role                                  ChatGPTRole                                     Resource creation Initiated                   
CREATE_COMPLETE                                 AWS::IAM::Role                                  BotRuntimeRole                                  -                                             
CREATE_IN_PROGRESS                              AWS::IAM::Role                                  NewCallLookupRole                               -                                             
CREATE_IN_PROGRESS                              AWS::IAM::Role                                  NewCallLookupRole                               Resource creation Initiated                   
CREATE_IN_PROGRESS                              AWS::Lex::Bot                                   LexBot                                          -                                             
CREATE_COMPLETE                                 AWS::IAM::Role                                  SendToSNSRole                                   -                                             
CREATE_IN_PROGRESS                              AWS::Lex::Bot                                   LexBot                                          Resource creation Initiated                   
CREATE_IN_PROGRESS                              AWS::Lambda::Function                           SendToSNS                                       -                                             
CREATE_IN_PROGRESS                              AWS::Lambda::Function                           SendToSNS                                       Resource creation Initiated                   
CREATE_COMPLETE                                 AWS::IAM::Role                                  ChatGPTRole                                     -                                             
CREATE_COMPLETE                                 AWS::Lambda::Function                           SendToSNS                                       -                                             
CREATE_COMPLETE                                 AWS::IAM::Role                                  NewCallLookupRole                               -                                             
CREATE_IN_PROGRESS                              AWS::Lambda::Function                           ChatGPT                                         -                                             
CREATE_IN_PROGRESS                              AWS::Connect::IntegrationAssociation            SendToSNSConnectIntegration                     -                                             
CREATE_IN_PROGRESS                              AWS::Lambda::Function                           ChatGPT                                         Resource creation Initiated                   
CREATE_IN_PROGRESS                              AWS::Lambda::Function                           NewCallLookup                                   -                                             
CREATE_IN_PROGRESS                              AWS::Connect::IntegrationAssociation            SendToSNSConnectIntegration                     Resource creation Initiated                   
CREATE_COMPLETE                                 AWS::Connect::IntegrationAssociation            SendToSNSConnectIntegration                     -                                             
CREATE_IN_PROGRESS                              AWS::Lambda::Function                           NewCallLookup                                   Resource creation Initiated                   
CREATE_COMPLETE                                 AWS::Lambda::Function                           ChatGPT                                         -                                             
CREATE_COMPLETE                                 AWS::Lex::Bot                                   LexBot                                          -                                             
CREATE_IN_PROGRESS                              AWS::Lambda::Permission                         LexToChatGPTPerm                                -                                             
CREATE_IN_PROGRESS                              AWS::Lambda::Version                            ChatGPTVersion5e83da0577                        -                                             
CREATE_IN_PROGRESS                              AWS::Lambda::Permission                         LexToChatGPTPerm                                Resource creation Initiated                   
CREATE_IN_PROGRESS                              AWS::Lambda::Version                            ChatGPTVersion5e83da0577                        Resource creation Initiated                   
CREATE_IN_PROGRESS                              AWS::Lex::BotVersion                            BotVersion                                      -                                             
CREATE_COMPLETE                                 AWS::Lambda::Function                           NewCallLookup                                   -                                             
CREATE_IN_PROGRESS                              AWS::Lex::BotVersion                            BotVersion                                      Resource creation Initiated                   
CREATE_IN_PROGRESS                              AWS::Lambda::Version                            NewCallLookupVersion23a3112bb1                  -                                             
CREATE_IN_PROGRESS                              AWS::Lambda::Version                            NewCallLookupVersion23a3112bb1                  Resource creation Initiated                   
CREATE_COMPLETE                                 AWS::Lambda::Permission                         LexToChatGPTPerm                                -                                             
CREATE_COMPLETE                                 AWS::Lex::BotVersion                            BotVersion                                      -                                             
CREATE_COMPLETE                                 AWS::KMS::Key                                   BucketKey                                       -                                             
CREATE_IN_PROGRESS                              AWS::S3::Bucket                                 PromptBucket                                    -                                             
CREATE_IN_PROGRESS                              AWS::S3::Bucket                                 PromptBucket                                    Resource creation Initiated                   
CREATE_COMPLETE                                 AWS::S3::Bucket                                 PromptBucket                                    -                                             
CREATE_IN_PROGRESS                              AWS::S3::BucketPolicy                           BucketPolicy                                    -                                             
CREATE_IN_PROGRESS                              AWS::IAM::Role                                  PromptCreatorRole                               -                                             
CREATE_IN_PROGRESS                              AWS::IAM::Role                                  PromptCreatorRole                               Resource creation Initiated                   
CREATE_IN_PROGRESS                              AWS::S3::BucketPolicy                           BucketPolicy                                    Resource creation Initiated                   
CREATE_COMPLETE                                 AWS::S3::BucketPolicy                           BucketPolicy                                    -                                             
CREATE_COMPLETE                                 AWS::IAM::Role                                  PromptCreatorRole                               -                                             
CREATE_IN_PROGRESS                              AWS::Lambda::Function                           PromptCreator                                   -                                             
CREATE_COMPLETE                                 AWS::Lambda::Version                            ChatGPTVersion5e83da0577                        -                                             
CREATE_IN_PROGRESS                              AWS::Lambda::Function                           PromptCreator                                   Resource creation Initiated                   
CREATE_IN_PROGRESS                              AWS::Lambda::Alias                              ChatGPTAliasSNAPSTART                           -                                             
CREATE_IN_PROGRESS                              AWS::Lambda::Alias                              ChatGPTAliasSNAPSTART                           Resource creation Initiated                   
CREATE_COMPLETE                                 AWS::Lambda::Alias                              ChatGPTAliasSNAPSTART                           -                                             
CREATE_COMPLETE                                 AWS::Lambda::Version                            NewCallLookupVersion23a3112bb1                  -                                             
CREATE_IN_PROGRESS                              AWS::Lambda::Permission                         LexToChatGPTSnapPerm                            -                                             
CREATE_IN_PROGRESS                              AWS::Lambda::Permission                         LexToChatGPTSnapPerm                            Resource creation Initiated                   
CREATE_IN_PROGRESS                              AWS::Lex::BotAlias                              BotAlias                                        -                                             
CREATE_IN_PROGRESS                              AWS::Lambda::Alias                              NewCallLookupAliasSNAPSTART                     -                                             
CREATE_IN_PROGRESS                              AWS::Lex::BotAlias                              BotAlias                                        Resource creation Initiated                   
CREATE_IN_PROGRESS                              AWS::Lambda::Alias                              NewCallLookupAliasSNAPSTART                     Resource creation Initiated                   
CREATE_COMPLETE                                 AWS::Lambda::Alias                              NewCallLookupAliasSNAPSTART                     -                                             
CREATE_COMPLETE                                 AWS::Lex::BotAlias                              BotAlias                                        -                                             
CREATE_COMPLETE                                 AWS::Lambda::Function                           PromptCreator                                   -                                             
CREATE_IN_PROGRESS                              AWS::SNS::Subscription                          NewCallLookupSNSTrigger                         -                                             
CREATE_IN_PROGRESS                              AWS::Lambda::Permission                         NewCallLookupSNSTriggerPermission               -                                             
CREATE_IN_PROGRESS                              AWS::Connect::ContactFlow                       ConnectFlow                                     -                                             
CREATE_IN_PROGRESS                              AWS::Lambda::Permission                         NewCallLookupSNSTriggerPermission               Resource creation Initiated                   
CREATE_IN_PROGRESS                              AWS::Connect::IntegrationAssociation            LexV2ConnectIntegration                         -                                             
CREATE_IN_PROGRESS                              AWS::SNS::Subscription                          NewCallLookupSNSTrigger                         Resource creation Initiated                   
CREATE_IN_PROGRESS                              Custom::PromptCreator                           LexPromptSpanish                                -                                             
CREATE_IN_PROGRESS                              Custom::PromptCreator                           ClosingPromptSpanish                            -                                             
CREATE_IN_PROGRESS                              Custom::PromptCreator                           TransferPromptEnglish                           -                                             
CREATE_IN_PROGRESS                              Custom::PromptCreator                           TransferPromptSpanish                           -                                             
CREATE_IN_PROGRESS                              Custom::PromptCreator                           ErrorPromptEnglish                              -                                             
CREATE_IN_PROGRESS                              Custom::PromptCreator                           ErrorPromptSpanish                              -                                             
CREATE_IN_PROGRESS                              Custom::PromptCreator                           ClosingPromptEnglish                            -                                             
CREATE_COMPLETE                                 AWS::SNS::Subscription                          NewCallLookupSNSTrigger                         -                                             
CREATE_IN_PROGRESS                              Custom::PromptCreator                           HelpPromptEnglish                               -                                             
CREATE_IN_PROGRESS                              Custom::PromptCreator                           MainPrompt                                      -                                             
CREATE_IN_PROGRESS                              Custom::PromptCreator                           LexPromptEnglish                                -                                             
CREATE_IN_PROGRESS                              Custom::PromptCreator                           SpanishPrompt                                   -                                             
CREATE_IN_PROGRESS                              Custom::PromptCreator                           HelpPromptSpanish                               -                                             
CREATE_IN_PROGRESS                              AWS::Connect::ContactFlow                       ConnectFlow                                     Resource creation Initiated                   
CREATE_COMPLETE                                 AWS::Connect::ContactFlow                       ConnectFlow                                     -                                             
CREATE_IN_PROGRESS                              AWS::Connect::IntegrationAssociation            LexV2ConnectIntegration                         Resource creation Initiated                   
CREATE_COMPLETE                                 AWS::Connect::IntegrationAssociation            LexV2ConnectIntegration                         -                                             
CREATE_COMPLETE                                 AWS::Lambda::Permission                         LexToChatGPTSnapPerm                            -                                             
CREATE_IN_PROGRESS                              Custom::PromptCreator                           ClosingPromptSpanish                            Resource creation Initiated                   
CREATE_IN_PROGRESS                              Custom::PromptCreator                           ClosingPromptEnglish                            Resource creation Initiated                   
CREATE_COMPLETE                                 Custom::PromptCreator                           ClosingPromptSpanish                            -                                             
CREATE_COMPLETE                                 Custom::PromptCreator                           ClosingPromptEnglish                            -                                             
CREATE_IN_PROGRESS                              Custom::PromptCreator                           SpanishPrompt                                   Resource creation Initiated                   
CREATE_IN_PROGRESS                              Custom::PromptCreator                           LexPromptSpanish                                Resource creation Initiated                   
CREATE_COMPLETE                                 Custom::PromptCreator                           SpanishPrompt                                   -                                             
CREATE_IN_PROGRESS                              Custom::PromptCreator                           TransferPromptSpanish                           Resource creation Initiated                   
CREATE_COMPLETE                                 Custom::PromptCreator                           LexPromptSpanish                                -                                             
CREATE_IN_PROGRESS                              Custom::PromptCreator                           ErrorPromptEnglish                              Resource creation Initiated                   
CREATE_COMPLETE                                 Custom::PromptCreator                           TransferPromptSpanish                           -                                             
CREATE_IN_PROGRESS                              Custom::PromptCreator                           ErrorPromptSpanish                              Resource creation Initiated                   
CREATE_IN_PROGRESS                              Custom::PromptCreator                           HelpPromptEnglish                               Resource creation Initiated                   
CREATE_IN_PROGRESS                              Custom::PromptCreator                           TransferPromptEnglish                           Resource creation Initiated                   
CREATE_IN_PROGRESS                              Custom::PromptCreator                           MainPrompt                                      Resource creation Initiated                   
CREATE_IN_PROGRESS                              Custom::PromptCreator                           LexPromptEnglish                                Resource creation Initiated                   
CREATE_COMPLETE                                 Custom::PromptCreator                           ErrorPromptEnglish                              -                                             
CREATE_COMPLETE                                 Custom::PromptCreator                           ErrorPromptSpanish                              -                                             
CREATE_IN_PROGRESS                              Custom::PromptCreator                           HelpPromptSpanish                               Resource creation Initiated                   
CREATE_COMPLETE                                 Custom::PromptCreator                           HelpPromptEnglish                               -                                             
CREATE_COMPLETE                                 Custom::PromptCreator                           TransferPromptEnglish                           -                                             
CREATE_COMPLETE                                 Custom::PromptCreator                           MainPrompt                                      -                                             
CREATE_COMPLETE                                 Custom::PromptCreator                           LexPromptEnglish                                -                                             
CREATE_COMPLETE                                 Custom::PromptCreator                           HelpPromptSpanish                               -                                             
CREATE_COMPLETE                                 AWS::Lambda::Permission                         NewCallLookupSNSTriggerPermission               -                                             
CREATE_COMPLETE                                 AWS::CloudFormation::Stack                      connect-chatgpt                                 -                                             
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


Successfully created/updated stack - connect-chatgpt in us-east-1

```
