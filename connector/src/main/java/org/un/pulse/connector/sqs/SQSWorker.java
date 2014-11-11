package org.un.pulse.connector.sqs;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.stereotype.Component;
import org.un.pulse.connector.model.Document;
import org.un.pulse.connector.model.Document.DocumentType;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: gregswensen
 * Date: 11/11/14
 * Time: 11:16 AM
 */
@Component
public class SQSWorker {
    private static Logger LOGGER = LoggerFactory.getLogger(SQSWorker.class);




}
