package org.un.pulse.connector.service;

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
import org.elasticsearch.common.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.un.pulse.connector.model.Document;
import org.un.pulse.connector.model.Document.DocumentType;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gregswensen
 * Date: 11/11/14
 * Time: 1:04 PM
 */
@Component
@ConfigurationProperties(prefix = "processor")
public class DocumentProcessor {
    private static Logger LOGGER = LoggerFactory.getLogger(DocumentProcessor.class);

    @Autowired
    private HttpClient client;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Client searchNode;

    @Autowired
    private DocumentAnalyzer analyzer;

    private String indexName = "docs";

    public void indexDocument(Document document, String indexType) {
        if (document == null) {
            return;
        }
        Iterable<Document> analyzedDocs = analyzer.analyze(document);

        int segment = 0;
        for (Document doc : analyzedDocs) {
            try {
                IndexRequestBuilder indexer = searchNode.prepareIndex();
                indexer.setId(Hashing.md5().hashBytes((Integer.toString(++segment) + document.url).getBytes()).toString());
                doc.segment = segment;
                indexer.setIndex(indexName);
                indexer.setType(indexType);
                indexer.setSource(objectMapper.writeValueAsString(doc));

                searchNode.index(indexer.request());
            } catch (IOException ioe) {
                LOGGER.error("Could not index document: " + document);
            }
        }
    }

    public Document parseDocument(DocumentReference reference) throws IOException {
        return getDocumentForPDF(new URL(reference.url));
    }

    private Document getDocumentForPDF(URL url) throws IOException {
        HttpGet get = new HttpGet(url.toString());

        HttpResponse response = client.execute(new HttpHost(url.getHost()), get);
        if (response.getStatusLine().getStatusCode() >= 400 && response.getStatusLine().getStatusCode() < 500) {
            return null;
        }
        Document docModel = new Document();
        docModel.type = DocumentType.pdf;
        docModel.url = url.toString();

        PDDocument doc = null;
        try {
            doc = new PDDocument().load(response.getEntity().getContent());

            StringWriter writer = new StringWriter();
            new PDFTextStripper().writeText(doc, writer);
            docModel.text = writer.toString();

            docModel.title = doc.getDocumentInformation().getTitle();
            return docModel;
        } finally {
            if (doc != null) {
                doc.close();
            }
        }
    }

    /**
     * { "type":"PDF", "url": "...", "index":"}
     */
    public static class DocumentReference {
        public DocumentType type;
        public String url;
        public String index;
    }
}
