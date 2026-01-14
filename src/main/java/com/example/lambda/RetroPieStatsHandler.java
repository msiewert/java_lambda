package com.example.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.lambda.model.FilterRequest;
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
            FilterRequest filter = gson.fromJson(request.getBody(), FilterRequest.class);
            
            ScanRequest.Builder scanBuilder = ScanRequest.builder().tableName(tableName);
            
            if (filter.getEmulator() != null || filter.getGame() != null) {
                Map<String, String> expressionNames = new HashMap<>();
                Map<String, AttributeValue> expressionValues = new HashMap<>();
                List<String> conditions = new ArrayList<>();
                
                if (filter.getEmulator() != null) {
                    expressionNames.put("#emulator", "emulator");
                    expressionValues.put(":emulator", AttributeValue.builder().s(filter.getEmulator()).build());
                    conditions.add("#emulator = :emulator");
                }
                
                if (filter.getGame() != null) {
                    expressionNames.put("#game", "game");
                    expressionValues.put(":game", AttributeValue.builder().s(filter.getGame()).build());
                    conditions.add("#game = :game");
                }
                
                scanBuilder.filterExpression(String.join(" AND ", conditions))
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
