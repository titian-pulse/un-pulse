package org.unpulse.workflow;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.regions.Region;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.AmazonSQSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created with IntelliJ IDEA.
 * User: gregswensen
 * Date: 11/11/14
 * Time: 11:34 AM
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Application {
    //private static string accessKey
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Application.class);
        application.setWebEnvironment(false);
        application.run(args);
    }

    @Bean
    public AmazonSQSClient amazonSQSClient(@Value("${aws.sqsAccessKey}") final String accessKey,
                                           @Value("${aws.sqsSecretKey}") final String secretKey,
                                           @Value("${aws.queueUrl}") final String queueUrl
    ) {
        AmazonSQSClient client = new AmazonSQSAsyncClient(new StaticCredentialsProvider(new AWSCredentials() {
            @Override
            public String getAWSAccessKeyId() {
                return accessKey;
            }

            @Override
            public String getAWSSecretKey() {
                return secretKey;
            }
        }));


        AmazonSqsHelper.initialize(client, queueUrl);

        return client;
    }

    @Bean
    public AmazonDynamoDBClient amazonDynamoClient(@Value("${aws.sqsAccessKey}") final String accessKey,
                                                   @Value("${aws.sqsSecretKey}") final String secretKey,
                                                   @Value("${aws.dynamoTable}") final String tableName) {

        AmazonDynamoDBClient dynClient = new AmazonDynamoDBClient(new StaticCredentialsProvider(new AWSCredentials() {
            @Override
            public String getAWSAccessKeyId() {
                return accessKey;
            }

            @Override
            public String getAWSSecretKey() {
                return secretKey;
            }
        }));

        com.amazonaws.regions.Region usWest2 = com.amazonaws.regions.Region.getRegion(Regions.US_WEST_1);
        dynClient.setRegion(usWest2);

        AmazonDynamoHelper.initialize(dynClient, tableName);

        return dynClient;
    }
}