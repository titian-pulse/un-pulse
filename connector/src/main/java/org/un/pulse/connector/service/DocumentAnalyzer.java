package org.un.pulse.connector.service;

import org.un.pulse.connector.model.AnalyzedDocument;
import org.un.pulse.connector.model.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by earaya on 11/11/14.
 */
public class DocumentAnalyzer {

    public Iterable<AnalyzedDocument> analyze(Document document) {
        List<AnalyzedDocument> analyzedDocuments = new ArrayList<AnalyzedDocument>(2);
        for (int i = 0; i <= 2; i++) {
            AnalyzedDocument analyzedDocument = (AnalyzedDocument) document;
            analyzedDocument.sentiment = 0.5;
            analyzedDocuments.add(analyzedDocument);
        }

        return analyzedDocuments;
    }
}
