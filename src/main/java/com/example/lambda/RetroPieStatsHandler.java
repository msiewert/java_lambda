package com.example.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.lambda.model.RetroPieStats;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.core.exception.SdkException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class RetroPieStatsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger logger = LoggerFactory.getLogger(RetroPieStatsHandler.class);
    private static final Set<String> VALID_EMULATORS = Set.of("nes", "snes", "genesis", "n64", "gba", "psx");
    private static final int MAX_EMULATOR_LENGTH = 20;
    
    private final DynamoDbClient dynamoDb = DynamoDbClient.create();
    private final Gson gson = new Gson();
    private final String tableName = System.getenv("TABLE_NAME");

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        logger.info("Processing request for path: {}", request.getPath());
        
        try {
            Optional<String> emulator = Optional.ofNullable(request.getQueryStringParameters())
                .map(params -> params.get("emulator"));
            
            // Validate emulator if present
            if (emulator.isPresent()) {
                String emu = emulator.get();
                
                if (emu.isBlank()) {
                    logger.warn("Empty emulator parameter provided");
                    return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody("{\"error\": \"Emulator parameter cannot be empty\"}")
                        .withHeaders(Map.of("Content-Type", "application/json"));
                }
                
                if (emu.length() > MAX_EMULATOR_LENGTH) {
                    logger.warn("Emulator parameter too long: {}", emu.length());
                    return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody("{\"error\": \"Emulator parameter too long\"}")
                        .withHeaders(Map.of("Content-Type", "application/json"));
                }
                
                if (!VALID_EMULATORS.contains(emu.toLowerCase())) {
                    logger.warn("Invalid emulator: {}", emu);
                    return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody("{\"error\": \"Invalid emulator. Valid values: " + String.join(", ", VALID_EMULATORS) + "\"}")
                        .withHeaders(Map.of("Content-Type", "application/json"));
                }
                
                logger.info("Filtering by emulator: {}", emu);
            }
            
            ScanRequest.Builder scanBuilder = ScanRequest.builder().tableName(tableName);
            
            emulator.ifPresent(emu -> {
                Map<String, String> expressionNames = new HashMap<>();
                Map<String, AttributeValue> expressionValues = new HashMap<>();
                
                expressionNames.put("#emulator", "emulator");
                expressionValues.put(":emulator", AttributeValue.builder().s(emu).build());
                
                scanBuilder.filterExpression("#emulator = :emulator")
                          .expressionAttributeNames(expressionNames)
                          .expressionAttributeValues(expressionValues);
            });
            
            ScanResponse response = dynamoDb.scan(scanBuilder.build());
            
            List<RetroPieStats> results = response.items().stream()
                .map(item -> new RetroPieStats(
                    item.get("timestamp").s(),
                    item.get("action").s(),
                    item.get("emulator").s(),
                    item.get("game").s()
                ))
                .collect(Collectors.toList());
            
            logger.info("Returning {} results", results.size());
            
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(gson.toJson(results))
                .withHeaders(Map.of("Content-Type", "application/json"));
                
        } catch (DynamoDbException e) {
            logger.error("DynamoDB error: {}", e.getMessage(), e);
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(503)
                .withBody("{\"error\": \"Database unavailable\"}")
                .withHeaders(Map.of("Content-Type", "application/json"));
        } catch (SdkException e) {
            logger.error("AWS SDK error: {}", e.getMessage(), e);
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(502)
                .withBody("{\"error\": \"Service error\"}")
                .withHeaders(Map.of("Content-Type", "application/json"));
        } catch (NullPointerException e) {
            logger.error("Missing data: {}", e.getMessage(), e);
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(500)
                .withBody("{\"error\": \"Invalid data format\"}")
                .withHeaders(Map.of("Content-Type", "application/json"));
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(500)
                .withBody("{\"error\": \"Internal server error\"}")
                .withHeaders(Map.of("Content-Type", "application/json"));
        }
    }
}
