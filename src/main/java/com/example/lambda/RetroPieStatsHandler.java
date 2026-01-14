package com.example.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.lambda.model.RetroPieStats;
import com.google.gson.Gson;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RetroPieStatsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final DynamoDbClient dynamoDb = DynamoDbClient.create();
    private final Gson gson = new Gson();
    private final String tableName = System.getenv("TABLE_NAME");

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            Map<String, String> queryParams = request.getQueryStringParameters();
            String emulator = queryParams != null ? queryParams.get("emulator") : null;
            
            ScanRequest.Builder scanBuilder = ScanRequest.builder().tableName(tableName);
            
            if (emulator != null) {
                Map<String, String> expressionNames = new HashMap<>();
                Map<String, AttributeValue> expressionValues = new HashMap<>();
                
                expressionNames.put("#emulator", "emulator");
                expressionValues.put(":emulator", AttributeValue.builder().s(emulator).build());
                
                scanBuilder.filterExpression("#emulator = :emulator")
                          .expressionAttributeNames(expressionNames)
                          .expressionAttributeValues(expressionValues);
            }
            
            ScanResponse response = dynamoDb.scan(scanBuilder.build());
            
            List<RetroPieStats> results = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                RetroPieStats stat = new RetroPieStats(
                    item.get("timestamp").s(),
                    item.get("action").s(),
                    item.get("emulator").s(),
                    item.get("game").s()
                );
                results.add(stat);
            }
            
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(gson.toJson(results))
                .withHeaders(Map.of("Content-Type", "application/json"));
                
        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(500)
                .withBody("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
