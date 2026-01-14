package com.example.cdk;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.dynamodb.ITable;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.SnapStartConf;
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
                .snapStart(SnapStartConf.ON_PUBLISHED_VERSIONS)
                .build();

        table.grantReadData(statsFunction);

        RestApi api = RestApi.Builder.create(this, "RetroPieStatsApi").build();
        Resource stats = api.getRoot().addResource("stats");
        stats.addMethod("GET", new LambdaIntegration(statsFunction));
    }
}
