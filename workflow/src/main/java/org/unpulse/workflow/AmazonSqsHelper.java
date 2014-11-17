package org.unpulse.workflow;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * Created by asundaram on 11/11/14.
 */
@ConfigurationProperties(prefix = "aws")
public class AmazonSqsHelper {

    private static AmazonSQSClient sqs;

    private static String queueUrl = "https://sqs.us-west-1.amazonaws.com/789353145558/PdfQueue";//sqs.getQueueUrl("PdfQueue").getQueueUrl();

    public static void AddMessageToQueue(String url){
        String json = getJson(url);
        sqs.sendMessage(new SendMessageRequest(queueUrl, json));
    }

    public static String getJson(String url)
    {
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", "pdf");
            obj.put("url", url);
            obj.put("index", "unorg");
        }
        catch (Exception ex)
        {
            return "";
        }

        return obj.toString();
    }

    public static void initialize(AmazonSQSClient sqs, String queueUrl) {
        AmazonSqsHelper.sqs = sqs;
        AmazonSqsHelper.queueUrl = queueUrl;
    }
}
