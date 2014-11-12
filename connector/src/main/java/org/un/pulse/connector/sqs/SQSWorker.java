package org.un.pulse.connector.sqs;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.un.pulse.connector.model.Document;
import org.un.pulse.connector.model.Document.DocumentType;
import org.un.pulse.connector.service.DocumentProcessor;
import org.un.pulse.connector.service.DocumentProcessor.DocumentReference;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: gregswensen
 * Date: 11/11/14
 * Time: 11:16 AM
 */
@Component
@ConfigurationProperties("aws")
public class SQSWorker implements CommandLineRunner {
    private static Logger LOGGER = LoggerFactory.getLogger(SQSWorker.class);

    private String queueUrl;
    private int workerCount;

    @Autowired
    private DocumentProcessor documentProcessor;

    @Autowired
    private AmazonSQSClient sqsClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        while (true) {
            try {
                ReceiveMessageRequest request = new ReceiveMessageRequest();
                request.withWaitTimeSeconds(20).withQueueUrl(queueUrl).withMaxNumberOfMessages(1);
                ReceiveMessageResult messages = sqsClient.receiveMessage(request);

                if (messages != null && messages.getMessages() != null && !messages.getMessages().isEmpty()) {
                    DeleteMessageBatchRequest deleteMessageBatchRequest = new DeleteMessageBatchRequest().withQueueUrl(queueUrl);
                    Collection<DeleteMessageBatchRequestEntry> deleteEntries = Lists.newArrayList();

                    int i = 0;
                    for (Message message : messages.getMessages()) {
                        try {
                            DocumentReference reference = objectMapper.readValue(message.getBody(), DocumentReference.class);
                            Document document = documentProcessor.parseDocument(reference);
                            documentProcessor.indexDocument(document, reference.index);
                        } catch (Exception e) {
                            LOGGER.error("Failed processing message: " + message.getBody());
                            continue;
                        }
                        deleteEntries.add(new DeleteMessageBatchRequestEntry(Integer.toString(++i), message.getReceiptHandle()));
                    }

                    sqsClient.deleteMessageBatch(deleteMessageBatchRequest.withEntries(deleteEntries));
                    LOGGER.info("Processed " + i + " messages from SQS");
                }
            } catch (Exception e) {
                LOGGER.error("Caught exception trying to proccess messages from SQS", e);
            }
        }

    }

    public String getQueueUrl() {
        return queueUrl;
    }

    public void setQueueUrl(String queueUrl) {
        this.queueUrl = queueUrl;
    }

    public int getWorkerCount() {
        return workerCount;
    }

    public void setWorkerCount(int workerCount) {
        this.workerCount = workerCount;
    }
}
