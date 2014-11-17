package org.unpulse.workflow;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by asundaram on 11/11/14.
 */
public class AmazonDynamoHelper {
    private static AmazonDynamoDBClient dynamo;
    private static String tableName;

    public static void initialize(AmazonDynamoDBClient dynamo, String tableName) {
        AmazonDynamoHelper.dynamo = dynamo;
        AmazonDynamoHelper.tableName = tableName;
    }

    public static void AddItemToDynamo(String url)
    {
        Map<String, AttributeValue> item = newItem(url);
        PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
        PutItemResult putItemResult = dynamo.putItem(putItemRequest);
    }

    public static boolean CheckIfAdded(String url)
    {
        Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
        key.put("Url", new AttributeValue().withS(url));

        GetItemRequest getItemRequest = new GetItemRequest()
                .withTableName(tableName)
                .withKey(key);

        try {
            GetItemResult result = dynamo.getItem(getItemRequest);
            if(result.getItem() == null)
            {
                return false;
            }
            return true;
        }catch (Exception ex) {
            return false;
        }
    }

    private static Map<String, AttributeValue> newItem(String url) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("Url", new AttributeValue(url));
        item.put("processed", new AttributeValue("0"));
        // Add additional metadata about the Url if any

        return item;
    }
}
