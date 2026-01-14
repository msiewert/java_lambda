# Java AWS Lambda REST API with DynamoDB

A REST API built with Java Lambda that queries RetroPie gaming statistics from DynamoDB.

## Purpose

This project demonstrates:
- REST API with API Gateway + Lambda
- DynamoDB integration with AWS SDK v2
- Filtering and querying data
- Infrastructure as Code with CDK
- Real-world AWS modernization patterns

## API Endpoint

**POST** `/prod/` - Query RetroPie stats by emulator and/or game

**Request Body:**
```json
{
  "emulator": "nes",
  "game": "Joust (U)"
}
```

Both fields are optional. Omit to return all records.

**Response:**
```json
[
  {
    "timestamp": "2025-09-13T22:45:42.124682",
    "action": "start",
    "emulator": "nes",
    "game": "Joust (U)"
  }
]
```

## Requirements

### Tools
- **Java JDK 11, 17, or 21** - Runtime for Lambda and CDK
- **Node.js and npm** - Required for AWS CDK CLI
- **AWS CDK CLI** - Install with `npm install -g aws-cdk`
- **AWS CLI** - For AWS credentials and account access
- **Gradle** - Build tool (Gradle Wrapper included in project)

### AWS Setup
1. Configure AWS credentials: `aws configure`
2. Bootstrap CDK (one-time): `cdk bootstrap`

## VS Code Extensions (Recommended)

- **Extension Pack for Java** (by Microsoft) - Java language support
- **Gradle for Java** (by Microsoft) - Gradle build support
- **AWS Toolkit** (by Amazon Web Services) - Optional, for visual AWS resource management

## Project Structure

```
java_lambda/
├── src/main/java/
│   ├── com/example/lambda/
│   │   ├── RetroPieStatsHandler.java  # Lambda function code
│   │   └── model/
│   │       ├── RetroPieStats.java     # Data model
│   │       └── FilterRequest.java     # Request model
│   └── com/example/cdk/
│       ├── LambdaApp.java             # CDK app entry point
│       └── LambdaStack.java           # CDK stack definition
├── build.gradle.kts                   # Gradle build configuration
├── cdk.json                           # CDK configuration
└── README.md
```

## Build and Deploy

### 1. Build the Lambda JAR
```bash
./gradlew buildLambda
```

### 2. Synthesize CloudFormation template (optional)
```bash
cdk synth
```

### 3. Deploy to AWS
```bash
cdk deploy
```

### 4. Clean up (delete stack)
```bash
cdk destroy
```

## What Gets Deployed

- AWS Lambda function running Java 21
- API Gateway REST API with POST endpoint
- IAM role with DynamoDB read permissions
- CloudWatch Logs group for function logs

## Testing the API

After deployment, CDK outputs the API URL. Test with:

```bash
# Get all records
curl -X POST https://<api-id>.execute-api.<region>.amazonaws.com/prod/ \
  -H "Content-Type: application/json" \
  -d '{}'

# Filter by emulator
curl -X POST https://<api-id>.execute-api.<region>.amazonaws.com/prod/ \
  -H "Content-Type: application/json" \
  -d '{"emulator": "nes"}'

# Filter by game
curl -X POST https://<api-id>.execute-api.<region>.amazonaws.com/prod/ \
  -H "Content-Type: application/json" \
  -d '{"game": "Joust (U)"}'

# Filter by both
curl -X POST https://<api-id>.execute-api.<region>.amazonaws.com/prod/ \
  -H "Content-Type: application/json" \
  -d '{"emulator": "nes", "game": "Joust (U)"}'
```
