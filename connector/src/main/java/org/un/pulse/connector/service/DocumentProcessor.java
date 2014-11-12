package org.un.pulse.connector.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import org.apache.http.client.HttpClient;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.un.pulse.connector.model.Document;
import org.un.pulse.connector.model.Document.DocumentType;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

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

    private static DateTimeFormatter PDF_DATE = DateTimeFormat.forPattern("'D:'yyyyMMddHHmmssZ");

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
        long start = System.currentTimeMillis();
        Iterable<Document> analyzedDocs = analyzer.analyze(document);
        LOGGER.info("Sentiment analysis took " + (System.currentTimeMillis() - start) + " ms");

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
        Document docModel = new Document();
        docModel.type = DocumentType.pdf;
        docModel.url = url.toString();

        PdfReader reader = new PdfReader(url);
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        TextExtractionStrategy strategy;
        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
            docModel.text = strategy.getResultantText();
        }

        Map<String, String> info = reader.getInfo();
        String dateStr = info.get("CreationDate");
        DateTime date = PDF_DATE.parseDateTime(dateStr.replace("'", ""));
        docModel.createDate = ISODateTimeFormat.dateTime().print(date);

        return docModel;
    }

    /**
     * Parses a PDF to a plain text file.
     * @param pdf the original PDF
     * @param txt the resulting text
     * @throws IOException
     */
    public void parsePdf(String pdf, String txt) throws IOException {


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
