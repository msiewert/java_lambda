package com.example.cdk;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.LambdaRestApi;
import software.amazon.awscdk.services.dynamodb.ITable;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import java.util.Map;

public class LambdaStack extends Stack {
    public LambdaStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        ITable table = Table.fromTableName(this, "RetroPieTable", 
            "RetroPieStatsStack-RetroPieStatsTable726CDF07-1V4WQXYFJCJLS");

        Function statsFunction = Function.Builder.create(this, "RetroPieStatsFunction")
                .runtime(Runtime.JAVA_21)
                .code(Code.fromAsset("build/libs/lambda.jar"))
                .handler("com.example.lambda.RetroPieStatsHandler::handleRequest")
                .environment(Map.of("TABLE_NAME", table.getTableName()))
                .timeout(Duration.seconds(90))
                .build();

        table.grantReadData(statsFunction);

        LambdaRestApi.Builder.create(this, "RetroPieStatsApi")
                .handler(statsFunction)
                .build();
    }
}
