package org.un.pulse.connector.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.un.pulse.connector.model.AnalyzedDocument;
import org.un.pulse.connector.model.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by earaya on 11/11/14.
 */
@Component
public class DocumentAnalyzer {

    private final StanfordCoreNLP nlp;

    @Autowired
    private ObjectMapper mapper;

    public DocumentAnalyzer() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        nlp = new StanfordCoreNLP(props);
    }

    public Iterable<Document> analyze(Document document) {
        List<Document> analyzedDocuments = new ArrayList<Document>();

        String[] sentimentText = { "Very Negative","Negative", "Neutral", "Positive", "Very Positive"};

        String [] documentChuncks = splitDocumentText(document.text);

        for (String documentChunk : documentChuncks) {
            Annotation annotation = nlp.process(documentChunk);
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
                int score = RNNCoreAnnotations.getPredictedClass(tree);
                AnalyzedDocument analyzedDocument = mapper.convertValue(document, AnalyzedDocument.class);
                document.text = documentChunk;
                analyzedDocument.sentiment = sentimentText[score];
                analyzedDocuments.add(analyzedDocument);
            }
        }

        return analyzedDocuments;
    }

    private String[] splitDocumentText(String text) {

        return text.split("\\r?\\n");
    }
}
