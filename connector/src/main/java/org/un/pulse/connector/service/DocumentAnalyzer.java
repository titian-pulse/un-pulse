package org.un.pulse.connector.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.springframework.beans.factory.annotation.Autowired;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;
import org.un.pulse.connector.model.AnalyzedDocument;
import org.un.pulse.connector.model.Document;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Created by earaya on 11/11/14.
 */
@Component
public class DocumentAnalyzer {

    private static final Pattern WHITESPACE = Pattern.compile("^\\s*$");
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

        Iterable<String> documentChuncks = splitDocumentText(document.text);

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

    private Iterable<String> splitDocumentText(String text) {
        Iterable<String> words = Splitter.on(" ").omitEmptyStrings().split(text);
        Iterable<List<String>> lists = Iterables.partition(words, 50);

        List<String> chunks = Lists.newArrayList();
        for (List<String> list : lists) {
            chunks.add(Joiner.on(" ").join(list));
        }

        return chunks;
    }
}
